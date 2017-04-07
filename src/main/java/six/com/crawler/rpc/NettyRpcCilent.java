package six.com.crawler.rpc;

import java.util.Map;

import java.util.concurrent.ConcurrentHashMap;
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
import six.com.crawler.rpc.service.WrapperClientService;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月20日 上午10:11:07
 */
public class NettyRpcCilent implements RpcCilent {

	final static Logger log = LoggerFactory.getLogger(NettyRpcCilent.class);

	private ClientAcceptorIdleStateTrigger IdleStateTrigger = new ClientAcceptorIdleStateTrigger();

	private EventLoopGroup workerGroup;

	private Map<String, WrapperFuture> requestMap;

	private ConnectionPool<ClientToServerConnection> pool;

	// 请求超时时间
	private long callTimeout = 6000;
	// 建立连接超时时间
	private long connectionTimeout = 300000;

	public NettyRpcCilent() {
		this(0);
	}

	public NettyRpcCilent(int workerGroupThreads) {
		workerGroup = new NioEventLoopGroup(workerGroupThreads < 0 ? 0 : workerGroupThreads);
		requestMap = new ConcurrentHashMap<>();
		pool = new ConnectionPool<>();
	}

	@SuppressWarnings("unchecked")
	public <T> T lookupService(String targetHost, int targetPort, Class<?> clz, AsyCallback callback) {
		WrapperClientService wrapperClientService = new WrapperClientService(this, targetHost, targetPort, callback);
		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(clz);
		enhancer.setCallback(wrapperClientService);
		return (T) enhancer.create();
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
		try {
			WrapperFuture wrapperFuture = clientToServerConnection.send(rpcRequest, callback, callTimeout);
			return wrapperFuture;
		} finally {
			pool.returnConn(clientToServerConnection);
		}
	}

	private ClientToServerConnection findHealthyNettyConnection(RpcRequest rpcRequest) {
		String callHost = rpcRequest.getCallHost();
		int callPort = rpcRequest.getCallPort();
		String findKey = NettyConnection.getNewConnectionKey(callHost, callPort);
		ClientToServerConnection clientToServerConnection = null;
		while (true) {
			clientToServerConnection = pool.find(findKey);
			if (null == clientToServerConnection) {
				synchronized (pool) {
					final ClientToServerConnection newClientToServerConnection = new ClientToServerConnection(
							NettyRpcCilent.this, callHost, callPort);
					Bootstrap bootstrap = new Bootstrap();
					bootstrap.group(workerGroup);
					bootstrap.channel(NioSocketChannel.class);
					bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
					bootstrap.handler(new ChannelInitializer<SocketChannel>() {
						@Override
						public void initChannel(SocketChannel ch) throws Exception {
							ch.pipeline().addLast(new IdleStateHandler(0, NettyConstant.WRITER_IDLE_TIME_SECONDES, 0));
							ch.pipeline().addLast(IdleStateTrigger);
							ch.pipeline().addLast(new RpcEncoder());
							ch.pipeline().addLast(new RpcDecoder());
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
							throw new RuntimeException("connected " + rpcRequest.toString() + " timeout:" + spendTime);
						}
					}
					clientToServerConnection = newClientToServerConnection;
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
