package six.com.crawler.common.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;

import six.com.crawler.common.DateFormats;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年2月20日 下午3:16:05
 */
public class JobTableUtils {

	public final static String TABLE_NAME_FLAG = "<<tableName>>";

	public static String buildSelectSql(String selectSqlTemplate, String tableName) {
		return replaceTableNameFlag(selectSqlTemplate, tableName);
	}

	public static String buildCreateTableSql(String createTableSqlTemplate, String tableName) {
		return replaceTableNameFlag(createTableSqlTemplate, tableName);
	}

	public static String buildInsertSql(String insertSqlTemplate, String tableName) {
		return replaceTableNameFlag(insertSqlTemplate, tableName);
	}
	
	public static String buildUpdateSql(String insertSqlTemplate, String tableName) {
		return replaceTableNameFlag(insertSqlTemplate, tableName);
	}

	public static String buildDelSql(String delSqlTemplate, String tableName) {
		return replaceTableNameFlag(delSqlTemplate, tableName);
	}

	private static String replaceTableNameFlag(String sql, String tableName) {
		String newSql = null;
		if(sql!=null){
			newSql = sql.replaceAll(TABLE_NAME_FLAG, tableName);
		}
		return newSql;
	}

	/**
	 * job 数据表名生成=固定表名前缀+时间
	 * 
	 * @param fixedTableNamePre
	 * @return
	 */
	public static String buildJobTableName(String fixedTableNamePre) {
		return fixedTableNamePre + "_" + DateFormatUtils.format(System.currentTimeMillis(), DateFormats.DATE_FORMAT_2);
	}

	/**
	 * job 数据表名生成=固定表名前缀+时间
	 * 
	 * @param fixedTableNamePre
	 * @return
	 */
	public static String buildJobTableName(String fixedTableNamePre, long time) {
		return fixedTableNamePre + "_" + time;
	}

	public static String splitJobTableNameSuffix(String fixedTableNamePre, String tableName) {
		String jobTableNameSuffix = StringUtils.substringAfter(tableName, fixedTableNamePre);
		return StringUtils.remove(jobTableNameSuffix, "_");
	}
}
