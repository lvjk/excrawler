package six.com.crawler.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2016年9月7日 下午2:30:48
 */
public class DbHelper {
	final static Logger LOG = LoggerFactory.getLogger(DbHelper.class);
	final static String queryTableSql = "select table_name from INFORMATION_SCHEMA.tables";
	static {
		String mysqlDriverName = "com.mysql.jdbc.Driver";
		try {
			Class.forName(mysqlDriverName);
		} catch (ClassNotFoundException e) {
			LOG.error("load class err:" + mysqlDriverName, e);
		} // 指定连接类型
	}

	/**
	 * where TABLE_NAME like ? // tableName
	 * 
	 * @param connection
	 * @param tableName
	 * @return
	 * @throws SQLException
	 */
	public static List<String> queryTableNames(Connection connection, String tableName) throws SQLException {
		List<String> tables = new ArrayList<>();
		String sql = queryTableSql + " where TABLE_NAME like ?";
		PreparedStatement ps = connection.prepareStatement(sql);
		ps.setString(1,tableName);
		ResultSet resultSet = ps.executeQuery();
		while (resultSet.next()) {
			String resultTableName = resultSet.getString("table_name");
			tables.add(resultTableName);
		}
		return tables;
	}


	public static void setPreparedStatement(PreparedStatement ps, List<Object> parameter) throws SQLException {
		if (null != parameter && parameter.size() > 0) {
			int index = 1;
			for (Object ob : parameter) {
				if (ob instanceof String) {
					ps.setString(index++, (String) ob);
				} else if (ob instanceof Integer) {
					ps.setInt(index++, (Integer) ob);
				} else if (ob instanceof Date) {
					java.sql.Date sqlDate = new java.sql.Date(((Date) ob).getTime());
					ps.setDate(index++, sqlDate);
				} else if (ob instanceof java.sql.Date) {
					ps.setDate(index++, ((java.sql.Date) ob));
				}
			}
		}
	}

	public static List<Map<String, Object>> paserResult(ResultSet resultSet, String[] columns) throws SQLException {
		if (null != resultSet) {
			List<Map<String, Object>> result = new ArrayList<>();
			Map<String, Object> object = null;
			while (resultSet.next()) {
				object = new HashMap<String, Object>();
				for (String column : columns) {
					Object value = resultSet.getObject(column);
					value = null == value ? "" : value;
					object.put(column, value);
				}
				result.add(object);
			}
			return result;
		} else {
			throw new NullPointerException("resultSet is null");
		}
	}

	public static String[] getColumn(ResultSet resultSet) throws SQLException {
		if (null != resultSet) {
			ResultSetMetaData rsmd = resultSet.getMetaData();
			int count = rsmd.getColumnCount();
			String[] column = new String[count];
			for (int i = 0; i < count; i++) {
				column[i] = rsmd.getColumnName(i + 1);
			}
			return column;
		} else {
			throw new NullPointerException("resultSet is null");
		}
	}

	/**
	 * 获取mysql 链接
	 * 
	 * @return
	 */
	public static Connection getConnection() {
		String url = "jdbc:mysql://172.18.84.44:3306/test?"
				+ "user=root&password=123456&useUnicode=true&characterEncoding=UTF8";
		String user = "root";
		String pwd = "123456";
		Connection conn = null;
		try {
			conn = DriverManager.getConnection(url, user, pwd);
		} catch (SQLException e) {
			LOG.error("getConnection err:" + url, e);
		} // 获取连接
		return conn;
	}

	/**
	 * 关闭链接
	 * 
	 * @param conn
	 */
	public static void close(Connection conn) {
		if (conn != null) {
			try {
				conn.close();
			} catch (SQLException e) {
				LOG.error("close connection err.", e);
			}
		}
	}
}
