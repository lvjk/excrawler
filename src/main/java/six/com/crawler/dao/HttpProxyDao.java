package six.com.crawler.dao;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Select;

import six.com.crawler.dao.provider.HttpProxyDaoProvider;
import six.com.crawler.entity.HttpProxy;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年5月19日 上午9:26:20
 */
public interface HttpProxyDao {

	/**
	 * 获取所有代理
	 * 
	 * @return
	 */
	@Select("select `host`,`port`,`type`,`userName`,`passWord`,`expire`,`describe`,`version` from "
			+ TableNames.HTTP_PROXY_TABLE_NAME)
	List<HttpProxy> getAll();

	/**
	 * 保存代理
	 * 
	 * @param httpProxy
	 * @return
	 */
	@InsertProvider(type = HttpProxyDaoProvider.class, method = "save")
	int save(HttpProxy httpProxy);

	/**
	 * 通过指定host和post删除代理
	 * 
	 * @param host
	 * @param port
	 * @return
	 */
	@Delete("delete from " + TableNames.HTTP_PROXY_TABLE_NAME + " where `host` = #{host} and `port` = #{port}")
	int del(String host, int port);

	/**
	 * 删除所有代理
	 * 
	 * @return
	 */
	@Delete("delete from " + TableNames.HTTP_PROXY_TABLE_NAME)
	int delAll();
}
