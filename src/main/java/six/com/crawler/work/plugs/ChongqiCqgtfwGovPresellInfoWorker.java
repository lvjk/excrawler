package six.com.crawler.work.plugs;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Element;

import six.com.crawler.common.entity.Page;
import six.com.crawler.common.entity.ResultContext;
import six.com.crawler.work.AbstractCrawlWorker;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2016年11月22日 上午11:01:31
 */
public class ChongqiCqgtfwGovPresellInfoWorker extends AbstractCrawlWorker {

	@Override
	protected void insideInit() {

	}

	@Override
	public void onComplete(Page p, ResultContext resultContext) {

	}

	@Override
	public boolean insideOnError(Exception t, Page p) {
		return false;
	}

	@Override
	protected void beforeDown(Page doingPage) {

	}

	@Override
	protected void beforeExtract(Page doingPage) {
		String issueDateCss = "div[class=wrap-c-m]>table>tbody>tr>td>table>tbody>tr>td>script";
		Element issueDateElement = doingPage.getDoc().select(issueDateCss).first();
		if (null == issueDateElement) {
			throw new RuntimeException("don't find issueDate");
		}
		String issueDate = issueDateElement.html();
		issueDate = StringUtils.substringBetween(issueDate, "var retime = \"", "\";");
		if (StringUtils.isBlank(issueDate)) {
			throw new RuntimeException("don't find issueDate");
		}
		doingPage.getMetaMap().put("issueDate", Arrays.asList(issueDate));
	}

	@Override
	protected void afterExtract(Page page, ResultContext resultContext) {
	}

}
