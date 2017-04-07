package six.com.crawler.rpc;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import six.com.crawler.rpc.exception.RpcInvokeException;
import six.com.crawler.rpc.handler.ServerAcceptorIdleStateTrigger;
import six.com.crawler.rpc.handler.ServerHandler;
import six.com.crawler.rpc.protocol.RpcDecoder;
import six.com.crawler.rpc.protocol.RpcEncoder;
import six.com.crawler.utils.ObjectCheckUtils;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月20日 上午10:11:44
 */
public class NettyRpcServer implements RpcServer {

	final static Logger log = LoggerFactory.getLogger(NettyRpcServer.class);

	private Map<String, WrapperService> registerMap = new ConcurrentHashMap<String, WrapperService>();

	private ServerAcceptorIdleStateTrigger idleStateTrigger = new ServerAcceptorIdleStateTrigger();

	private String loaclHost;

	private int trafficPort;

	private Thread thread;

	private EventLoopGroup bossGroup;

	private EventLoopGroup workerGroup;

	private ServerBootstrap serverBootstrap;

	public NettyRpcServer(String loaclHost, int trafficPort) {
		this(loaclHost, trafficPort, 0, 0);
	}

	public NettyRpcServer(String loaclHost, int trafficPort, int bossGroupThreads, int workerGroupThreads) {
		this.loaclHost = loaclHost;
		this.trafficPort = trafficPort;
		bossGroup = new NioEventLoopGroup(bossGroupThreads < 0 ? 0 : bossGroupThreads);
		workerGroup = new NioEventLoopGroup(workerGroupThreads < 0 ? 0 : workerGroupThreads);
		serverBootstrap = new ServerBootstrap();
		serverBootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
				.childHandler(new ChannelInitializer<SocketChannel>() {
					@Override
					public void initChannel(SocketChannel ch) throws Exception {
						ch.pipeline().addLast(new IdleStateHandler(0, 0, NettyConstant.ALL_IDLE_TIME_SECONDES));
						ch.pipeline().addLast(idleStateTrigger);
						ch.pipeline().addLast(new RpcEncoder());
						ch.pipeline().addLast(new RpcDecoder());
						ch.pipeline().addLast(new ServerHandler(NettyRpcServer.this));
					}
				}).option(ChannelOption.SO_BACKLOG, 128).childOption(ChannelOption.SO_KEEPALIVE, true);
		thread = new Thread(new Runner());
		thread.setDaemon(true);
		thread.start();
	}

	public void register(Object tagetOb) {
		ObjectCheckUtils.checkNotNull(tagetOb, "tagetOb");
		Class<?> targetClz = tagetOb.getClass();
		while (null != targetClz && targetClz != Object.class) {
			Method[] allMethods = targetClz.getMethods();
			Map<String, Method> map = new HashMap<>();
			for (Method method : allMethods) {
				RpcService rpcAnnotation = method.getAnnotation(RpcService.class);
				if (null != rpcAnnotation) {
					String serviceName=((RpcService) rpcAnnotation).name();
					if(StringUtils.isBlank(serviceName)){
						serviceName=method.getName();
					}
					map.put(serviceName, method);
				}
			}
			for (String serviceName : map.keySet()) {
				registerMap.put(serviceName, paras -> {
					try {
						return map.get(serviceName).invoke(tagetOb, paras);
					} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
						throw new RpcInvokeException(e);
					}
				});
				log.info("register rpc service:" + serviceName);
			}
			targetClz = targetClz.getSuperclass();
		}
	}

	public WrapperService get(String rpcServiceName) {
		return registerMap.get(rpcServiceName);
	}

	@Override
	public void remove(String rpcServiceName) {
		registerMap.remove(rpcServiceName);
	}

	class Runner implements Runnable {
		@Override
		public void run() {
			try {
				Channel ch = serverBootstrap.bind(loaclHost, trafficPort).sync().channel();
				ch.closeFuture().sync();
			} catch (InterruptedException e) {
				log.error("netty serverBootstrap err", e);
			} finally {
				workerGroup.shutdownGracefully();
				bossGroup.shutdownGracefully();
			}
		}
	}

	public void destroy() {
		if (null != workerGroup) {
			workerGroup.shutdownGracefully();
		}
		if (null != bossGroup) {
			bossGroup.shutdownGracefully();
		}
	}

}
