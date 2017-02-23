package six.com.crawler.work.plugs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import six.com.crawler.common.entity.Job;
import six.com.crawler.common.entity.Site;
import six.com.crawler.schedule.AbstractSchedulerManager;
import six.com.crawler.work.AbstractWorker;
import six.com.crawler.work.WorkQueue;
import six.com.crawler.work.WorkerLifecycleState;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2016年10月27日 上午10:38:51
 */
public class TestWorker extends AbstractWorker {

	final static Logger LOG = LoggerFactory.getLogger(TestWorker.class);

	public TestWorker(String name, AbstractSchedulerManager manager, Job job, Site site, WorkQueue stored) {
		super(name, manager, job);
	}

	int processCount;

	@Override
	protected void insideWork() throws Exception {
		processCount++;
		LOG.info("test worker process:" + processCount);
		if (processCount >= 20) {
			compareAndSetState(WorkerLifecycleState.STARTED, WorkerLifecycleState.WAITED);
		}
	}
	
	@Override
	protected void initWorker() {
		
	}
	
	protected void insideDestroy(){
		
	}

	@Override
	protected void onError(Exception t) {
		
	}

}
