package six.com.crawler.work;


import six.com.crawler.common.entity.Job;
import six.com.crawler.common.entity.WorkerSnapshot;
import six.com.crawler.schedule.AbstractSchedulerManager;

/**
 * @author six
 * @date 2016年1月15日 下午6:20:00
 */
public interface Worker extends WorkerLifecycle {
	
	
	void bindManager(AbstractSchedulerManager manager);
	
	void bindJob(Job job);
	/**
	 * 初始化
	 */
	public void init();

	/**
	 * 获取 manager
	 * 
	 * @return
	 */
	AbstractSchedulerManager getManager();

	/**
	 * 获取 worker name
	 * 
	 * @return
	 */
	String getName();
	

	/**
	 * 获取worker 快照
	 * 
	 * @return
	 */
	WorkerSnapshot getWorkerSnapshot();

	/**
	 * 获取最后一次活动时间
	 * 
	 * @return
	 */
	long getLastActivityTime();

	/**
	 * 获取work Job
	 * 
	 * @return
	 */
	Job getJob();

	/**
	 * 默认空worker
	 */
	public static Worker EMPTY_WORKER = new Worker() {

		@Override
		public boolean isRunning() {
			return false;
		}

		@Override
		public void suspend() {
		}

		@Override
		public void stop() {
		}

		@Override
		public void start() {
		}

		@Override
		public WorkerLifecycleState getState() {
			return null;
		}

		@Override
		public void destroy() {
		}

		@Override
		public String getName() {
			return null;
		}

		@Override
		public WorkerSnapshot getWorkerSnapshot() {
			return null;
		}


		@Override
		public void goOn() {

		}

		@Override
		public void waited() {

		}

		@Override
		public void init() {

		}

		@Override
		public AbstractSchedulerManager getManager() {
			return null;
		}

		

		@Override
		public Job getJob() {
			return null;
		}


		@Override
		public long getLastActivityTime() {
			return 0;
		}

		@Override
		public void bindManager(AbstractSchedulerManager manager) {
			
		}

		@Override
		public void bindJob(Job job) {
			
		}

	};

}
