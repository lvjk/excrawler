package six.com.rpc.remoting.netty;

import java.util.Date;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import six.com.rpc.protocol.RPCRequest;
import six.com.rpc.protocol.RPCResponse;

/**
 * @author six
 * @date 2016年6月2日 下午3:39:04
 */
public class ClientHandler extends ChannelHandlerAdapter {

	private RPCRequest RPCRequest;

	private RPCResponse RPCResponse;

	public ClientHandler(RPCRequest RPCRequest) {
		this.RPCRequest = RPCRequest;
	}

	@Override
	public void channelActive(final ChannelHandlerContext ctx) throws Exception {
		 final ChannelFuture f = ctx.writeAndFlush("a");
         f.addListener(new ClientChannelFutureListener(ctx));
	}
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) {
		ByteBuf m = (ByteBuf) msg; // (1)
		try {
			long currentTimeMillis = (m.readUnsignedInt() - 2208988800L) * 1000L;
			System.out.println(new Date(currentTimeMillis));
			ctx.close();
		} finally {
			m.release();
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		cause.printStackTrace();
		ctx.close();
	}

	public RPCRequest getRPCRequest() {
		return RPCRequest;
	}

	public void setRPCRequest(RPCRequest rPCRequest) {
		RPCRequest = rPCRequest;
	}

	public RPCResponse getRPCResponse() {
		return RPCResponse;
	}

	public void setRPCResponse(RPCResponse rPCResponse) {
		RPCResponse = rPCResponse;
	}
}
