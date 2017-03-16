package six.com.crawler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.alibaba.druid.pool.DruidDataSource;

import six.com.crawler.common.utils.DbHelper;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月16日 上午10:15:03
 */
public class TableTest {

	public static void main(String[] args) {
		String dbUrl = "";
		String dbUser = "";
		String dbPasswd = "";
		String dbDriverClassName = "";
		DruidDataSource datasource = new DruidDataSource();
		datasource.setUrl(dbUrl);
		datasource.setDriverClassName(dbDriverClassName);
		datasource.setUsername(dbUser);
		datasource.setPassword(dbPasswd);
		datasource.setMaxActive(1);
		String sql = "select * from ex_dc_tmsf_house_info_20170314193037 order by collectionDate desc limit ?,?";
		Connection connection = null;
		try {
			connection = datasource.getConnection();
			PreparedStatement ps = null;
			int start = 0;
			int size = 1000;
			Map<String,Map<String, Object>> maps = new HashMap<>();
			String[] columns = null;
			try {
				ps = connection.prepareStatement(sql);
				ps.setInt(1,start);
				ps.setInt(2,size);
				ResultSet resultSet = ps.executeQuery();
				if (null == columns) {
					columns = DbHelper.getColumn(resultSet);
				}
				while (resultSet.next()) {
					Map<String, Object> map = new HashMap<>();
					for (String column : columns) {
						Object value = resultSet.getObject(column);
						map.put(column, value);
					}
					String houseId=(String)map.get("houseId");
					if(maps.containsKey(houseId)){
						Date collectionDate=(Date)map.get("collectionDate");
					}else{
						maps.put(houseId, map);
					}
				}

			} catch (SQLException e) {
			} finally {
				DbHelper.close(connection);
			}

		} catch (SQLException e) {

		} finally {
			DbHelper.close(connection);
		}
	}
	
	public boolean save(Connection connection,Map<String, String> map) throws SQLException{
		String sql="";
		String houseId=map.get("houseId");
		PreparedStatement ps = connection.prepareStatement(sql);
		return false;
	}

}
