package org.rain.vertx.app.redis.guice.enums;

public enum Model {
    //哨兵
    SENTINEL,
    //主从
    MASTER_SLAVE,
    //单例
    SINGLE,
    //集群
    CLUSTER,
    //云托管模式
    REPLICATED
}
