package six.com.crawler.rpc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import six.com.crawler.rpc.protocol.RpcRequest;
import six.com.crawler.rpc.protocol.RpcResponse;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月20日 下午10:44:16
 */
public class WrapperFuture {

	final static Logger log = LoggerFactory.getLogger(WrapperFuture.class);

	private volatile FutureState state = FutureState.DOING;

	private volatile long sendTime;

	private volatile long receiveTime;

	private volatile RpcRequest rpcRequest;

	private volatile RpcResponse rpcResponse;

	private long createTime = System.currentTimeMillis();

	Object lock = new Object();

	WrapperFuture(RpcRequest rpcRequest) {
		this.rpcRequest = rpcRequest;
	}

	public long getSendTime() {
		return sendTime;
	}

	public void setSendTime(long sendTime) {
		this.sendTime = sendTime;
	}

	public long getReceiveTime() {
		return receiveTime;
	}

	public RpcRequest getRPCRequest() {
		return rpcRequest;
	}

	public void onSuccess(RpcResponse response, long receiveTime) {
		this.rpcResponse = response;
		this.receiveTime = receiveTime;
		done();
	}

	public void onFailure(RpcResponse response, long receiveTime) {
		this.rpcResponse = response;
		this.receiveTime = receiveTime;
		done();
	}

	private boolean done() {
		synchronized (lock) {
			if (!isDoing()) {
				return false;
			}

			state = FutureState.DONE;
			lock.notifyAll();
		}
		return true;
	}

	private boolean isDoing() {
		return state.isDoingState();
	}

	public RpcResponse getResult(long timeout) {
		synchronized (lock) {
			if (!isDoing()) {
				return rpcResponse;
			}
			long waitTime = timeout - (System.currentTimeMillis() - createTime);
			if (waitTime > 0) {
				for (;;) {
					try {
						lock.wait(waitTime);
					} catch (InterruptedException e) {
					}

					if (!isDoing()) {
						break;
					} else {
						waitTime = timeout - (System.currentTimeMillis() - createTime);
						if (waitTime <= 0) {
							break;
						}
					}
				}
			}
			if (null == rpcResponse) {
				throw new RuntimeException("get rpcRequest[" + rpcRequest.toString() + "]'s response timeout");
			}
			return rpcResponse;
		}

	}
}
