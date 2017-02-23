package six.com.crawler.common.ocr;

import java.io.Serializable;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2016年9月27日 下午4:43:58
 */
public class CharImage implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -196809906813611157L;
	private String path;
	private String result;
	private String hash;
	
	
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public String getResult() {
		return result;
	}
	public void setResult(String result) {
		this.result = result;
	}
	public String getHash() {
		return hash;
	}
	public void setHash(String hash) {
		this.hash = hash;
	}
}
