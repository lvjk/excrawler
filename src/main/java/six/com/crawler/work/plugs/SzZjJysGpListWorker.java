package six.com.crawler.work.plugs;

import java.util.HashMap;
import java.util.Map;

import six.com.crawler.entity.Page;
import six.com.crawler.entity.ResultContext;
import six.com.crawler.work.AbstractCrawlWorker;

/** 
* @author  作者 
* @E-mail: 359852326@qq.com 
* @date 创建时间：2017年6月16日 上午11:32:26 
*/
public class SzZjJysGpListWorker extends AbstractCrawlWorker {
	
	private String seedUrl="http://www.szse.cn/main/marketdata/hqcx/zqhq_history/";

	@Override
	protected void insideInit() {
		Page firstPage=new Page(getSite().getCode(), 1, seedUrl, seedUrl);
		Map<String,Object> param=new HashMap<>();
		
		firstPage.setParameters(param);
	}

	@Override
	protected void beforeDown(Page doingPage) {
		
	}

	@Override
	protected void beforeExtract(Page doingPage) {
		
	}

	@Override
	protected void afterExtract(Page doingPage, ResultContext resultContext) {
		
	}

	@Override
	protected void onComplete(Page doingPage, ResultContext resultContext) {
		
	}

}
