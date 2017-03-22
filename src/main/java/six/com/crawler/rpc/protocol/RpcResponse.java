package six.com.crawler.rpc.protocol;

import java.io.Serializable;

/**
 * @author six
 * @date 2016年6月2日 下午4:15:17 rpc响应
 */
public class RpcResponse extends RpcMsg implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -7823438169107836197L;


	// 响应结果
	private Object result;
	
	private boolean succeed;
	
	private String msg;
	
	private Exception exception;


	public Exception getException() {
		return exception;
	}

	public void setException(Exception exception) {
		this.exception = exception;
	}

	public Object getResult() {
		return result;
	}

	public void setResult(Object result) {
		this.result = result;
	}
	public boolean isSucceed() {
		return succeed;
	}

	public void setSucceed(boolean succeed) {
		this.succeed = succeed;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}
	
}
