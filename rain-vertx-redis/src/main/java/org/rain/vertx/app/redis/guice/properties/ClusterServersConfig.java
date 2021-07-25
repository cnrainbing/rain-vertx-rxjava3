package org.rain.vertx.app.redis.guice.properties;

import lombok.Data;
import lombok.experimental.Accessors;
import org.redisson.config.ReadMode;
import org.redisson.config.SubscriptionMode;

import java.util.List;

@Accessors(chain = true)
@Data
public class ClusterServersConfig {
    /**
     * loadBalancer 负载均衡算法类的选择.
     * <p>
     * 在多Redis服务节点的环境里，可以选用以下几种负载均衡方式选择一个节点：
     * org.redisson.connection.balancer.WeightedRoundRobinBalancer - 权重轮询调度算法
     * org.redisson.connection.balancer.RoundRobinLoadBalancer - 轮询调度算法(默认值)
     * org.redisson.connection.balancer.RandomLoadBalancer - 随机调度算法
     * </p>
     */
    private String loadBalancer = "org.redisson.connection.balancer.RoundRobinLoadBalancer";

    /**
     * 从节点最小空闲连接数 默认值32.
     */
    private int slaveConnectionMinimumIdleSize = 32;

    /**
     * 从节点连接池大小 默认64.
     */
    private int slaveConnectionPoolSize = 64;

    /**
     * 失败从节点重连间隔时间.
     */
    private int failedSlaveReconnectionInterval = 3000;

    /**
     * 失败从节点校验间隔时间.
     */
    private int failedSlaveCheckInterval = 180000;

    /**
     * 主节点最小空闲连接数 默认32.
     */
    private int masterConnectionMinimumIdleSize = 32;

    /**
     * 主节点连接池大小 默认64.
     */
    private int masterConnectionPoolSize = 64;

    /**
     * 只在从服务器读取.
     */
    private ReadMode readMode = ReadMode.SLAVE;

    /**
     * 订阅操作的负载均衡模式.
     */
    private SubscriptionMode subscriptionMode = SubscriptionMode.SLAVE;

    /**
     * 从节点发布和订阅连接的最小空闲连接数（长连接）默认值：1.
     */
    private int subscriptionConnectionMinimumIdleSize = 1;

    /**
     * 从节点发布和订阅连接池大小,多从节点的环境里,每个从服务节点里用于发布和订阅连接的连接池最大容量,连接池的连接数量自动弹性伸缩.
     */
    private int subscriptionConnectionPoolSize = 50;

    /**
     * 监测DNS的变化情况的时间间隔(DNS监控间隔，单位：毫秒)默认值：5000.
     */
    private long dnsMonitoringInterval = 5000L;

    /**
     * redis 集群地址.
     */
    private List<String> nodeAddresses = List.of();

    /**
     * 哨兵模式 检查节点是否少于一个,默认检查true
     */
    private boolean checkSentinelsList = true;

    /**
     * 集群,哨兵,云托管.
     * 对Redis集群节点状态扫描的时间间隔。单位是毫秒。默认1000.
     */
    private int scanInterval = 2000;

    /**
     * 哨兵模式,云托管,主从.
     */
    private int database = 0;

    /**
     * 哨兵模式.
     */
    private String masterName;
}
