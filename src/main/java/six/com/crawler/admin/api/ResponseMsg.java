package six.com.crawler.admin.api;


/**
 * @author six
 * @date 2016年5月31日 上午9:37:06 响应消息
 */
public class ResponseMsg<T> {

	String nodeName;// 响应请求的节点名字
	String errCode;// 错误编码
	String msg;// 信息
	T data;// 返回数据

	public String getNodeName() {
		return nodeName;
	}

	public String getErrCode() {
		return errCode;
	}

	public void setErrCode(String errCode) {
		this.errCode = errCode;
	}

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
}
