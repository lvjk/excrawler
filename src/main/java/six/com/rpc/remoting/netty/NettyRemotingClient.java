package six.com.rpc.remoting.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import six.com.rpc.protocol.RPCRequest;
import six.com.rpc.protocol.RPCResponse;

/**
 * @author six
 * @date 2016年6月2日 下午4:08:51
 */
public class NettyRemotingClient extends NettyRemotingAbstract implements RPCClientService {

	@Override
	public RPCResponse call(RPCRequest RPCRequest) {
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		ClientHandler handler = new ClientHandler(RPCRequest);
		try {
			Bootstrap b = new Bootstrap();
			b.group(workerGroup);
			b.channel(NioSocketChannel.class);
			b.option(ChannelOption.SO_KEEPALIVE, true);
			b.handler(new ChannelInitializer<SocketChannel>() {
				@Override
				public void initChannel(SocketChannel ch) throws Exception {
					ch.pipeline().addLast(handler);
				}
			});
			// Start the client.
			ChannelFuture f;
			try {
				f = b.connect(RPCRequest.getRequestIP(), RPCRequest.getRequestPort()).sync();
				f.channel().closeFuture().sync();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} //
				// Wait until the connection is closed.
		} finally {
			workerGroup.shutdownGracefully();
		}
		return handler.getRPCResponse();
	}

}
