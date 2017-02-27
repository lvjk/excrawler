package six.com.rpc.remoting.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author six
 * @date 2016年6月2日 下午3:35:35
 */
public class ServerHandler extends ChannelHandlerAdapter {
	
	@Override
	public void channelActive(final ChannelHandlerContext ctx) { // (1)
		final ByteBuf time = ctx.alloc().buffer(4); // (2)
		time.writeInt((int) (System.currentTimeMillis() / 1000L + 2208988800L));
		final ChannelFuture f = ctx.writeAndFlush(time); // (3)
		f.addListener(new ServerChannelFutureListener(ctx)); // (4)
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		cause.printStackTrace();
		ctx.close();
	}
}
