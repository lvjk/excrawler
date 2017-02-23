package six.com.crawler.work.downer;

import java.io.Serializable;


/**
 * @author six
 * @date 2016年7月1日 下午3:36:49
 */
public enum DownerType implements Serializable {
	OKHTTP(1),
	HTTPCLIENT(2),
	CHROME(3),
	PHANTOMJS(4); 

	final int value;

	DownerType(int value) {
		this.value = value;
	}

	public int value() {
		return value;
	}

	public  static DownerType valueOf(int type) {
		if (1==type) {
			return OKHTTP;
		}else if (2==type) {
			return HTTPCLIENT;
		} else if (3==type) {
			return CHROME;
		} else if (4==type) {
			return PHANTOMJS;
		} else {
			return OKHTTP;
		}
	}
}
