package six.com.crawler.work.store;

import java.util.Map;

import six.com.crawler.work.space.Index;
import six.com.crawler.work.space.WorkSpaceData;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月29日 下午3:24:52
 */
public class RedisStoreData implements WorkSpaceData {

	private String key;

	private Map<String, String> dataMap;

	public Map<String, String> getDataMap() {
		return dataMap;
	}

	public void setDataMap(Map<String, String> dataMap) {
		this.dataMap = dataMap;
	}

	@Override
	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	@Override
	public Index getIndex() {
		return null;
	}

	@Override
	public void setIndex(Index index) {
		// TODO Auto-generated method stub
		
	}
}
