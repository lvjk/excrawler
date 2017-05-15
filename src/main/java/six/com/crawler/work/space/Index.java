package six.com.crawler.work.space;

import java.io.Serializable;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年5月12日 下午3:10:03
 */
public class Index implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 8530540957218975935L;
	private String mapKey;
	private String dataKey;
	
	public String getMapKey() {
		return mapKey;
	}
	public void setMapKey(String mapKey) {
		this.mapKey = mapKey;
	}
	public String getDataKey() {
		return dataKey;
	}
	public void setDataKey(String dataKey) {
		this.dataKey = dataKey;
	}
}
