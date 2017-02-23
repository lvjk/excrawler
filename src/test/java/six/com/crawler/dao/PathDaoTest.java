package six.com.crawler.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import six.com.crawler.BaseTest;
import six.com.crawler.common.dao.PathDao;
import six.com.crawler.work.extract.ExtractPath;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2016年9月12日 上午9:35:02
 */
public class PathDaoTest extends BaseTest {

	@Autowired
	public PathDao pathdao;

	@Test
	public void test() {
//		Map<String, Object> parameters = new HashMap<>();
//		parameters.put("siteCode","qichacha");
//		parameters.put("type",1);
//		parameters.put("ranking",1);
//		parameters.put("depth",0);
//		List<PaserPath> path = pathdao.query(parameters);
//		LOG.info(path.toString());
	}
}
