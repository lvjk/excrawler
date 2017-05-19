package six.com.crawler.work.space;

import org.junit.Test;

import six.com.crawler.BaseTest;
import six.com.crawler.tools.WorkeSpaceTransferTools;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年5月12日 上午10:23:52
 */
public class WorkeSpaceTransfer extends BaseTest {


	@Test
	public void test() {
		//delRedis();
	}

	protected void doRedis() {
		String redisConnection = "172.30.103.81:6379;172.30.103.82:6379;172.30.103.83:6379";
		String workSpaceName = "tmsf_house_info";
		WorkeSpaceTransferTools.transfer(workSpaceName, redisConnection);
	}
	
	protected void delRedis() {
		WorkeSpaceTransferTools.del( "workspace_segment_queue");
	}

	protected void doMysql() {
		WorkeSpaceTransferTools.doMysql();
	}

	
}
