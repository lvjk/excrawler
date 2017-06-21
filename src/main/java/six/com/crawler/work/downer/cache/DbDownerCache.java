package six.com.crawler.work.downer.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import six.com.crawler.dao.PageDao;
import six.com.crawler.dao.po.PagePo;
import six.com.crawler.entity.JobSnapshot;
import six.com.crawler.entity.Page;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年5月15日 下午6:12:12
 */
public class DbDownerCache extends AbstractDownerCache {

	private PageDao pageDao;

	public DbDownerCache(String siteCode, JobSnapshot jobSnapshot, PageDao pageDao) {
		super(siteCode, jobSnapshot);
		this.pageDao = pageDao;
	}

	protected final static Logger log = LoggerFactory.getLogger(DbDownerCache.class);

	@Override
	protected void doWirte(PagePo page) {
		pageDao.save(page);
	}

	@Override
	protected PagePo doRead(Page page) {
		PagePo pagePo = null;
		String siteCode = page.getSiteCode();
		String pageKey = page.getPageKey();
		pagePo = pageDao.queryBySiteAndKey(siteCode, pageKey);
		return pagePo;
	}

}
