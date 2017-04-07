package six.com.crawler.rpc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import six.com.crawler.rpc.protocol.RpcMsg;
import six.com.crawler.rpc.protocol.RpcRequest;
import six.com.crawler.rpc.protocol.RpcResponse;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月20日 下午11:05:56
 */

public class ClientToServerConnection extends NettyConnection {

	final static Logger log = LoggerFactory.getLogger(ClientToServerConnection.class);

	private RpcCilent rpcCilent;

	protected ClientToServerConnection(RpcCilent rpcCilent, String host, int port) {
		super(host, port);
		this.rpcCilent = rpcCilent;
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, RpcMsg msg) throws Exception {
		if (msg instanceof RpcResponse) {
			RpcResponse rpcResponse = (RpcResponse) msg;
			WrapperFuture wrapperRPCRequest = rpcCilent.takeWrapperFuture(rpcResponse.getId());
			if (null != wrapperRPCRequest) {
				wrapperRPCRequest.onComplete(rpcResponse, System.currentTimeMillis());
				log.info("client received rpcResponse from rpcRequest[" + wrapperRPCRequest.getRPCRequest().toString()
						+ "]");
			}
		} else {
			log.error("ClientHandler messageReceived type not support: class=" + msg.getClass());
		}
	}

	/**
	 * 发送 rpcRequest
	 * 
	 * @param rpcRequest
	 * @param callback
	 * @param callTimeout
	 * @return
	 */
	public WrapperFuture send(RpcRequest rpcRequest, AsyCallback callback, long timeout) {
		WrapperFuture wrapperFuture = new WrapperFuture(rpcRequest, callback);
		wrapperFuture.setSendTime(System.currentTimeMillis());
		rpcCilent.putWrapperFuture(rpcRequest.getId(), wrapperFuture);
		ChannelFuture channelFuture = super.writeAndFlush(rpcRequest);
		boolean result = channelFuture.awaitUninterruptibly(timeout);
		if (result) {
			channelFuture.addListener(new GenericFutureListener<Future<? super Void>>() {
				@Override
				public void operationComplete(Future<? super Void> future) throws Exception {
					if (future.isSuccess() || future.isDone()) {
						log.info("send rpcRequest successed");
					} else {
						log.info("send rpcRequest failed");
					}
				}
			});
		}
		return wrapperFuture;
	}

	@Override
	protected void doConnect() {
		this.rpcCilent.removeConnection(this);
	}

}
