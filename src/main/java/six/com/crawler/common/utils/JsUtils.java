package six.com.crawler.common.utils;

import java.util.HashMap;
import java.util.Map;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.apache.commons.lang3.StringUtils;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2016年9月28日 下午1:42:21
 */
public class JsUtils {

	/**
	 * 算术符号map
	 */
	static Map<String, String> arithmeticSymbolMap = new HashMap<>();

	static {
		arithmeticSymbolMap.put("加", "+");
		arithmeticSymbolMap.put("减", "-");
		arithmeticSymbolMap.put("乘", "*");
		arithmeticSymbolMap.put("除", "/");
	}

	static ScriptEngine engine = new ScriptEngineManager().getEngineByName("javascript");

	/**
	 * 计算 结果 "2+2"="4"; "2-2"="0"; "2*2"="4"; "2/2"="1"; "二二"=defaultResult;
	 * 
	 * @param js
	 * @param defaultResult
	 * @return
	 */
	public static String eval(String js, String defaultResult) {
		Object ob = null;
		js = convert(js);
		String result = defaultResult;
		try {
			result = (ob = engine.eval(js)) != null ? ob.toString() : defaultResult;
		} catch (ScriptException e) {
			// 忽略
		}
		return result;
	}

	public static String convert(String js) {
		for (String key : arithmeticSymbolMap.keySet()) {
			js = StringUtils.replace(js, key, arithmeticSymbolMap.get(key));
		}
		return js;
	}
	
	public static void main(String[] s){
		String js="var retime = '2016-11-18 17:10:15.0';var arr = retime.split(' ');document.write(arr[0]);";
		String result=eval(js, "");
		System.out.println(result);
	}
}
