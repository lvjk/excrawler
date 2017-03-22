package six.com.crawler.rpc.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelHandlerContext;
import six.com.crawler.rpc.RpcCilent;
import six.com.crawler.rpc.WrapperFuture;
import six.com.crawler.rpc.protocol.RpcMsg;
import six.com.crawler.rpc.protocol.RpcResponse;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月20日 下午11:05:56
 */

public class ClientToServerConnection extends NettyConnection {

	final static Logger log = LoggerFactory.getLogger(ClientToServerConnection.class);
	private RpcCilent rpcCilent;

	public ClientToServerConnection(RpcCilent rpcCilent, String host, int port) {
		super(host, port);
		this.rpcCilent = rpcCilent;
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, RpcMsg msg) throws Exception {
		if (msg instanceof RpcResponse) {
			processResponse(ctx, (RpcResponse) msg);
		} else {
			log.error("ClientHandler messageReceived type not support: class=" + msg.getClass());
			throw new RuntimeException("ClientHandler messageReceived type not support: class=" + msg.getClass());
		}
	}

	private void processResponse(ChannelHandlerContext ctx, RpcResponse rpcResponse) {
		WrapperFuture wrapperRPCRequest = rpcCilent.getRequest(rpcResponse.getId());
		if (null != wrapperRPCRequest) {
			if (rpcResponse.getException() != null) {
				wrapperRPCRequest.onFailure(rpcResponse, System.currentTimeMillis());
			} else {
				wrapperRPCRequest.onSuccess(rpcResponse, System.currentTimeMillis());
			}
			log.info("client received rpcResponse from rpcRequest[" + wrapperRPCRequest.getRPCRequest().toString()
					+ "]");
		}
	}

	@Override
	protected void doConnect() {
		this.rpcCilent.removeConnection(this);
	}

}
