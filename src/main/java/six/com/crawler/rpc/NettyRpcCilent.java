package six.com.crawler.rpc;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import six.com.crawler.rpc.handler.ClientAcceptorIdleStateTrigger;
import six.com.crawler.rpc.handler.ClientToServerConnection;
import six.com.crawler.rpc.handler.NettyConnection;
import six.com.crawler.rpc.protocol.RpcDecoder;
import six.com.crawler.rpc.protocol.RpcEncoder;
import six.com.crawler.rpc.protocol.RpcRequest;
import six.com.crawler.rpc.protocol.RpcResponse;

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

	@Override
	public RpcResponse execute(RpcRequest rpcRequest) {
		// 获取一个可用 netty链接
		ClientToServerConnection clientToServerConnection = findHealthyNettyConnection(rpcRequest);
		WrapperFuture wrapperFuture = new WrapperFuture(rpcRequest);
		requestMap.put(rpcRequest.getId(), wrapperFuture);
		wrapperFuture.setSendTime(System.currentTimeMillis());
		ChannelFuture channelFuture = clientToServerConnection.writeAndFlush(rpcRequest);
		try {
			boolean result = channelFuture.awaitUninterruptibly(callTimeout);
			if (result) {
				channelFuture.addListener(new GenericFutureListener<Future<? super Void>>() {
					@Override
					public void operationComplete(Future<? super Void> future) throws Exception {
						if (future.isSuccess() || future.isDone()) {
							log.info("call rpcRequest successed");
						} else {
							log.info("call rpcRequest failed");
						}
					}
				});
			}
			RpcResponse rpcResponse = wrapperFuture.getResult(callTimeout);
			if (null == rpcResponse) {
				throw new RuntimeException("call rpcRequest's rpcResponse is null");
			} else if (!StringUtils.equals(rpcResponse.getId(), rpcRequest.getId())) {
				throw new RuntimeException("the rpcRequest's id don't match rpcResponse's id");
			} else if (null != rpcResponse.getException()) {
				throw new RuntimeException(rpcResponse.getException());
			} else {
				return rpcResponse;
			}
		} finally {
			requestMap.remove(rpcRequest.getId());
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

	public WrapperFuture getRequest(String requestId) {
		return requestMap.get(requestId);
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
