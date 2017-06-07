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
	 * @return 返回所有数据集合
	 */
	@Select("select `host`,`port`,`type`,`userName`,`passWord`,`expire`,`describe`,`version` from "
			+ TableNames.HTTP_PROXY_TABLE_NAME)
	List<HttpProxy> getAll();

	/**
	 * 保存指定代理
	 * 
	 * @param httpProxy
	 * @return 返回保存的数据条数
	 */
	@InsertProvider(type = HttpProxyDaoProvider.class, method = "save")
	int save(HttpProxy httpProxy);

	/**
	 * 通过指定host和post删除代理
	 * 
	 * @param host
	 *            http代理主机
	 * @param port
	 *            http代理端口
	 * @return 返回删除掉的数据条数
	 */
	@Delete("delete from " + TableNames.HTTP_PROXY_TABLE_NAME + " where `host` = #{host} and `port` = #{port}")
	int del(String host, int port);

	/**
	 * 删除所有代理
	 * 
	 * @return 返回删除掉的数据条数
	 */
	@Delete("delete from " + TableNames.HTTP_PROXY_TABLE_NAME)
	int delAll();
}
