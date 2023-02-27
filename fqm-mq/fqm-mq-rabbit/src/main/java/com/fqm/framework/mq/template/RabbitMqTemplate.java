package com.fqm.framework.mq.template;

import com.fqm.framework.common.core.util.JsonUtil;
import com.fqm.framework.common.core.util.ReturnParamUtil;
import com.fqm.framework.common.core.util.system.SystemUtil;
import com.fqm.framework.common.http.HttpUtil;
import com.fqm.framework.mq.MqMode;
import com.fqm.framework.mq.api.RabbitQueueMessage;
import com.fqm.framework.mq.api.RabbitQueueMessageQueryRequest;
import com.fqm.framework.mq.callback.RabbitListenableFutureCallback;
import com.fqm.framework.mq.client.producer.SendCallback;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.ChannelCallback;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.LockSupport;

/**
 * Rabbit消息队列，发送都是异步消息
 * 
 * @version 
 * @author 傅泉明
 */
public class RabbitMqTemplate implements MqTemplate {
    
    private Logger logger = LoggerFactory.getLogger(getClass());
    
    private RabbitTemplate rabbitTemplate;
    private AmqpAdmin amqpAdmin;
    private RabbitProperties rabbitProperties;
    /** api请求端口：即rabbitmq控制台访问的端口，默认15672 */
    private int apiPort;
    /** 生产者对应默认的队列列表 */
    private Set<String> topicSet = ConcurrentHashMap.newKeySet();
    /** 消费者组队列列表 */
    private Set<String> groupSet = ConcurrentHashMap.newKeySet();
    
    private AtomicLong atomicLong = new AtomicLong();
    
    private String hostAddress = SystemUtil.getHostAddress();
    
    private long pid = SystemUtil.getCurrentPid();
    /** 延迟任务的信息头 */
    public static final String HEADER_DELAY = "rabbit-delay";
    
    private String messageStr = "],message=[";
    
    public RabbitMqTemplate(RabbitTemplate rabbitTemplate, AmqpAdmin amqpAdmin, RabbitProperties rabbitProperties, int apiPort) {
        this.rabbitTemplate = rabbitTemplate;
        this.amqpAdmin = amqpAdmin;
        this.rabbitProperties = rabbitProperties;
        this.apiPort = apiPort;
    }
    
    /**
     * 1.生产者：
     *   exchange：生成type为x-delayed-message(延迟交换机)，名称为topic
     *   queue：生成属性为 x-message-ttl(过期时间3天)，名称为topic
     *   绑定：exchange绑定queue，路由key为#
     * 2.消费者：
     *   exchange：生成type为x-delayed-message(延迟交换机)，名称为topic
     *   exchange-dlq：生成type为direct(死信交换机)，名称为DLQ
     *   queue：生成属性为 x-dead-letter-exchange(死信交换机:DLQ)、
     *          x-dead-letter-routing-key(死信交换机的路由key:topic + . + group)，
     *          名称为topic + . + group
     *   绑定：exchange绑定queue，路由key为#
     *        exchange-dlq绑定queue，路由key为topic + . + group
     *   如果 queue 未生成：将名称为 topic 的queue数据复制到该queue中
     *      1）通过api调用控台获取topic名称的queue所有数据，发送到消费者组的queue中
     *   该消费者组中的数据可能有重复数据
     *   
     * @param topic
     * @param group
     * @return 队列名称：topic 或 topic.group
     */
    public String initTopic(String topic, String group) {
        // 是否生产者
        boolean isProducer = false;
        String queueName = null;
        if (StringUtils.isBlank(group)) {
            isProducer = true;
            queueName = topic;
        } else {
            queueName = topic + "." + group;
        }
        
        if (isProducer) {
            if (!topicSet.contains(queueName)) {
                buildTopic(topic, queueName);
                topicSet.add(queueName);
            }
        } else {
            // 死信主题监听不用消费者组
            if (isDlqTopic(topic)) {
                queueName = topic;
            }
            if (!groupSet.contains(queueName)) {
                buildGroup(topic, queueName);
                groupSet.add(queueName);
            }
        }
        return queueName;
    }
    
    /**
     * 是否死信主题
     * @param topic
     * @return
     */
    private boolean isDlqTopic(String topic) {
        return topic.endsWith(".DLQ");
    }

    /**
     * 构建消费者组的队列
     * @param topic
     * @param queueName
     */
    private void buildGroup(String topic, String queueName) {
        // 查询消费者组队列
        QueueInformation queueInfo = amqpAdmin.getQueueInfo(queueName);
        if (null == queueInfo) {
            // 监听死信队列则只初始化该队列
            if (isDlqTopic(topic)) {
                if (null == amqpAdmin.getQueueInfo(topic)) {
                    // 死信队列
                    Queue dlqQueue = new Queue(topic, true, false, false);
                    amqpAdmin.declareQueue(dlqQueue);
                    logger.info("Init RabbitGroupDlqQueue={}", dlqQueue.getName());
                }
                return;
            }
            // 死信交换机名称
            String dlqExchangeName = "DLQ";
            // 1、死信交换机
            DirectExchange dlqExchange = new DirectExchange(dlqExchangeName);
            amqpAdmin.declareExchange(dlqExchange);
            logger.info("Init RabbitDLQExchange={}", dlqExchangeName);
            // 2、死信消费者组队列
            Queue dlqQueue = new Queue(queueName + ".DLQ", true, false, false);
            amqpAdmin.declareQueue(dlqQueue);
            logger.info("Init RabbitGroupDlqQueue={}", dlqQueue.getName());
            // 3、死信交换机绑定死信消费者组队列
            amqpAdmin.declareBinding(BindingBuilder.bind(dlqQueue).to(dlqExchange).with(queueName));
            
            // 4、创建消费者组队列
            Map<String, Object> args = new HashMap<>(2);
            // 死信交换机
            args.put("x-dead-letter-exchange", dlqExchangeName);
            // 死信交换机路由key
            args.put("x-dead-letter-routing-key", queueName);
            Queue groupQueue = new Queue(queueName, true, false, false, args);
            amqpAdmin.declareQueue(groupQueue);
            logger.info("Init RabbitGroupQueue={}", groupQueue.getName());
            
            // 5、创建主题
            TopicExchange topicExchange = new TopicExchange(topic, true, false);
            // 延迟队列
            topicExchange.setDelayed(true);
            amqpAdmin.declareExchange(topicExchange);
            logger.info("Init RabbitExchange={}", topic);
            // 6、绑定主题和消费者组
            amqpAdmin.declareBinding(BindingBuilder.bind(groupQueue).to(topicExchange).with("#"));
            
            // 7、同步名称为topic的queue数据
            QueueInformation topicQueueInfo = amqpAdmin.getQueueInfo(topic);
            if (null != topicQueueInfo) {
                int messageCount = topicQueueInfo.getMessageCount();
                if (messageCount > 0) {
                    buildTopicPreviousMessageToGroup(topic, queueName, groupQueue, messageCount);
                }
            }
        }
    }
    /**
     * 将主题之前的消息推送到消费者组的队列中
     * @param topic
     * @param queueName
     * @param groupQueue
     * @param messageCount
     */
    private void buildTopicPreviousMessageToGroup(String topic, String queueName, Queue groupQueue, int messageCount) {
        // 获取2倍数据，避免可能期间出现新的数据
        messageCount = messageCount * 2;
        String separator = "/";
        String virtualHost = rabbitProperties.getVirtualHost();
        if (StringUtils.isBlank(virtualHost)) {
            // 默认 /
            virtualHost = separator;
        }
        StringBuilder urlData = new StringBuilder("http://");
        urlData.append(rabbitProperties.getHost()).append(":").append(apiPort)
        .append("/api/queues/");
        if (separator.equals(virtualHost)) {
            urlData.append("%2F");
        } else {
            urlData.append(virtualHost);
        }
        urlData.append(separator).append(topic).append("/get");
        // 获取消息 http://192.168.86.145:15672/api/queues/virtualHost/topic/get
        String url = urlData.toString();
        RabbitQueueMessageQueryRequest request = new RabbitQueueMessageQueryRequest();
        request.setCount(String.valueOf(messageCount));
        request.setName(topic);
        Map<String, String> headerMap = new HashMap<>(2);
        String auth = rabbitProperties.getUsername() + ":" + rabbitProperties.getPassword();
        auth = Base64.encodeBase64String(auth.getBytes(StandardCharsets.UTF_8));
        headerMap.put("authorization", "Basic " + auth);
        headerMap.put("Content-Type", "text/plain;charset=UTF-8");
        String messageJson = HttpUtil.post(url, JsonUtil.toJsonStr(request), headerMap);
        if (StringUtils.isNoneBlank(messageJson)) {
            List<RabbitQueueMessage> list = JsonUtil.toList(messageJson, RabbitQueueMessage.class);
            int size = list.size();
            if (!list.isEmpty()) {
                // 是否获取全部数据
                RabbitQueueMessage rabbitQueueMessage = list.get(size - 1);
                int lastMessageCount = rabbitQueueMessage.getMessageCount();
                if (lastMessageCount > 0) {
                    // 未获取全部数据
                    messageCount = messageCount + lastMessageCount * 2;
                    buildTopicPreviousMessageToGroup(topic, queueName, groupQueue, messageCount);
                    return;
                }
            }
            logger.info("Init RabbitGroupQueueMessage:[queue={}],[size={}]", groupQueue.getName(), size);
            size = size - 1;
            // 后发的其 messageCount值越小
            for (int i = 0; i <= size; i++) {
                RabbitQueueMessage message = list.get(i);
                logger.debug("Init RabbitGroupQueueMessage={},{},{}", message.getMessageCount(), message.getPayload(), message.getPayloadEncoding());
                // 推送到该消费者组队列
                rabbitTemplate.execute(new ChannelCallback<String>() {
                    @Override
                    public String doInRabbit(Channel channel) throws Exception {
                        AMQP.BasicProperties.Builder builder = new AMQP.BasicProperties.Builder();
                        // 消息持久化
                        builder.contentType("text/plain").deliveryMode(2);
                        channel.basicPublish("", queueName, builder.build(), 
                                message.getPayload().getBytes(StandardCharsets.UTF_8));
                        return null;
                    }
                });
            }
        } else {
            logger.warn("Unable to synchronize previous messages,topic={},groupQueue={}", topic, queueName);
        }
    }
    /**
     * 构建生产者的主题
     * @param topic
     * @param queueName
     */
    private void buildTopic(String topic, String queueName) {
        // 查询是否存在
        QueueInformation queueInfo = amqpAdmin.getQueueInfo(queueName);
        if (null == queueInfo) {
            // 1、创建queue
            Map<String, Object> args = new HashMap<>(1);
            // 过期时间3天
            args.put("x-message-ttl", 3 * 24 * 3600 * 1000);
            Queue queue = new Queue(queueName, true, false, false, args);
            amqpAdmin.declareQueue(queue);
            logger.info("Init RabbitQueue={}", queue.getName());
            // 2、创建exchange
            TopicExchange topicExchange = new TopicExchange(topic, true, false);
            // 延迟队列
            topicExchange.setDelayed(true);
            amqpAdmin.declareExchange(topicExchange);
            logger.info("Init RabbitExchange={}", topic);
            // 3、绑定
            amqpAdmin.declareBinding(BindingBuilder.bind(queue).to(topicExchange).with("#"));
        }
    }
    
    public void destroy() {
        HttpUtil.destroy();
    }
    
    /**
     * 消息ID
     * @return
     */
    private String getId() {
        return String.format("%s@%d@%s", hostAddress, pid, atomicLong.incrementAndGet());
    }
    
    private ImmutablePair<CorrelationData, RabbitListenableFutureCallback> getCorrelationData(SendCallback sendCallback) {
        String id = getId();
        CorrelationData correlationData = new CorrelationData(id);
        RabbitListenableFutureCallback callback = new RabbitListenableFutureCallback(Thread.currentThread(), id, sendCallback);
        correlationData.getFuture().addCallback(callback);
        return ReturnParamUtil.of(correlationData, callback);
    }
    
    @Override
    public MqMode getMqMode() {
        return MqMode.RABBIT;
    }
    
    @Override
    public boolean syncSend(String topic, Object msg) {
        String str = getJsonStr(msg);
        try {
            initTopic(topic, null);
            
            ImmutablePair<CorrelationData, RabbitListenableFutureCallback> params = getCorrelationData(null);
            CorrelationData correlationData = params.left;
            RabbitListenableFutureCallback callback = params.right;
            // 使用默认交换机（默认持久化），默认消息持久化
            rabbitTemplate.convertAndSend(topic, "#", str, correlationData);
            // 最多等3秒
            LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(3));
            if (callback.isError()) {
                logger.info("syncSend.error->topic=[{}],message=[{}]", topic, str);
                return false;
            } else {
                logger.info("syncSend.success->topic=[{}],message=[{}]", topic, str);
                return true;
            }
        } catch (Exception e) {
            logger.error("syncSend.error->topic=[" + topic + messageStr + str + "]", e);
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * 需要安装x-delayed-message延迟插件
     * @see com.fqm.framework.common.mq.template.MqTemplate#syncDelaySend(java.lang.String, java.lang.Object, int, java.util.concurrent.TimeUnit)
     *
     */
    @Override
    public boolean syncDelaySend(String topic, Object msg, int delayTime, TimeUnit timeUnit) {
        String str = getJsonStr(msg);
        try {
            initTopic(topic, null);
            
            ImmutablePair<CorrelationData, RabbitListenableFutureCallback> params = getCorrelationData(null);
            CorrelationData correlationData = params.left;
            RabbitListenableFutureCallback callback = params.right;
            // 使用默认交换机（默认持久化），默认消息持久化
            rabbitTemplate.convertAndSend(topic, "#", str, message ->{
                int time = (int)timeUnit.toMillis(delayTime);
                message.getMessageProperties().setDelay(time);
                // 标识消息是延迟任务，RabbitReturnsCallback判断如果是延迟任务则不认为是异常
                message.getMessageProperties().getHeaders().put(HEADER_DELAY, time);
                return message;
            }, correlationData);
            // 最多等3秒
            LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(3));
            if (callback.isError()) {
                logger.info("syncDelaySend.error->topic=[{}],message=[{}],delayTime=[{}],timeUnit=[{}]", topic, str, delayTime, timeUnit);
                return false;
            } else {
                logger.info("syncDelaySend.success->topic=[{}],message=[{}],delayTime=[{}],timeUnit=[{}]", topic, str, delayTime, timeUnit);
                return true;
            }
        } catch (Exception e) {
            logger.error("syncDelaySend.error->topic=[" + topic + messageStr + str + "],delayTime=[" + delayTime + "],timeUnit=[" + timeUnit + "]", e);
            e.printStackTrace();
        }
        return false;
    }
    
    @Override
    public void asyncSend(String topic, Object msg, SendCallback sendCallback) {
        String str = getJsonStr(msg);
        try {
            initTopic(topic, null);
            
            ImmutablePair<CorrelationData, RabbitListenableFutureCallback> params = getCorrelationData(sendCallback);
            CorrelationData correlationData = params.left;
            // 使用默认交换机（默认持久化），默认消息持久化
            rabbitTemplate.convertAndSend(topic, "#", str, correlationData);
            
            logger.info("asyncSend->topic=[{}],message=[{}]", topic, str);
        } catch (Exception e) {
            logger.error("asyncSend.error->topic=[" + topic + messageStr + str + "]", e);
            e.printStackTrace();
        }
    }
    
}