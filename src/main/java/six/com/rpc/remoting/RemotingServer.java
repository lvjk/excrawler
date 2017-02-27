package six.com.rpc.remoting;
/**
 *@author six    
 *@date 2016年6月2日 下午3:56:22  
*/
public interface RemotingServer {

	/**
	 * 服务注册
	 * @param service
	 */
	public void register(String serviceName,Object service);
	
	/**
	 * 服务移除
	 * @param service
	 */
	public void remove(String serviceName);
}
