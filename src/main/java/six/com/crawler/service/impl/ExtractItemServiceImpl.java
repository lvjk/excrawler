package six.com.crawler.service.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import six.com.crawler.api.ResponseMsg;
import six.com.crawler.dao.ExtractItemDao;
import six.com.crawler.service.BaseService;
import six.com.crawler.service.ExtractItemService;
import six.com.crawler.work.extract.ExtractItem;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月30日 上午10:31:02
 */
@Service
public class ExtractItemServiceImpl extends BaseService implements ExtractItemService {

	final static Logger log = LoggerFactory.getLogger(ExtractItemServiceImpl.class);

	@Autowired
	private ExtractItemDao extractItemDao;

	@Override
	public ResponseMsg<List<ExtractItem>> query(String jobName) {
		ResponseMsg<List<ExtractItem>> response = createResponseMsg();
		List<ExtractItem> result = extractItemDao.query(jobName);
		response.setData(result);
		response.setIsOk(1);
		return response;
	}

	@Override
	public ResponseMsg<String> add(ExtractItem extractItem) {
		return null;
	}

	@Override
	public ResponseMsg<String> update(ExtractItem extractItem) {
		return null;
	}

	@Override
	public ResponseMsg<String> del(String id) {
		return null;
	}

	public ExtractItemDao getExtractItemDao() {
		return extractItemDao;
	}

	public void setExtractItemDao(ExtractItemDao extractItemDao) {
		this.extractItemDao = extractItemDao;
	}

}
