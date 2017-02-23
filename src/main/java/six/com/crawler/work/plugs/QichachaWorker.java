package six.com.crawler.work.plugs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import six.com.crawler.common.entity.Job;
import six.com.crawler.common.entity.Page;
import six.com.crawler.common.entity.ResultContext;
import six.com.crawler.common.entity.Site;
import six.com.crawler.common.utils.JsoupUtils;
import six.com.crawler.common.utils.JsoupUtils.TableResult;
import six.com.crawler.schedule.AbstractSchedulerManager;
import six.com.crawler.work.AbstractCrawlWorker;
import six.com.crawler.work.WorkQueue;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com+
 * @date 创建时间：2016年11月17日 下午5:39:15
 */
public class QichachaWorker extends AbstractCrawlWorker {

	protected final static Logger LOG = LoggerFactory.getLogger(QichachaSearchWorker.class);

	private Map<String, String> fieldMap = new HashMap<String, String>();

	public QichachaWorker(String name, AbstractSchedulerManager manager, Job job, Site site, WorkQueue stored) {
		super(name, manager, job, site, stored);
	}

	@Override
	protected void insideInit() {
		fieldMap.put("法定代表人：", "corporater");
		fieldMap.put("企业地址：", "address");
		fieldMap.put("经营范围：", "operateRange");
		fieldMap.put("成立日期：", "foundDate");
		fieldMap.put("注册资本：", "registeredCapital");
		fieldMap.put("营业期限：", "operatingPeriod");
		fieldMap.put("发照日期：", "issueDate");
		fieldMap.put("经营状态：", "status");
		fieldMap.put("所属行业：", "industry");
	}

	@Override
	public void onComplete(Page p,ResultContext resultContext) {

	}

	@Override
	public void insideOnError(Exception t, Page p) {

	}

	protected void beforeDown(Page doingPage) {

	}

	@Override
	protected void beforeExtract(Page doingPage) {
		String html = doingPage.getPageSrc();
		Document document = Jsoup.parse(html);
		Elements temp = document.select("section[id=Cominfo]>table");
		Element table = temp.first();
		List<TableResult> results = JsoupUtils.paserTable(table);
		for (TableResult tableResult : results) {
			String key = fieldMap.get(tableResult.getKey());
			if (StringUtils.isNotBlank(key)) {
				doingPage.getMetaMap().computeIfAbsent(key, mapKey -> new ArrayList<>()).add(tableResult.getValue());
			}
		}
	}

	@Override
	protected void afterExtract(Page doingPage, ResultContext result) {

	}

}
