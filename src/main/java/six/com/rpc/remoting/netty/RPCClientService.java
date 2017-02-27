package six.com.rpc.remoting.netty;

import six.com.rpc.protocol.RPCRequest;
import six.com.rpc.protocol.RPCResponse;

/**
 *@author six    
 *@date 2016年6月2日 下午4:08:38  
*/
public interface RPCClientService {
	
	/**
	 * 服务调用
	 * @param RPCRequest
	 * @return
	 */
	public RPCResponse call(RPCRequest RPCRequest);
}
