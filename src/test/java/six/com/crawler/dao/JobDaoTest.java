package six.com.crawler.dao;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import six.com.crawler.BaseTest;
import six.com.crawler.common.dao.JobDao;
import six.com.crawler.common.entity.Job;
import six.com.crawler.common.entity.JobSnapshotState;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2016年9月9日 下午2:31:59
 */
public class JobDaoTest extends BaseTest {

	protected final static Logger LOG = LoggerFactory.getLogger(JobDaoTest.class);
			
	

	@Test
	public void test() {
		// Configure.CURRENT_NODE.getName(),0
//		Job job = jobService.queryByName("testJob");
//		job.setState(JobState.EXECUTING.get());
		//jobService.update(job);
	}
	
	@Test
	public void query(){
		Job job=jobDao.query("cq315house_pre_sale1");
		if(null!=job){
			LOG.info(job.toString());
		}
	}

}
