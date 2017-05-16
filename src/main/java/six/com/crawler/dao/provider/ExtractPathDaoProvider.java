package six.com.crawler.dao.provider;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.jdbc.SQL;

import six.com.crawler.dao.BaseDao;
import six.com.crawler.dao.TableNames;
import six.com.crawler.work.extract.ExtractPath;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2016年9月12日 上午10:47:56
 */
public class ExtractPathDaoProvider extends BaseProvider{
	
	private String queryColumns="`name`,"
			+ "siteCode,"
			+ "ranking,"
			+ "`path`,"
			+ "`tableHeadPath`,"
			+ "`tableDataPath`,"
			+ "filterPath,"
			+ "extractAttName,"
			+ "substringStart,"
			+ "substringEnd,"	
			+ "appendHead,"
			+ "appendEnd,"			
			+ "compareAttName,"
			+ "containKeyWord,"
			+ "replaceWord,"
			+ "replaceValue,"
			+ "extractEmptyCount,"
			+ "`describe`,"
			+ "`version` ";
	
	private String insertColumns="`name`,"
			+ "siteCode,"
			+ "ranking,"
			+ "`path`,"
			+ "`tableHeadPath`,"
			+ "`tableDataPath`,"
			+ "filterPath,"
			+ "extractAttName,"
			+ "substringStart,"
			+ "substringEnd,"	
			+ "appendHead,"
			+ "appendEnd,"			
			+ "compareAttName,"
			+ "containKeyWord,"
			+ "replaceWord,"
			+ "replaceValue,"
			+ "extractEmptyCount,"
			+ "`describe`";
	
	public String queryBySite(String siteCode) {
		SQL sql = new SQL();
		sql.SELECT(queryColumns);
		sql.FROM(TableNames.SITE_EXTRACT_PATH_TABLE_NAME);
		sql.WHERE("siteCode=#{siteCode}");
		sql.ORDER_BY("ranking asc");
		return sql.toString();
	}
	
	public String query(Map<String,Object> param) {
		SQL sql = new SQL();
		sql.SELECT(queryColumns);
		sql.FROM(TableNames.SITE_EXTRACT_PATH_TABLE_NAME);
		sql.WHERE("`name`=#{name}");
		sql.AND().WHERE("siteCode=#{siteCode}");
		sql.ORDER_BY("ranking asc");
		return sql.toString();
	}
	
	public String fuzzyQuery(Map<String,Object> params) {
		String sql="		select "
				+queryColumns
				+ "       from "+TableNames.SITE_EXTRACT_PATH_TABLE_NAME
				+ "      where `siteCode` like concat(#{siteCode},'%')"
				+ "      or `name` like concat(#{name},'%')"
				+ "      order by ranking asc ";
		return sql;
	}
	
	
	@SuppressWarnings("unchecked")
	public String batchSave(Map<String, Object> map) {
		List<ExtractPath> extractPaths = (List<ExtractPath>) map.get(BaseDao.BATCH_SAVE_PARAM);
		String values="(#{list["+INDEX_FLAG+"].name},"
				+ "#{list["+INDEX_FLAG+"].siteCode},"
				+ "#{list["+INDEX_FLAG+"].ranking},"
				+ "#{list["+INDEX_FLAG+"].path},"
				+ "#{list["+INDEX_FLAG+"].tableHeadPath},"
				+ "#{list["+INDEX_FLAG+"].tableDataPath},"
				+ "#{list["+INDEX_FLAG+"].filterPath},"
				+ "#{list["+INDEX_FLAG+"].extractAttName},"
				+ "#{list["+INDEX_FLAG+"].substringStart},"
				+ "#{list["+INDEX_FLAG+"].substringEnd},"
				+ "#{list["+INDEX_FLAG+"].appendHead},"
				+ "#{list["+INDEX_FLAG+"].appendEnd},"
				+ "#{list["+INDEX_FLAG+"].compareAttName},"
				+ "#{list["+INDEX_FLAG+"].containKeyWord},"
				+ "#{list["+INDEX_FLAG+"].replaceWord},"
				+ "#{list["+INDEX_FLAG+"].replaceValue},"
				+ "#{list["+INDEX_FLAG+"].extractEmptyCount},"
				+ "#{list["+INDEX_FLAG+"].describe})";
		StringBuilder sbd = new StringBuilder();  
		sbd.append("insert into ").append(TableNames.SITE_EXTRACT_PATH_TABLE_NAME);  
		sbd.append("(").append(insertColumns).append(") ");  
		sbd.append("values");  
		sbd.append(setBatchSaveSql(values,extractPaths));
		return sbd.toString();
	}
	
	public String delByName(String name) {
		SQL sql = new SQL();
		sql.DELETE_FROM(TableNames.SITE_EXTRACT_PATH_TABLE_NAME);
		sql.WHERE("name = #{name}");
		return sql.toString();
	}
	
	public String delBySiteCode(String siteCode) {
		SQL sql = new SQL();
		sql.DELETE_FROM(TableNames.SITE_EXTRACT_PATH_TABLE_NAME);
		sql.WHERE("siteCode = #{siteCode}");
		return sql.toString();
	}
}
