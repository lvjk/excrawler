package six.com.crawler.common;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

/** 
* @author  作者 
* @E-mail: 359852326@qq.com 
* @date 创建时间：2017年1月24日 上午11:51:11 
*/
public class JsonUtils {

	public static void main(String[] args) {
		String value="{'name':'six\\sssss'}";
		System.out.println(value);
		Map<String,String> map=new HashMap<>();
		map.put("name", value);
		System.out.println(map);
		value=StringUtils.replace(value, "\\", "\\\\");
		Object ob=six.com.crawler.common.utils.JsonUtils.toObject(value, Map.class);
		System.out.println(ob);
	}

}
