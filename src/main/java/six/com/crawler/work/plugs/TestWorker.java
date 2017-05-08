package six.com.crawler.work.plugs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import six.com.crawler.entity.JobSnapshot;
import six.com.crawler.entity.Page;
import six.com.crawler.utils.ThreadUtils;
import six.com.crawler.work.AbstractWorker;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2016年10月27日 上午10:38:51
 */
public class TestWorker extends AbstractWorker<Page> {

	public TestWorker() {
		super(Page.class);
	}

	final static Logger log = LoggerFactory.getLogger(TestWorker.class);

	int processCount;

	@Override
	protected void insideWork(Page doingPage) throws Exception {
		processCount++;
		log.info("test worker process:" + processCount);
		ThreadUtils.sleep(3000);
	}

	protected void insideDestroy() {

	}

	@Override
	protected void onError(Exception t,Page doingPage) {

	}

	@Override
	protected void initWorker(JobSnapshot jobSnapshot) {

	}

}
