package six.com.crawler.rpc.protocol;

import java.io.Serializable;
import java.util.Map;

/**
 * @author six
 * @date 2016年6月2日 下午4:14:39 rpc 请求
 */
public class RpcRequest extends RpcMsg implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1071881684426113946L;

	// 来源host
	private String originHost;
	// 呼叫host
	private String callHost;
	// 呼叫host端口
	private int callPort;
	// 呼叫命令
	private String command;
	// 呼叫参数
	private Map<String,Object> params;
	
	public RpcRequest() {
		super(RpcProtocol.REQUEST);
	}
	
	public String getOriginHost() {
		return originHost;
	}

	public void setOriginHost(String originHost) {
		this.originHost = originHost;
	}

	public String getCallHost() {
		return callHost;
	}

	public void setCallHost(String callHost) {
		this.callHost = callHost;
	}

	public int getCallPort() {
		return callPort;
	}

	public void setCallPort(int callPort) {
		this.callPort = callPort;
	}

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	public Map<String,Object> getParams() {
		return params;
	}
	
	public void setParams(Map<String,Object> params) {
		this.params=params;
	}

	
	public String toString(){
		return originHost+"@"+callHost+":"+callPort+"/"+command+"/"+getId();
	}
}
