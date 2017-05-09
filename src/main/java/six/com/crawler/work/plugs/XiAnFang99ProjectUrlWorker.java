package six.com.crawler.work.plugs;

import java.util.List;

import six.com.crawler.entity.Page;
import six.com.crawler.entity.PageType;
import six.com.crawler.entity.ResultContext;
import six.com.crawler.work.AbstractCrawlWorker;
import six.com.crawler.work.space.WorkSpace;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2016年12月26日 下午3:55:06
 */
public class XiAnFang99ProjectUrlWorker extends AbstractCrawlWorker {

	WorkSpace<Page> projectInfoQueue;

	@Override
	protected void insideInit() {
		projectInfoQueue = getManager().getWorkSpaceManager().newWorkSpace("xianfang99_project_info_2", Page.class);
	}

	@Override
	protected void beforeDown(Page doingPage) {

	}

	@Override
	protected void beforeExtract(Page doingPage) {

	}

	@Override
	protected void afterExtract(Page doingPage, ResultContext resultContext) {
		List<String> projectInfoUrls = resultContext.getExtractResult("项目信息url_1");
		if (null != projectInfoUrls && projectInfoUrls.size() > 0) {
			String url = projectInfoUrls.get(0);
			Page newPage = new Page(doingPage.getSiteCode(), 1, url, url);
			newPage.setReferer(doingPage.getFinalUrl());
			newPage.setType(PageType.DATA.value());
			if (!projectInfoQueue.isDone(newPage.getPageKey())) {
				projectInfoQueue.push(newPage);
			}
		}

	}

	@Override
	public void onComplete(Page p, ResultContext resultContext) {
	}

	@Override
	public boolean insideOnError(Exception t, Page doingPage) {
		return false;
	}

}
