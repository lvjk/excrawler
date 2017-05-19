package six.com.crawler.admin.api;

/**
 * @author six
 * @date 2016年5月31日 上午9:37:06 响应消息
 * 
 * 
 */
public class ResponseMsg<T> {

	/**
	 * 返回给调用者的提示消息
	 */
	private String msg = "";// 信息
	/**
	 * 请求要么成功要么失败
	 */
	private int isOk;// 信息 1成功 0失败
	/**
	 * 返回给调用者的数据
	 */
	private T data;// 返回数据

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	}

	public int getIsOk() {
		return isOk;
	}

	public void setIsOk(int isOk) {
		this.isOk = isOk;
	}

	public void isOk() {
		this.isOk = 1;
	}

	public void isNoOk() {
		this.isOk = 0;
	}
}
