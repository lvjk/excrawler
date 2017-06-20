package six.com.crawler.work.downer.cache;

import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import six.com.crawler.dao.po.PagePo;
import six.com.crawler.entity.JobSnapshot;
import six.com.crawler.entity.Page;
import six.com.crawler.utils.JavaSerializeUtils;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年5月15日 下午5:58:27
 */
public abstract class AbstractDownerCache implements DownerCache {

	protected final static Logger log = LoggerFactory.getLogger(AbstractDownerCache.class);

	/** 结束标志对象 **/
	private static PagePo endFlag = new PagePo();
	private String siteCode;
	private JobSnapshot jobSnapshot;
	private LinkedBlockingQueue<PagePo> writeCacheQueue = new LinkedBlockingQueue<PagePo>();
	private Thread cacheThread;

	public AbstractDownerCache(String siteCode,JobSnapshot jobSnapshot) {
		this.siteCode = siteCode;
		this.jobSnapshot=jobSnapshot;
		cacheThread = new Thread(() -> {
			loopDoWirte();
		}, "downer-write-cache-thread");
		cacheThread.start();
	}

	@Override
	public final void write(Page page) {
		if(null!=page){
			PagePo pagePo = new PagePo();
			pagePo.setJobName(jobSnapshot.getName());
			pagePo.setJobSnapshotId(jobSnapshot.getId());
			pagePo.setSiteCode(page.getSiteCode());
			pagePo.setPageKey(page.getKey());
			pagePo.setPageUrl(page.toString());
			pagePo.setPageSrc(page.getPageSrc());
			byte[] data = JavaSerializeUtils.serialize(page);
			pagePo.setData(data);
			writeCacheQueue.add(pagePo);
		}
	}

	@Override
	public final Page read(Page page) {
		Page cachePage = null;
		if(null!=page){
			PagePo pagePo=doRead(page);
			if (null != pagePo && null != pagePo.getData()) {
				cachePage = JavaSerializeUtils.unSerialize(pagePo.getData(), Page.class);
				cachePage.setPageSrc(pagePo.getPageSrc());
			}
		}
		return cachePage;
	}

	private void loopDoWirte() {
		PagePo page = null;
		while (true) {
			try {
				page = writeCacheQueue.take();
			} catch (InterruptedException e1) {
			}
			if (null != page) {
				if (page == endFlag) {
					break;
				} else {
					try {
						doWirte(page);
					} catch (Exception e) {
						log.error("donwer cache write page:" + page.toString(), e);
					}
				}
			}
		}
	}

	protected abstract void doWirte(PagePo page);

	protected abstract PagePo doRead(Page page);

	public String getSiteCode() {
		return siteCode;
	}

	@Override
	public void close() {
		writeCacheQueue.add(endFlag);
	}

}
