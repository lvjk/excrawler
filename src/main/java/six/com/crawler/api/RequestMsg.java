package six.com.crawler.api;
/**
 *@author six    
 *@date 2016年5月31日 上午9:52:02  
*/
public class RequestMsg<T> {

	private String requestFrom;//请求来源
	private T data;//请求参数

	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	} 
	public String getRequestFrom() {
		return requestFrom;
	}

	public void setRequestFrom(String requestFrom) {
		this.requestFrom = requestFrom;
	}
}
