package six.com.crawler.entity;

import java.io.Serializable;

/** 
* @author  作者 
* @E-mail: 359852326@qq.com 
* @date 创建时间：2017年3月24日 上午10:07:02 
* 基本数据定义, 所有需要存储的数据都需要继承此类 ，有id 和version 2个字段
*/
public abstract class BaseVo implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 6376642014574218774L;

	/**
	 * 数据id
	 */
	private String id;
	
	/**
	 * 数据版本
	 */
	private int version;//数据版本
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}
}
