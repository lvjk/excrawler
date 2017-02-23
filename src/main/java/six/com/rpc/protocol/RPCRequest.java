package six.com.rpc.protocol;

/**
 * @author six
 * @date 2016年6月2日 下午4:14:39 rpc 请求
 */
public class RPCRequest {

	private String requestIP;// 请求调用主机
	private int requestPort;// 请求调用Port
	private byte[] param;// 请求参数

	public String getRequestIP() {
		return requestIP;
	}

	public void setRequestIP(String requestIP) {
		this.requestIP = requestIP;
	}

	public int getRequestPort() {
		return requestPort;
	}

	public void setRequestPort(int requestPort) {
		this.requestPort = requestPort;
	}

	public byte[] getParam() {
		return param;
	}

	public void setParam(byte[] param) {
		this.param = param;
	}
}
