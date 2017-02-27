package six.com.rpc.remoting.netty;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import six.com.rpc.remoting.RemotingServer;

/**
 * @author six
 * @date 2016年6月2日 下午3:56:35
 */

public class NettyRemotingServer extends NettyRemotingAbstract implements RemotingServer {

	final static Logger LOG = LoggerFactory.getLogger(NettyRemotingServer.class);

	private Map<String, Object> serviceRegisterMap = new ConcurrentHashMap<String, Object>();

	private Thread thread;

	private EventLoopGroup bossGroup;

	private EventLoopGroup workerGroup;

	private ServerBootstrap serverBootstrap;

	@Override
	public void register(String serviceName, Object service) {
		serviceRegisterMap.put(serviceName, service);
	}

	@Override
	public void remove(String serviceName) {
		serviceRegisterMap.remove(serviceName);
	}

	public void interiorInit() {
		thread = new Thread(new Runner());
		thread.setDaemon(true);
		thread.start();

	}

	class Runner implements Runnable {
		@Override
		public void run() {
			bossGroup = new NioEventLoopGroup(); //
			workerGroup = new NioEventLoopGroup();
			try {
				serverBootstrap = new ServerBootstrap(); //
				serverBootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class) //
						.childHandler(new ChannelInitializer<SocketChannel>() { //
							@Override
							public void initChannel(SocketChannel ch) throws Exception {
								ch.pipeline().addLast(new ServerHandler());
							}
						}).option(ChannelOption.SO_BACKLOG, 128) //
						.childOption(ChannelOption.SO_KEEPALIVE, true); //
				// Bind and start to accept incoming connections.
				ChannelFuture f;
				int trafficPort = getConfigure().getConfig("node.trafficPort.port", 9081);
				try {
					f = serverBootstrap.bind(trafficPort).sync();
					f.channel().closeFuture().sync();
				} catch (InterruptedException e) {
					LOG.error("netty serverBootstrap err", e);
				} //
					// Wait until the server socket is closed.
					// In this example, this does not happen, but you can do
					// that to
					// gracefully
					// shut down your server.
			} finally {
				workerGroup.shutdownGracefully();
				bossGroup.shutdownGracefully();
			}
		}
	}
}
