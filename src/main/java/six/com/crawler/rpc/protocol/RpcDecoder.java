package six.com.crawler.rpc.protocol;

import java.net.SocketAddress;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import six.com.crawler.rpc.Signals;
import six.com.crawler.utils.JavaSerializeUtils;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月23日 上午8:50:07
 */
public class RpcDecoder extends ByteToMessageDecoder implements RpcProtocol {

	final static Logger log = LoggerFactory.getLogger(RpcDecoder.class);

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf buffer, List<Object> out) throws Exception {
		if (buffer.readableBytes() >= RpcProtocol.HEAD_MIN_LENGTH) {
			buffer.markReaderIndex();
			byte msgType = buffer.readByte();
			if (msgType == RpcProtocol.HEARTBEAT) {
				log.info("received heartbeat from " + getRemoteAddress(ctx.channel()));
			} else if (msgType != RpcProtocol.REQUEST && msgType != RpcProtocol.RESPONSE) {
				buffer.resetReaderIndex();
				log.error("received illegal msg type[" + msgType + "] from" + getRemoteAddress(ctx.channel()));
				throw Signals.ILLEGAL_MSG_ERR;
			} else {
				int dataLength = buffer.readInt();
				// 如果dataLength过大，可能导致问题
				if (buffer.readableBytes() < dataLength) {
					buffer.resetReaderIndex();
					return;
				}
				if (RpcProtocol.MAX_BODY_SIZE > 0 && dataLength > RpcProtocol.MAX_BODY_SIZE) {
					throw Signals.BODY_TOO_BIG_ERR;
				}

				byte[] data = new byte[dataLength];
				buffer.readBytes(data);
				switch (msgType) {
				case RpcProtocol.HEARTBEAT: {
					break;
				}
				case RpcProtocol.REQUEST: {
					RpcRequest rpcRequest = JavaSerializeUtils.unSerialize(data, RpcRequest.class);
					out.add(rpcRequest);
					break;
				}
				case RpcProtocol.RESPONSE: {
					RpcResponse rpcResponse = JavaSerializeUtils.unSerialize(data, RpcResponse.class);
					out.add(rpcResponse);
					break;
				}
				default:
					throw Signals.ILLEGAL_MSG_ERR;
				}
			}
		}

	}

	public static String getRemoteAddress(io.netty.channel.Channel channel) {
		String address = "";
		SocketAddress remote = channel.remoteAddress();
		if (remote != null) {
			address = remote.toString();
		}
		return address;

	}

}
