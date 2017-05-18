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

	private volatile long createTime;

	private volatile long sendTime;

	private volatile long receiveTime;

	private volatile RpcRequest rpcRequest;

	private volatile RpcResponse rpcResponse;

	private AsyCallback asyCallback;

	private long timeout;

	public WrapperFuture(RpcRequest rpcRequest, AsyCallback asyCallback, long timeout) {
		this.createTime = System.currentTimeMillis();
		this.rpcRequest = rpcRequest;
		this.asyCallback = asyCallback;
		this.timeout = timeout;
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

	public long getTimeout() {
		return timeout;
	}

	public boolean isDone() {
		return null != rpcResponse;
	}

	public synchronized void onComplete(RpcResponse response, long receiveTime) {
		this.rpcResponse = response;
		this.receiveTime = receiveTime;
		notifyAll();
		if (null != asyCallback) {
			asyCallback.execute(response.getResult());
		}
	}

	public RpcResponse getResult() {
		if (null == rpcResponse) {
			synchronized (this) {
				if (null == rpcResponse) {
					long waitTime = timeout - (System.currentTimeMillis() - createTime);
					if (waitTime > 0) {
						while (true) {
							try {
								wait(waitTime);
							} catch (InterruptedException e) {
							}
							if (null != rpcResponse) {
								break;
							} else {
								waitTime = timeout - (System.currentTimeMillis() - createTime);
								if (waitTime <= 0) {
									break;
								}
							}
						}
					} else {
						try {
							wait();
						} catch (InterruptedException e) {
						}
					}
				}
			}
		}
		return rpcResponse;
	}
}
