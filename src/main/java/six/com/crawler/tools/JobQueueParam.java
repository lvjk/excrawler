package six.com.crawler.tools;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSON;

public class JobQueueParam {
	
	private String url;
	private String method;
	private Map<String,Object> metas;
	private Map<String,Object> params;
	
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getMethod() {
		return method;
	}
	public void setMethod(String method) {
		this.method = method;
	}
	public Map<String,Object> getMetas() {
		return metas;
	}
	public void setMetas(Map<String,Object> metas) {
		this.metas = metas;
	}
	public Map<String,Object> getParams() {
		return params;
	}
	public void setParams(Map<String,Object> params) {
		this.params = params;
	}
	
	public static void main(String[] args) {
		JobQueueParam param=new JobQueueParam();
		Map<String,Object> metas=new HashMap<String,Object>();
		metas.put("id", 1);
		metas.put("name", "name1");
		param.setMetas(metas);
		
		Map<String,Object> params=new HashMap<String,Object>();
		params.put("id", 1);
		params.put("name", "name1");
		param.setParams(params);
		param.setMethod("get");
		param.setUrl("http://newhouse.cnnbfdc.com/lpxxshow.aspx?projectid=14761");
		
		System.out.println(JSON.toJSONString(param));
	}
	
}
