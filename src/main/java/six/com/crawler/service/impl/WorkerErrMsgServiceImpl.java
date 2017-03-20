package six.com.crawler.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import six.com.crawler.dao.WorkerErrMsgDao;
import six.com.crawler.entity.WorkerErrMsg;
import six.com.crawler.service.WorkerErrMsgService;

/** 
* @author  作者 
* @E-mail: 359852326@qq.com 
* @date 创建时间：2017年3月17日 下午1:21:10 
*/

@Service
public class WorkerErrMsgServiceImpl implements WorkerErrMsgService{

	@Autowired
	private WorkerErrMsgDao workerErrMsgDao;
	
	public WorkerErrMsgDao getWorkerErrMsgDao() {
		return workerErrMsgDao;
	}

	public void setWorkerErrMsgDao(WorkerErrMsgDao workerErrMsgDao) {
		this.workerErrMsgDao = workerErrMsgDao;
	}

	@Override
	public void batchSave(List<WorkerErrMsg> WorkerErrMsgs) {
		workerErrMsgDao.batchSave(WorkerErrMsgs);
	}

}
