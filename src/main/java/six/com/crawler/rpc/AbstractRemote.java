package six.com.crawler.rpc;

import six.com.crawler.rpc.protocol.RpcSerialize;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年4月10日 上午11:20:06
 */
public class AbstractRemote implements Remote {

	private RpcSerialize rpcSerialize;

	public AbstractRemote(RpcSerialize rpcSerialize) {
		if (null == rpcSerialize) {
			throw new NullPointerException("the remote's rpcSerialize must be not null");
		}
		this.rpcSerialize = rpcSerialize;
	}

	@Override
	public RpcSerialize getRpcSerialize() {
		return rpcSerialize;
	}
	
	protected String getServiceName(String className,String methodName){
		return className+"."+methodName;
	}

}
