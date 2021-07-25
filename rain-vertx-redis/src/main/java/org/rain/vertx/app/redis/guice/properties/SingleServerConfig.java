package org.rain.vertx.app.redis.guice.properties;

import lombok.Data;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Data
public class SingleServerConfig {
    /**
     * redis 服务器地址(节点地址).
     */
    private String address;

    /**
     * 从节点发布和订阅连接的最小空闲连接数,默认值：1.
     */
    private int subscriptionConnectionMinimumIdleSize = 4;

    /**
     * 从节点发布和订阅连接池大小,默认值：50.
     */
    private int subscriptionConnectionPoolSize = 64;

    /**
     * 最小空闲连接数,默认值：24.
     */
    private int connectionMinimumIdleSize = 32;

    /**
     * 连接池大小.
     */
    private int connectionPoolSize = 64;

    /**
     * 数据库编号,默认值：0.
     */
    private int database = 0;

    /**
     * DNS监测时间间隔，单位：毫,默认值：5000.
     */
    private long dnsMonitoringInterval = 5000L;
}
