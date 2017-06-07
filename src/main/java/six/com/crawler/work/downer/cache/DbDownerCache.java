package six.com.crawler.work.downer.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import six.com.crawler.dao.PageDao;
import six.com.crawler.dao.po.PagePo;
import six.com.crawler.entity.Page;
import six.com.crawler.utils.JavaSerializeUtils;

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
		PagePo pagePo = new PagePo();
		pagePo.setSiteCode(page.getSiteCode());
		pagePo.setPageKey(page.getKey());
		pagePo.setPageUrl(page.toString());
		pagePo.setPageSrc(page.getPageSrc());
		byte[] data = JavaSerializeUtils.serialize(page);
		pagePo.setData(data);
		pageDao.save(pagePo);
	}

	@Override
	protected Page doRead(Page page) {
		Page cachePage = null;
		String siteCode = page.getSiteCode();
		String pageKey = page.getPageKey();
		PagePo pagePo = pageDao.queryBySiteAndKey(siteCode, pageKey);
		if (null != pagePo && null != pagePo.getData()) {
			cachePage = JavaSerializeUtils.unSerialize(pagePo.getData(), Page.class);
			cachePage.setPageSrc(pagePo.getPageSrc());
		}
		return cachePage;
	}

}
