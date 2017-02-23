package six.com.crawler.common.dao.provider;

import java.util.Map;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2016年9月12日 上午10:47:56
 */
public class PathDaoProvider extends BaseProvider{

	public String query(Map<String, Object> map) {
		StringBuilder sql = new StringBuilder("select siteCode,ranking,path,filterPath,"
				+ "reslutAttName,appendHead,appendEnd,compareAttName,containKeyWord,replaceWord,replaceValue,depth,"
				+ "emptyExtractCount,`describe` from ex_crawler_platform_paser_path ");
		buildParameter(sql, map);
		return sql.toString();
	}
}
