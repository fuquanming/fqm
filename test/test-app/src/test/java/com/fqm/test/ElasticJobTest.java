//package com.fqm.test;
//
//import com.zaxxer.hikari.HikariConfig;
//import com.zaxxer.hikari.HikariDataSource;
//
//import javax.sql.DataSource;
//
//import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
//import org.apache.shardingsphere.elasticjob.api.ShardingContext;
//import org.apache.shardingsphere.elasticjob.lite.api.bootstrap.impl.ScheduleJobBootstrap;
//import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
//import org.apache.shardingsphere.elasticjob.reg.zookeeper.ZookeeperConfiguration;
//import org.apache.shardingsphere.elasticjob.reg.zookeeper.ZookeeperRegistryCenter;
//import org.apache.shardingsphere.elasticjob.simple.job.SimpleJob;
//import org.apache.shardingsphere.elasticjob.tracing.api.TracingConfiguration;
//
//public class ElasticJobTest {
//
//    public static void main(String[] args) {
//        CoordinatorRegistryCenter coordinatorRegistryCenter = createRegistryCenter();
//        DataSource dataSource = getDataSource();
//        TracingConfiguration tracingConfig = new TracingConfiguration<>("RDB", dataSource);
//
//        new ScheduleJobBootstrap(coordinatorRegistryCenter, new SimpleJob() {
//            @Override
//            public void execute(ShardingContext shardingContext) {
//                System.out.println("1212");
//            }
//        }, createJobConfiguration("job1", tracingConfig)).schedule();
//    }
//
//    private static CoordinatorRegistryCenter createRegistryCenter() {
//        ZookeeperConfiguration zookeeperConfiguration = new ZookeeperConfiguration("172.16.50.105:2181", "elasticjob-1");
//        zookeeperConfiguration.setMaxSleepTimeMilliseconds(1000 * 30);
//        zookeeperConfiguration.setConnectionTimeoutMilliseconds(1000 * 30);
//        CoordinatorRegistryCenter coordinatorRegistryCenter = new ZookeeperRegistryCenter(zookeeperConfiguration);
//        coordinatorRegistryCenter.init();
//        return coordinatorRegistryCenter;
//    }
//
//    private static JobConfiguration createJobConfiguration(String jobName, TracingConfiguration tracingConfig) {
//        JobConfiguration jobConfiguration = JobConfiguration.newBuilder(jobName, 1)
//                .cron("0/5 * * * * ?").overwrite(false).build();
//
//        //配置事件追踪，即记录任务执行日志
//        jobConfiguration.getExtraConfigurations().add(tracingConfig);
//        return jobConfiguration;
//    }
//
//    //这里使用 Hikari 连接池，使用 Druid 有时会报错
//    private static DataSource getDataSource() {
//        HikariConfig hikariConfig = new HikariConfig();
//        hikariConfig.setDriverClassName("com.mysql.cj.jdbc.Driver");
//        hikariConfig.setJdbcUrl("jdbc:mysql://172.16.50.102:13306/ejob?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8");
//        hikariConfig.setUsername("root");
//        hikariConfig.setPassword("TbEmQetvrMrlNBZO");
//        hikariConfig.setMinimumIdle(2);
//        hikariConfig.setMaximumPoolSize(5);
//        hikariConfig.setConnectionTestQuery("select 1");
//        HikariDataSource hikariDataSource = new HikariDataSource(hikariConfig);
//        return hikariDataSource;
//    }
//    
//}
