package six.com.crawler.work.downer.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import six.com.crawler.dao.PageDao;
import six.com.crawler.entity.Page;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年5月15日 下午6:12:12
 */
public class DbDownerCache extends AbstractDownerCache {

	private PageDao pageDao;

	public DbDownerCache(String siteCode, PageDao pageDao) {
		super(siteCode);
		this.pageDao = pageDao;
	}

	protected final static Logger log = LoggerFactory.getLogger(DbDownerCache.class);

	@Override
	protected void doWirte(Page page) {
		pageDao.save(page);
	}

	@Override
	protected Page doRead(Page page) {
		String siteCode = page.getSiteCode();
		String pageKey = page.getPageKey();
		page = pageDao.queryByPageKey(siteCode, pageKey);
		return page;
	}

}
