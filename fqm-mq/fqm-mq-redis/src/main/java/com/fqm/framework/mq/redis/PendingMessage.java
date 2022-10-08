package com.fqm.framework.mq.redis;

import java.time.Duration;

import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.RecordId;
/**
 * 
 * 
 * @version 
 * @author 傅泉明
 */
public class PendingMessage {

    private final RecordId id;
    private final Consumer consumer;
    private final Duration elapsedTimeSinceLastDelivery;
    private final Long totalDeliveryCount;

    public PendingMessage(RecordId id, Consumer consumer, Duration elapsedTimeSinceLastDelivery,
            long totalDeliveryCount) {

        this.id = id;
        this.consumer = consumer;
        this.elapsedTimeSinceLastDelivery = elapsedTimeSinceLastDelivery;
        this.totalDeliveryCount = totalDeliveryCount;
    }

    /**
     * @return the message id.
     */
    public RecordId getId() {
        return id;
    }

    /**
     * @return the message id as {@link String}.
     */
    public String getIdAsString() {
        return id.getValue();
    }

    /**
     * The {@link Consumer} to acknowledge the message.
     *
     * @return never {@literal null}.
     */
    public Consumer getConsumer() {
        return consumer;
    }

    /**
     * The {@literal consumer name} to acknowledge the message.
     *
     * @return never {@literal null}.
     */
    public String getConsumerName() {
        return consumer.getName();
    }

    /**
     * Get the {@literal consumer group}.
     *
     * @return never {@literal null}.
     */
    public String getGroupName() {
        return consumer.getGroup();
    }

    /**
     * Get the elapsed time (with milliseconds precision) since the messages last delivery to the {@link #getConsumer()
     * consumer}.
     *
     * @return never {@literal null}.
     */
    public Duration getElapsedTimeSinceLastDelivery() {
        return elapsedTimeSinceLastDelivery;
    }

    /**
     * Get the total number of times the messages has been delivered to the {@link #getConsumer() consumer}.
     *
     * @return never {@literal null}.
     */
    public long getTotalDeliveryCount() {
        return totalDeliveryCount;
    }

    @Override
    public String toString() {
        return "PendingMessage{" + "id=" + id + ", consumer=" + consumer + ", elapsedTimeSinceLastDeliveryMS="
                + elapsedTimeSinceLastDelivery.toMillis() + ", totalDeliveryCount=" + totalDeliveryCount + '}';
    }
}
