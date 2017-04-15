package six.com.crawler.rpc;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import six.com.crawler.rpc.exception.RpcInvokeException;
import six.com.crawler.rpc.exception.RpcNotFoundServiceException;
import six.com.crawler.rpc.exception.RpcRejectServiceException;
import six.com.crawler.rpc.exception.RpcTimeoutException;
import six.com.crawler.rpc.handler.ClientAcceptorIdleStateTrigger;
import six.com.crawler.rpc.protocol.RpcDecoder;
import six.com.crawler.rpc.protocol.RpcEncoder;
import six.com.crawler.rpc.protocol.RpcRequest;
import six.com.crawler.rpc.protocol.RpcResponse;
import six.com.crawler.rpc.protocol.RpcResponseStatus;
import six.com.crawler.rpc.protocol.RpcSerialize;
import six.com.crawler.utils.ObjectCheckUtils;
import six.com.crawler.utils.StringCheckUtils;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月20日 上午10:11:07
 * 
 *       基于netty 4.19final 实现的 简易 rpc 调用客户端
 * 
 *       <p>
 *       注意:
 *       </p>
 *       <p>
 *       所有rpc服务调用都有可能抛出一下异常:
 *       </p>
 *       <p>
 *       1.链接或者请求超时
 *       </p>
 *       <p>
 *       2.服务拒绝
 *       </p>
 *       <p>
 *       3.未发现服务
 *       </p>
 *       <p>
 *       4.执行异常
 *       </p>
 * 
 *       <p>
 *       具体参考 six.com.crawler.rpc.exception 包下的异常
 *       </p>
 * 
 * 
 */
public class NettyRpcCilent extends AbstractRemote implements RpcCilent {

	final static Logger log = LoggerFactory.getLogger(NettyRpcCilent.class);

	private ClientAcceptorIdleStateTrigger IdleStateTrigger = new ClientAcceptorIdleStateTrigger();

	private EventLoopGroup workerGroup;
	/**
	 * 用来存放执行的request WrapperFuture
	 */
	private Map<String, WrapperFuture> requestMap;
	/**
	 * 链接池
	 */
	private ConnectionPool<ClientToServerConnection> pool;

	/**
	 * 用来存放服务，
	 */
	private Map<String, Object> serviceWeakHashMap;

	// 请求超时时间  10秒
	private long callTimeout = 10000;
	// 建立连接超时时间 60秒
	private long connectionTimeout = 60000;

	private static AtomicInteger requestIndex = new AtomicInteger(0);

	public NettyRpcCilent() {
		this(0, new RpcSerialize() {
		});
	}

	public NettyRpcCilent(int workerGroupThreads, RpcSerialize rpcSerialize) {
		super(rpcSerialize);
		workerGroup = new NioEventLoopGroup(workerGroupThreads < 0 ? 0 : workerGroupThreads);
		requestMap = new ConcurrentHashMap<>();
		pool = new ConnectionPool<>();
		serviceWeakHashMap = Collections.synchronizedMap(new java.util.WeakHashMap<>());
	}


	@SuppressWarnings("unchecked")
	public <T> T lookupService(String targetHost, int targetPort, Class<?> clz, final AsyCallback asyCallback) {
		StringCheckUtils.checkStrBlank(targetHost, "targetHost");
		ObjectCheckUtils.checkIntValid(targetPort, 1, 65535, "targetPort");
		ObjectCheckUtils.checkNotNull(clz, "clz");
		String key = serviceKey(targetHost, targetPort, clz);
		Object service = serviceWeakHashMap.computeIfAbsent(key, mapkey -> {
			Enhancer enhancer = new Enhancer();
			enhancer.setSuperclass(clz);
			enhancer.setCallback(new MethodInterceptor() {
				@Override
				public Object intercept(Object targetOb, Method method, Object[] args, MethodProxy arg3)
						throws Throwable {
					String requestId = createRequestId(targetHost, targetPort, method.getName());
					RpcRequest rpcRequest = new RpcRequest();
					rpcRequest.setId(requestId);
					rpcRequest.setCommand(method.getName());
					rpcRequest.setCallHost(targetHost);
					rpcRequest.setCallPort(targetPort);
					rpcRequest.setParams(args);
					RpcResponse rpcResponse = null;
					if (null == asyCallback) {
						rpcResponse = synExecute(rpcRequest);
						return rpcResponse.getResult();
					} else {
						asyExecute(rpcRequest, asyCallback);
						return null;
					}
				}
			});
			return enhancer.create();
		});
		return (T) service;
	}

	public String createRequestId(String targetHost, int targetPort, String serviceName) {
		String requestId = "@" + targetHost + ":" + targetPort + "/" + serviceName + "/" + System.currentTimeMillis()
				+ "/" + requestIndex.incrementAndGet();
		return requestId;
	}

	/**
	 * rpc service key=目标host+:+目标端口+service class name
	 * 
	 * @param targetHost
	 * @param targetPort
	 * @param clz
	 * @return
	 */
	private String serviceKey(String targetHost, int targetPort, Class<?> clz) {
		String key = targetHost + ":" + targetPort + "/" + clz.getName();
		return key;
	}

	@Override
	public RpcResponse synExecute(RpcRequest rpcRequest) {
		WrapperFuture wrapperFuture = doExecute(rpcRequest, null);
		RpcResponse rpcResponse = wrapperFuture.getResult(callTimeout);
		if (rpcResponse.getStatus() == RpcResponseStatus.timeout) {
			throw new RpcTimeoutException("execute rpcRequest[" + rpcRequest.toString() + "] timeout");
		} else if (rpcResponse.getStatus() == RpcResponseStatus.notFoundService) {
			throw new RpcNotFoundServiceException(rpcResponse.getMsg());
		} else if (rpcResponse.getStatus() == RpcResponseStatus.reject) {
			throw new RpcRejectServiceException(rpcResponse.getMsg());
		} else if (rpcResponse.getStatus() == RpcResponseStatus.invokeErr) {
			throw new RpcInvokeException(rpcResponse.getMsg());
		} else {
			return rpcResponse;
		}
	}

	public void asyExecute(RpcRequest rpcRequest, AsyCallback callback) {
		doExecute(rpcRequest, callback);
	}

	private WrapperFuture doExecute(RpcRequest rpcRequest, AsyCallback callback) {
		ClientToServerConnection clientToServerConnection = findHealthyNettyConnection(rpcRequest);
		WrapperFuture wrapperFuture = clientToServerConnection.send(rpcRequest, callback, callTimeout);
		return wrapperFuture;
	}

	/**
	 * 获取可用的netty 链接
	 * 
	 * @param rpcRequest
	 * @return
	 */
	private ClientToServerConnection findHealthyNettyConnection(RpcRequest rpcRequest) {
		String callHost = rpcRequest.getCallHost();
		int callPort = rpcRequest.getCallPort();
		String findKey = NettyConnection.getNewConnectionKey(callHost, callPort);
		ClientToServerConnection clientToServerConnection = null;
		while (true) {
			clientToServerConnection = pool.find(findKey);
			if (null == clientToServerConnection) {
				synchronized (pool) {
					clientToServerConnection = pool.find(findKey);
					if (null == clientToServerConnection) {
						final ClientToServerConnection newClientToServerConnection = new ClientToServerConnection(
								NettyRpcCilent.this, callHost, callPort);
						Bootstrap bootstrap = new Bootstrap();
						bootstrap.group(workerGroup);
						bootstrap.channel(NioSocketChannel.class);
						bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
						bootstrap.handler(new ChannelInitializer<SocketChannel>() {
							@Override
							public void initChannel(SocketChannel ch) throws Exception {
								ch.pipeline()
										.addLast(new IdleStateHandler(0, NettyConstant.WRITER_IDLE_TIME_SECONDES, 0));
								ch.pipeline().addLast(IdleStateTrigger);
								ch.pipeline().addLast(new RpcEncoder(getRpcSerialize()));
								ch.pipeline().addLast(new RpcDecoder(getRpcSerialize()));
								ch.pipeline().addLast(newClientToServerConnection);
							}
						});
						bootstrap.connect(callHost, callPort);
						long startTime = System.currentTimeMillis();
						// 判断是否可用，如果不可用等待可用直到超时
						while (!newClientToServerConnection.available()) {
							long spendTime = System.currentTimeMillis() - startTime;
							if (spendTime > connectionTimeout) {
								newClientToServerConnection.close();
								throw new RpcTimeoutException(
										"connected " + rpcRequest.toString() + " timeout:" + spendTime);
							}
						}
						clientToServerConnection = newClientToServerConnection;
					}
				}
			}
			if (null != clientToServerConnection && clientToServerConnection.available()) {
				break;
			}
		}
		return clientToServerConnection;
	}

	public long getCallTimeout() {
		return callTimeout;
	}

	public void putWrapperFuture(String requestId, WrapperFuture wrapperFuture) {
		requestMap.put(requestId, wrapperFuture);
	}

	public WrapperFuture takeWrapperFuture(String requestId) {
		return requestMap.remove(requestId);
	}

	public void removeConnection(ClientToServerConnection connection) {
		pool.remove(connection);
	}

	public void destroy() {
		if (null != workerGroup) {
			workerGroup.shutdownGracefully();
		}
	}

}
