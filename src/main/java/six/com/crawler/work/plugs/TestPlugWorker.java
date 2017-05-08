package six.com.crawler.work.plugs;

import six.com.crawler.entity.JobSnapshot;
import six.com.crawler.entity.Page;
import six.com.crawler.work.AbstractWorker;

/** 
* @author  作者 
* @E-mail: 359852326@qq.com 
* @date 创建时间：2017年5月8日 上午10:53:19 
*/
public class TestPlugWorker extends AbstractWorker<Page>{

	public TestPlugWorker(Class<Page> workSpaceDataClz) {
		super(workSpaceDataClz);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void initWorker(JobSnapshot jobSnapshot) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void insideWork(Page workerData) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void onError(Exception t, Page workerData) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void insideDestroy() {
		// TODO Auto-generated method stub
		
	}

}
