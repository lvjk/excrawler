package six.com.rpc.remoting.netty;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author six
 * @date 2016年6月2日 下午4:48:41
 */
public class ServerChannelFutureListener implements ChannelFutureListener {

	private ChannelHandlerContext ctx;

	public ServerChannelFutureListener(ChannelHandlerContext ctx) {
		this.ctx = ctx;
	}

	@Override
	public void operationComplete(ChannelFuture future) throws Exception {
		ctx.close();
	}

}
