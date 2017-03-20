package six.com.crawler.common.service;

import java.util.List;

import six.com.crawler.common.entity.WorkerErrMsg;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月17日 下午1:21:01
 */
public interface WorkerErrMsgService {

	void batchSave(List<WorkerErrMsg> WorkerErrMsgs);
}
