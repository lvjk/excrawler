package six.com.crawler.admin.api;

/**
 * @author six
 * @date 2016年5月31日 上午9:37:06 响应消息
 */
public class ResponseMsg<T> {

	private final String nodeName;// 响应请求的节点名字
	private String msg="";// 信息
	private int isOk;// 信息 1成功 0失败
	private T data;// 返回数据

	public ResponseMsg(String nodeName){
		this.nodeName=nodeName;
	}
	
	public String getNodeName() {
		return nodeName;
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
	
	public int getIsOk() {
		return isOk;
	}

	public void setIsOk(int isOk) {
		this.isOk = isOk;
	}
}
