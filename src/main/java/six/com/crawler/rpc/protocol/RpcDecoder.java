package six.com.crawler.rpc.protocol;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import six.com.crawler.rpc.Signal;
import six.com.crawler.rpc.Signals;
import six.com.crawler.utils.JavaSerializeUtils;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月20日 下午3:21:38
 */
public class RpcDecoder extends ReplayingDecoder<RpcDecoder.State> implements RpcProtocol {
	/**
	 * 读取消息状态位
	 * @author six
	 * @email  359852326@qq.com
	 */
	enum State {
		HEADER_MSGTYPE, HEADER_BODY_LENGTH, BODY
	}
	
	public RpcDecoder() {
		super(State.HEADER_MSGTYPE);
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		byte msgType = RpcProtocol.UNKNOW;
		int dataLength = 0;
		try {
			switch (state()) {
			case HEADER_MSGTYPE:
				msgType = in.readByte();
				checkMsgType(msgType);
				checkpoint(State.HEADER_BODY_LENGTH);
				// 判断是否是心跳数据包，如果是只需要读取数据类型即可
				if (RpcProtocol.HEARTBEAT == msgType) {
					return;
				}
			case HEADER_BODY_LENGTH:
				dataLength = in.readInt();
				checkBodyLength(dataLength);
				checkpoint(State.BODY);
			case BODY:
				switch (msgType) {
				case RpcProtocol.REQUEST: {
					byte[] body = new byte[dataLength];
					in.readBytes(body);
					RpcRequest rpcRequest = JavaSerializeUtils.unSerialize(body, RpcRequest.class);
					out.add(rpcRequest);
					break;
				}
				case RpcProtocol.RESPONSE: {
					byte[] body = new byte[dataLength]; // 传输正常
					in.readBytes(body);
					RpcResponse rpcResponse = JavaSerializeUtils.unSerialize(body, RpcResponse.class);
					out.add(rpcResponse);
					break;
				}
				default:
					throw Signals.ILLEGAL_MSG_ERR;
				}
			}
		} finally {
			checkpoint(State.HEADER_MSGTYPE);
		}
	}

	private static void checkMsgType(byte magic) throws Signal {
		if (magic != RpcProtocol.REQUEST && magic != RpcProtocol.RESPONSE && magic != RpcProtocol.HEARTBEAT) {
			throw Signals.ILLEGAL_MSG_ERR;
		}
	}

	private static int checkBodyLength(int size) throws Signal {
		if (size > RpcProtocol.MAX_BODY_SIZE) {
			throw Signals.BODY_TOO_BIG_ERR;
		}
		return size;
	}

}
