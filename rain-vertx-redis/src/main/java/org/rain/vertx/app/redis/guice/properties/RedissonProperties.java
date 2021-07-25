package org.rain.vertx.app.redis.guice.properties;

import io.netty.channel.EventLoopGroup;
import lombok.Data;
import lombok.experimental.Accessors;
import org.rain.vertx.app.redis.guice.enums.Model;
import org.redisson.config.SslProvider;
import org.redisson.config.TransportMode;

import java.net.URL;
import java.util.concurrent.ExecutorService;

@Accessors(chain = true)
@Data
public class RedissonProperties {
    /**
     * 默认单例模式.
     */
    private Model model = Model.SINGLE;

    /**
     * 序列化和反序列化编码方式 默认 org.redisson.codec.MsgPackJacksonCodec.
     * <p>
     * 1、org.redisson.codec.FstCodec FST 10倍于JDK序列化性能而且100%兼容的编码.
     * 2、org.redisson.codec.JsonJacksonCodec Jackson JSON 编码.
     * 3、org.redisson.codec.MsgPackJacksonCodec MsgPack 二进制的JSON编码.
     * 4、org.redisson.codec.CborJacksonCodec CBOR又一个二进制的JSON编码
     * 5、org.redisson.codec.SmileJacksonCodec Smile 另一个二进制的JSON编码
     * 6、org.redisson.codec.Kryo5Codec Kryo5 binary codec
     * 7、org.redisson.codec.LZ4Codec LZ4 compression codec.
     * 8、org.redisson.codec.SnappyCodec Netty's implementation of Snappy compression codec.
     * 9、org.redisson.codec.SnappyCodecV2 Snappy compression codec.
     */
    private String codec = "org.redisson.codec.Kryo5Codec";

    /**
     * 这个线程池数量被所有RTopic对象监听器，RRemoteService调用者和RExecutorService任务共同共享。默认值: 当前处理核数量 * 2.
     */
    private int threads = 16;

    /**
     * 这个线程池数量是在一个Redisson实例内，被其创建的所有分布式数据类型和服务，以及底层客户端所一同共享的线程池里保存的线程数量。默认值: 当前处理核数量 * 2.
     */
    private int nettyThreads = 16;

    /**
     * 单独提供一个用来执行所有RTopic对象监听器，RRemoteService调用者和RExecutorService任务的线程池（ExecutorService）实例.
     */
    private ExecutorService executor;

    /**
     * 用于特别指定一个EventLoopGroup. EventLoopGroup是用来处理所有通过Netty与Redis服务之间的连接发送和接受的消息。每一个Redisson都会在默认情况下自己创建管理一个EventLoopGroup实例。因此，如果在同一个JVM里面可能存在多个Redisson实例的情况下，采取这个配置实现多个Redisson实例共享一个EventLoopGroup的目的。
     * <p>
     * 只有io.netty.channel.epoll.EpollEventLoopGroup或io.netty.channel.nio.NioEventLoopGroup才是允许的类型
     */
    private EventLoopGroup eventLoopGroup;

    /**
     * 传输模式 默认值：TransportMode.NIO.
     * <p>
     * TransportMode.EPOLL - 需要依赖里有netty-transport-native-epoll包（Linux）
     * TransportMode.KQUEUE - 需要依赖里有 netty-transport-native-kqueue包（macOS）
     * </p>
     */
    private TransportMode transportMode = TransportMode.NIO;

    //============================================公共参数 S ======================================================
    /**
     * 连接空闲超时，单位：毫秒 默认10000.
     */
    private int idleConnectionTimeout = 10000;

    /**
     * 同任何节点建立连接时的等待超时,时间单位是毫秒 默认10000.(连接超时，单位：毫秒)
     */
    private int connectTimeout = 10000;

    /**
     * 等待节点回复命令的时间。该时间从命令发送成功时开始计时。默认3000(命令等待超时，单位：毫秒).
     */
    private int timeout = 3000;

    /**
     * 命令失败重试次数,默认值：3.
     * <p>
     * 如果尝试达到 retryAttempts（命令失败重试次数） 仍然不能将命令发送至某个指定的节点时，将抛出错误。
     * 如果尝试在此限制之内发送成功，则开始启用 timeout（命令等待超时） 计时
     * </p>
     */
    private int retryAttempts = 3;

    /**
     * 在一条命令发送失败以后，等待重试发送的时间间隔,单位：毫秒,默认值：1500.
     */
    private int retryInterval = 1500;

    /**
     * 当与某个节点的连接断开时，等待与其重新建立连接的时间间隔。时间单位是毫秒（重新连接时间间隔，单位：毫秒）默认值：3000.
     */
    private int reconnectionTimeout = 3000;

    /**
     * 执行失败最大次数,默认值：3
     * 在某个节点执行相同或不同命令时，连续 失败 failedAttempts（执行失败最大次数） 时，该节点将被从可用节点列表里清除，直到 reconnectionTimeout（重新连接时间间隔） 超时以后再次尝试.
     */
    private int failedAttempts = 3;

    /**
     * 密码.
     */
    private String password;

    /**
     * 单个连接最大订阅数量,默认值：5.
     */
    private int subscriptionsPerConnection = 8;

    /**
     * 在Redis节点里显示的客户端名称,默认null.
     */
    private String clientName = "vertx-redisson";

    //=======================================ssl S==================================================
    /**
     * 开启SSL终端识别能力,默认值：true.
     */
    private boolean sslEnableEndpointIdentification = true;

    /**
     * 确定采用哪种方式（JDK或OPENSSL）来实现SSL连接,SSL实现方式默认值：JDK.
     */
    private SslProvider sslProvider = SslProvider.JDK;

    /**
     * 指定SSL信任证书库的路径,默认值：null.
     */
    private URL sslTruststore;

    /**
     * 指定SSL信任证书库的密码,默认值：null.
     */
    private String sslTruststorePassword;

    /**
     * 指定SSL钥匙库的路径,默认值：null.
     */
    private URL sslKeystore;

    /**
     * 指定SSL钥匙库的密码,默认值：null.
     */
    private String sslKeystorePassword;
    //=======================================ssl E==================================================
    /**
     * ping连接间隔 默认值 30000,单位：毫秒.
     */
    private int pingConnectionInterval = 5000;

    /**
     * TCP是否保持连接 默认值 false.
     */
    private boolean keepAlive = true;

    /**
     * TCP是否延迟 默认值 true.
     */
    private boolean tcpNoDelay = true;

    /**
     * 用于启用 Redisson 参考功能的配置选项 默认值 true.
     */
    private boolean referenceEnabled = true;

    /**
     * 分布式锁自动过期时间，防止死锁，默认30000.
     * <p>
     * 监控锁的看门狗超时时间单位为毫秒。该参数只适用于分布式锁的加锁请求中未明确使用leaseTimeout参数的情况。
     * 如果该看门口未使用lockWatchdogTimeout去重新调整一个分布式锁的lockWatchdogTimeout超时，
     * 那么这个锁将变为失效状态。这个参数可以用来避免由Redisson客户端节点宕机或其他原因造成死锁的情况
     * </p>
     */
    private long lockWatchdogTimeout = 30000L;

    /**
     * 保持订阅发布顺序默认值：true.
     * <p>
     * 通过该参数来修改是否按订阅发布消息的接收顺序出来消息，如果选否将对消息实行并行处理，该参数只适用于订阅发布消息的情况
     * </p>
     */
    private boolean keepPubSubOrder = true;

    /**
     * 如果需要 Lua 脚本缓存，则false,默认值：false
     */
    private boolean useScriptCache = false;

    /**
     * 定义过期条目清理过程的最小延迟（以秒为单位）默认值：5.
     */
    private int minCleanUpDelay = 5;

    /**
     * 定义过期条目清理过程的最大延迟（以秒为单位）默认值：1800.
     */
    private int maxCleanUpDelay = 1800;

}
