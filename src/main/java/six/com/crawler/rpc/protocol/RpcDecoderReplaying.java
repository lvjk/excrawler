package six.com.crawler.rpc.protocol;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class RpcDecoderReplaying extends ReplayingDecoder<RpcDecoderReplaying.State> implements RpcProtocol {

	final static Logger log = LoggerFactory.getLogger(RpcDecoderReplaying.class);

	/**
	 * 读取消息状态位
	 * 
	 * @author six
	 * @email 359852326@qq.com
	 */
	enum State {
		HEADER_MSGTYPE, HEADER_BODY_LENGTH, BODY
	}

	public RpcDecoderReplaying() {
		super(State.HEADER_MSGTYPE);
		setCumulator(COMPOSITE_CUMULATOR);
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		byte msgType = RpcProtocol.HEARTBEAT;
		int dataLength = 0;
		switch (state()) {
		case HEADER_MSGTYPE:
			msgType = in.readByte();
			checkMsgType(msgType);
			checkpoint(State.HEADER_BODY_LENGTH);
		case HEADER_BODY_LENGTH:
			dataLength = in.readInt();
			checkBodyLength(dataLength);
			checkpoint(State.BODY);
		case BODY:
			switch (msgType) {
			// 判断是否是心跳数据包，如果是只需要读取数据类型即可
			case RpcProtocol.HEARTBEAT: {
				break;
			}
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
			checkpoint(State.HEADER_MSGTYPE);
		}

	}

	private static void checkMsgType(byte msgType) throws Signal {
		if (msgType != RpcProtocol.REQUEST && msgType != RpcProtocol.RESPONSE && msgType != RpcProtocol.HEARTBEAT) {
			log.error("illegal msg type:" + msgType);
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
