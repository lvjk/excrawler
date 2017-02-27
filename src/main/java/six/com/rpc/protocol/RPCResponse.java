package six.com.rpc.protocol;
/**
 *@author six    
 *@date 2016年6月2日 下午4:15:17  
 *rpc响应
*/
public class RPCResponse {
	
	private String responseIP;// 响应请求主机
	private int  responsePort;// 响应请求Port
	private byte[] data;// 响应数据
	
	public String getResponseIP() {
		return responseIP;
	}
	public void setResponseIP(String responseIP) {
		this.responseIP = responseIP;
	}
	public int getResponsePort() {
		return responsePort;
	}
	public void setResponsePort(int responsePort) {
		this.responsePort = responsePort;
	}
	public byte[] getData() {
		return data;
	}
	public void setData(byte[] data) {
		this.data = data;
	}
}
