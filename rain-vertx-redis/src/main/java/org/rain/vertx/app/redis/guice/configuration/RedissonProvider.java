package org.rain.vertx.app.redis.guice.configuration;

import io.netty.channel.EventLoopGroup;
import lombok.SneakyThrows;
import org.rain.vertx.app.redis.guice.enums.Model;
import org.rain.vertx.app.redis.guice.properties.ClusterServersConfig;
import org.rain.vertx.app.redis.guice.properties.RedissonProperties;
import org.rain.vertx.app.redis.guice.properties.SingleServerConfig;
import org.redisson.api.RedissonClient;
import org.redisson.config.ReadMode;
import org.redisson.config.SslProvider;
import org.redisson.config.SubscriptionMode;
import org.redisson.config.TransportMode;

import javax.inject.Named;
import javax.inject.Provider;
import java.net.URL;
import java.util.concurrent.ExecutorService;

/**
 * 文档说明:https://www.bookstack.cn/read/redisson-wiki-zh/175151
 * https://blog.csdn.net/lizz861109/article/details/109207304
 */
public final class RedissonProvider implements Provider<RedissonClient> {
    //基础 + 公共属性
    private final RedissonProperties baseServerConfig = new RedissonProperties();

    //哨兵
    private final SingleServerConfig singleServerConfig = new SingleServerConfig();

    //集群
    private final ClusterServersConfig clusterServersConfig = new ClusterServersConfig();

    @Override
    public RedissonClient get() {
        return null;
    }

    //=========================================基础 + 公共属性 S============================================================
    @com.google.inject.Inject(optional = true)
    public void setModel(@Named("redisson.model") Model model) {
        baseServerConfig.setModel(model);
    }

    @com.google.inject.Inject(optional = true)
    public void setCodec(@Named("redisson.codec") String codec) {
        baseServerConfig.setCodec(codec);
    }

    @com.google.inject.Inject(optional = true)
    public void setThreads(@Named("redisson.threads") int threads) {
        baseServerConfig.setThreads(threads);
    }

    @com.google.inject.Inject(optional = true)
    public void setNettyThreads(@Named("redisson.nettyThreads") int nettyThreads) {
        baseServerConfig.setNettyThreads(nettyThreads);
    }

    @com.google.inject.Inject(optional = true)
    public void setExecutor(@Named("redisson.executor") ExecutorService executor) {
        baseServerConfig.setExecutor(executor);
    }

    @com.google.inject.Inject(optional = true)
    public void setEventLoopGroup(@Named("redisson.eventLoopGroup") EventLoopGroup eventLoopGroup) {
        baseServerConfig.setEventLoopGroup(eventLoopGroup);
    }

    @com.google.inject.Inject(optional = true)
    public void setTransportMode(@Named("redisson.transportMode") TransportMode transportMode) {
        baseServerConfig.setTransportMode(transportMode);
    }

    @com.google.inject.Inject(optional = true)
    public void setIdleConnectionTimeout(@Named("redisson.idleConnectionTimeout") int idleConnectionTimeout) {
        baseServerConfig.setIdleConnectionTimeout(idleConnectionTimeout);
    }

    @com.google.inject.Inject(optional = true)
    public void setConnectTimeout(@Named("redisson.connectTimeout") int connectTimeout) {
        baseServerConfig.setConnectTimeout(connectTimeout);
    }

    @com.google.inject.Inject(optional = true)
    public void setTimeout(@Named("redisson.timeout") int timeout) {
        baseServerConfig.setTimeout(timeout);
    }

    @com.google.inject.Inject(optional = true)
    public void setRetryAttempts(@Named("redisson.retryAttempts") int retryAttempts) {
        baseServerConfig.setRetryAttempts(retryAttempts);
    }

    @com.google.inject.Inject(optional = true)
    public void setRetryInterval(@Named("redisson.retryInterval") int retryInterval) {
        baseServerConfig.setRetryInterval(retryInterval);
    }

    @com.google.inject.Inject(optional = true)
    public void setReconnectionTimeout(@Named("redisson.reconnectionTimeout") int reconnectionTimeout) {
        baseServerConfig.setReconnectionTimeout(reconnectionTimeout);
    }

    @com.google.inject.Inject(optional = true)
    public void setFailedAttempts(@Named("redisson.failedAttempts") int failedAttempts) {
        baseServerConfig.setFailedAttempts(failedAttempts);
    }

    @com.google.inject.Inject(optional = true)
    public void setPassword(@Named("redisson.password") String password) {
        baseServerConfig.setPassword(password);
    }

    @com.google.inject.Inject(optional = true)
    public void setSubscriptionsPerConnection(@Named("redisson.subscriptionsPerConnection") int subscriptionsPerConnection) {
        baseServerConfig.setSubscriptionsPerConnection(subscriptionsPerConnection);
    }

    @com.google.inject.Inject(optional = true)
    public void setClientName(@Named("redisson.clientName") String clientName) {
        baseServerConfig.setClientName(clientName);
    }

    //========================================= ssl S============================================================

    @com.google.inject.Inject(optional = true)
    public void setSslEnableEndpointIdentification(@Named("redisson.sslEnableEndpointIdentification") boolean sslEnableEndpointIdentification) {
        baseServerConfig.setSslEnableEndpointIdentification(sslEnableEndpointIdentification);
    }

    @com.google.inject.Inject(optional = true)
    public void setSslProvider(@Named("redisson.sslProvider") SslProvider sslProvider) {
        baseServerConfig.setSslProvider(sslProvider);
    }

    @SneakyThrows
    @com.google.inject.Inject(optional = true)
    public void setSslTruststore(@Named("redisson.sslTruststore") String sslTruststore) {
        baseServerConfig.setSslTruststore(new URL(sslTruststore));
    }

    @com.google.inject.Inject(optional = true)
    public void setSslTruststorePassword(@Named("redisson.sslTruststorePassword") String sslTruststorePassword) {
        baseServerConfig.setSslTruststorePassword(sslTruststorePassword);
    }

    @SneakyThrows
    @com.google.inject.Inject(optional = true)
    public void setSslKeystore(@Named("redisson.sslKeystore") String sslKeystore) {
        baseServerConfig.setSslKeystore(new URL(sslKeystore));
    }

    @com.google.inject.Inject(optional = true)
    public void setSslKeystorePassword(@Named("redisson.sslKeystorePassword") String sslKeystorePassword) {
        baseServerConfig.setSslKeystorePassword(sslKeystorePassword);
    }
    //========================================= ssl E============================================================

    @com.google.inject.Inject(optional = true)
    public void setPingConnectionInterval(@Named("redisson.pingConnectionInterval") int pingConnectionInterval) {
        baseServerConfig.setPingConnectionInterval(pingConnectionInterval);
    }

    @com.google.inject.Inject(optional = true)
    public void setKeepAlive(@Named("redisson.keepAlive") boolean keepAlive) {
        baseServerConfig.setKeepAlive(keepAlive);
    }

    @com.google.inject.Inject(optional = true)
    public void setTcpNoDelay(@Named("redisson.tcpNoDelay") boolean tcpNoDelay) {
        baseServerConfig.setTcpNoDelay(tcpNoDelay);
    }

    @com.google.inject.Inject(optional = true)
    public void setReferenceEnabled(@Named("redisson.referenceEnabled") boolean referenceEnabled) {
        baseServerConfig.setReferenceEnabled(referenceEnabled);
    }

    @com.google.inject.Inject(optional = true)
    public void setLockWatchdogTimeout(@Named("redisson.lockWatchdogTimeout") int lockWatchdogTimeout) {
        baseServerConfig.setLockWatchdogTimeout(lockWatchdogTimeout);
    }

    @com.google.inject.Inject(optional = true)
    public void setKeepPubSubOrder(@Named("redisson.keepPubSubOrder") boolean keepPubSubOrder) {
        baseServerConfig.setKeepPubSubOrder(keepPubSubOrder);
    }

    @com.google.inject.Inject(optional = true)
    public void setUseScriptCache(@Named("redisson.useScriptCache") boolean useScriptCache) {
        baseServerConfig.setUseScriptCache(useScriptCache);
    }

    @com.google.inject.Inject(optional = true)
    public void setMinCleanUpDelay(@Named("redisson.minCleanUpDelay") int minCleanUpDelay) {
        baseServerConfig.setMinCleanUpDelay(minCleanUpDelay);
    }

    @com.google.inject.Inject(optional = true)
    public void setMaxCleanUpDelay(@Named("redisson.maxCleanUpDelay") int maxCleanUpDelay) {
        baseServerConfig.setMaxCleanUpDelay(maxCleanUpDelay);
    }
    //=========================================基础 + 公共属性 E============================================================


    //=========================================SingleServerConfig 属性 S============================================================
    @com.google.inject.Inject(optional = true)
    public void setAddress(@Named("redisson.singleServerConfig.address") String address) {
        singleServerConfig.setAddress(address);
    }

    @com.google.inject.Inject(optional = true)
    public void setSubscriptionConnectionMinimumIdleSize(@Named("redisson.singleServerConfig.subscriptionConnectionMinimumIdleSize") int subscriptionConnectionMinimumIdleSize) {
        singleServerConfig.setSubscriptionConnectionMinimumIdleSize(subscriptionConnectionMinimumIdleSize);
    }

    @com.google.inject.Inject(optional = true)
    public void setSubscriptionConnectionPoolSize(@Named("redisson.singleServerConfig.subscriptionConnectionPoolSize") int subscriptionConnectionPoolSize) {
        singleServerConfig.setSubscriptionConnectionPoolSize(subscriptionConnectionPoolSize);
    }

    @com.google.inject.Inject(optional = true)
    public void setConnectionMinimumIdleSize(@Named("redisson.singleServerConfig.connectionMinimumIdleSize") int connectionMinimumIdleSize) {
        singleServerConfig.setConnectionMinimumIdleSize(connectionMinimumIdleSize);
    }

    @com.google.inject.Inject(optional = true)
    public void setConnectionPoolSize(@Named("redisson.singleServerConfig.connectionPoolSize") int connectionPoolSize) {
        singleServerConfig.setConnectionPoolSize(connectionPoolSize);
    }

    @com.google.inject.Inject(optional = true)
    public void setDatabase(@Named("redisson.singleServerConfig.database") int database) {
        singleServerConfig.setDatabase(database);
    }

    @com.google.inject.Inject(optional = true)
    public void setDnsMonitoringInterval(@Named("redisson.singleServerConfig.dnsMonitoringInterval") int dnsMonitoringInterval) {
        singleServerConfig.setDnsMonitoringInterval(dnsMonitoringInterval);
    }
    //=========================================SingleServerConfig 属性 E============================================================

    //=========================================clusterServersConfig 属性 S============================================================
    @com.google.inject.Inject(optional = true)
    public void setLoadBalancer(@Named("redisson.clusterServersConfig.loadBalancer") String loadBalancer) {
        clusterServersConfig.setLoadBalancer(loadBalancer);
    }

    @com.google.inject.Inject(optional = true)
    public void setSlaveConnectionMinimumIdleSize(@Named("redisson.clusterServersConfig.slaveConnectionMinimumIdleSize") int slaveConnectionMinimumIdleSize) {
        clusterServersConfig.setSlaveConnectionMinimumIdleSize(slaveConnectionMinimumIdleSize);
    }

    @com.google.inject.Inject(optional = true)
    public void setSlaveConnectionPoolSize(@Named("redisson.clusterServersConfig.slaveConnectionPoolSize") int slaveConnectionPoolSize) {
        clusterServersConfig.setSlaveConnectionPoolSize(slaveConnectionPoolSize);
    }

    @com.google.inject.Inject(optional = true)
    public void setFailedSlaveReconnectionInterval(@Named("redisson.clusterServersConfig.failedSlaveReconnectionInterval") int failedSlaveReconnectionInterval) {
        clusterServersConfig.setFailedSlaveReconnectionInterval(failedSlaveReconnectionInterval);
    }

    @com.google.inject.Inject(optional = true)
    public void setFailedSlaveCheckInterval(@Named("redisson.clusterServersConfig.failedSlaveCheckInterval") int failedSlaveCheckInterval) {
        clusterServersConfig.setFailedSlaveCheckInterval(failedSlaveCheckInterval);
    }

    @com.google.inject.Inject(optional = true)
    public void setMasterConnectionMinimumIdleSize(@Named("redisson.clusterServersConfig.masterConnectionMinimumIdleSize") int masterConnectionMinimumIdleSize) {
        clusterServersConfig.setMasterConnectionMinimumIdleSize(masterConnectionMinimumIdleSize);
    }

    @com.google.inject.Inject(optional = true)
    public void setMasterConnectionPoolSize(@Named("redisson.clusterServersConfig.masterConnectionPoolSize") int masterConnectionPoolSize) {
        clusterServersConfig.setMasterConnectionPoolSize(masterConnectionPoolSize);
    }

    @com.google.inject.Inject(optional = true)
    public void setReadMode(@Named("redisson.clusterServersConfig.readMode") ReadMode readMode) {
        clusterServersConfig.setReadMode(readMode);
    }

    @com.google.inject.Inject(optional = true)
    public void setSubscriptionMode(@Named("redisson.clusterServersConfig.subscriptionMode") SubscriptionMode subscriptionMode) {
        clusterServersConfig.setSubscriptionMode(subscriptionMode);
    }

    @com.google.inject.Inject(optional = true)
    public void setClusterSubscriptionConnectionMinimumIdleSize(@Named("redisson.clusterServersConfig.subscriptionConnectionMinimumIdleSize") int subscriptionConnectionMinimumIdleSize) {
        clusterServersConfig.setSubscriptionConnectionMinimumIdleSize(subscriptionConnectionMinimumIdleSize);
    }

    @com.google.inject.Inject(optional = true)
    public void setClusterSubscriptionConnectionPoolSize(@Named("redisson.clusterServersConfig.subscriptionConnectionPoolSize") int subscriptionConnectionPoolSize) {
        clusterServersConfig.setSubscriptionConnectionPoolSize(subscriptionConnectionPoolSize);
    }

    @com.google.inject.Inject(optional = true)
    public void setClusterDnsMonitoringInterval(@Named("redisson.clusterServersConfig.dnsMonitoringInterval") int dnsMonitoringInterval) {
        clusterServersConfig.setDnsMonitoringInterval(dnsMonitoringInterval);
    }
    //=========================================clusterServersConfig 属性 E============================================================
}
