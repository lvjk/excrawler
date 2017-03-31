package six.com.crawler.rpc.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import six.com.crawler.rpc.RpcServer;
import six.com.crawler.rpc.RpcService;
import six.com.crawler.rpc.Signal;
import six.com.crawler.rpc.Signals;
import six.com.crawler.rpc.protocol.RpcMsg;

import six.com.crawler.rpc.protocol.RpcRequest;
import six.com.crawler.rpc.protocol.RpcResponse;
import six.com.crawler.utils.ExceptionUtils;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月21日 上午10:34:59
 */
public class ServerHandler extends SimpleChannelInboundHandler<RpcMsg> {

	final static Logger log = LoggerFactory.getLogger(ServerHandler.class);

	private RpcServer rpcServer;

	public ServerHandler(RpcServer rpcServer) {
		this.rpcServer = rpcServer;
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, RpcMsg msg) throws Exception {
		RpcResponse rpcesponse = null;
		if (msg instanceof RpcRequest) {
			rpcesponse = processRequest(ctx, (RpcRequest) msg);
		} else {
			String errMsg = "ServerHandler messageReceived type not support: class=" + msg.getClass();
			rpcesponse = new RpcResponse();
			rpcesponse.setMsg(errMsg);
			log.error(errMsg);
		}
		ctx.writeAndFlush(rpcesponse);
	}

	private RpcResponse processRequest(ChannelHandlerContext ctx, RpcRequest rpcRequest) {
		RpcService rpcService = rpcServer.get(rpcRequest.getCommand());
		RpcResponse rpcResponse = new RpcResponse();
		rpcResponse.setId(rpcRequest.getId());
		if (null != rpcService) {
			log.info("server received coommand[" + rpcRequest.getCommand() + "] from " + rpcRequest.getOriginHost());
			try {
				Object result = rpcService.execute(rpcRequest.getParams());
				rpcResponse.setSucceed(true);
				rpcResponse.setResult(result);
			} catch (Exception e) {
				String errMsg = ExceptionUtils.getExceptionMsg(e);
				rpcResponse.setMsg(errMsg);
			}
		} else {
			rpcResponse.setMsg("server did not find the rpcRequest:" + rpcRequest.toString());
		}
		return rpcResponse;
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		Channel ch = ctx.channel();
		String address = ctx.channel().remoteAddress().toString();
		if (cause instanceof Signal) {
			Signal signalErr = (Signal) cause;
			if (signalErr.getType() == Signals.MSG_ILLEGAL_TYPE) {
				RpcResponse response = new RpcResponse();
				response.setMsg("the msg is illegal");
				ctx.writeAndFlush(response);
				ctx.close();
				log.error("the msg is illegal from channel[" + address + "]", cause);
			} else if (signalErr.getType() == Signals.MSG_TOO_BIG) {
				RpcResponse response = new RpcResponse();
				response.setMsg("the msg is too big");
				ctx.writeAndFlush(response);
				ctx.close();
				log.error("the msg is too big from channel[" + address + "]", cause);
			} else if (signalErr.getType() == Signals.READER_IDLE) {
				ch.close();
				log.error("the channel[" + address + "] is reader idle and will be close", cause);
			}
		} else {
			ch.close();
			log.error("unknow err and close channel[" + address + "]", cause);
		}
	}

}
