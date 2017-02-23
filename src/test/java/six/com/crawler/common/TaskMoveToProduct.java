package six.com.crawler.common;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.alibaba.druid.pool.DruidDataSource;

import six.com.crawler.common.utils.DbHelper;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2016年12月15日 上午9:57:21
 */
public class TaskMoveToProduct {

	public static void main(String[] args) {
		String dbDriverClassName = "com.mysql.jdbc.Driver";
		String testDbUrl = "jdbc:mysql://172.18.84.44:3306/test?user=root&password=123456&useUnicode=true&characterEncoding=UTF8&useSSL=false";
		String testDbUser = "root";
		String testDbPasswd = "123456";
		DruidDataSource testDatasource = new DruidDataSource();
		testDatasource.setUrl(testDbUrl);
		testDatasource.setDriverClassName(dbDriverClassName);
		testDatasource.setUsername(testDbUser);
		testDatasource.setPassword(testDbPasswd);
		testDatasource.setMaxActive(1);

		String dbUrl = "jdbc:mysql://172.30.103.80:3306/excra_meta?user=excrawler&password=Aa123456&useUnicode=true&characterEncoding=UTF8&useSSL=false";
		String dbUser = "excrawler";
		String dbPasswd = "Aa123456";

		DruidDataSource datasource = new DruidDataSource();
		datasource.setUrl(dbUrl);
		datasource.setDriverClassName(dbDriverClassName);
		datasource.setUsername(dbUser);
		datasource.setPassword(dbPasswd);
		datasource.setMaxActive(1);
		Connection srcConn = null;
		Connection targetConn = null;
		
		List<MoveInfo> moveInfoList=buildMoveBaseInfo("tjfdc");
		moveInfoList.addAll(
				buildMoveJobInfo("tjfdc_project_url",
						"tjfdc_project_info",
						"tjfdc_presale_info",
						"tjfdc_building_info",
						"tjfdc_house_state",
						"tjfdc_house_info"));
		try {
			srcConn = testDatasource.getConnection();
			targetConn = datasource.getConnection();
			doMove(srcConn, targetConn, moveInfoList);
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DbHelper.close(srcConn);
			DbHelper.close(targetConn);
			testDatasource.close();
			datasource.close();
		}
	}
	
	public static List<MoveInfo> buildMoveJobInfo(String... jobNames) {
		List<MoveInfo> moveInfoList = new ArrayList<TaskMoveToProduct.MoveInfo>();
		for(String jobName:jobNames){
			// job移動信息
			MoveInfo jobMoveInfo = new MoveInfo();
			jobMoveInfo.selectSql = "select `name`,siteCode,hostNode,level,workerClass,"
					+ "needNodes,everyProcessDelayTime,queueName,user,"
					+ "resultStoreClass,`state`,isScheduled,cronTrigger,`describe`"
					+ "  from ex_crawler_platform_job where name=?";
			jobMoveInfo.insertSql = "insert into ex_crawler_platform_job" + "(`name`,siteCode,hostNode,level,workerClass,"
					+ "needNodes,everyProcessDelayTime,queueName,user,"
					+ "resultStoreClass,`state`,isScheduled,cronTrigger,`describe`) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
			jobMoveInfo.selectParameters = Arrays.asList(jobName);
			moveInfoList.add(jobMoveInfo);
			// job參數移動信息
			MoveInfo jobParameterMoveInfo = new MoveInfo();
			jobParameterMoveInfo.selectSql = "select jobName,attName,attValue"
					+ "  from ex_crawler_platform_job_parameter where jobName=?";
			jobParameterMoveInfo.insertSql = "insert into ex_crawler_platform_job_parameter"
					+ "(jobName,attName,attValue) VALUES(?,?,?)";
			jobParameterMoveInfo.selectParameters = Arrays.asList(jobName);
			moveInfoList.add(jobParameterMoveInfo);
			// job解析組件移動信息
			MoveInfo jobPaserComponentMoveInfo = new MoveInfo();
			jobPaserComponentMoveInfo.selectSql = "select `name`,jobName,serialNub,output,`type`,"
					+ "paserClassName,resultKey,mustHaveResult,depth,`describe`,pageType "
					+ "  from ex_crawler_platform_paser_component where jobName=?";
			jobPaserComponentMoveInfo.insertSql = "insert into ex_crawler_platform_paser_component"
					+ "(`name`,jobName,serialNub,output,`type`,paserClassName,"
					+ "resultKey,mustHaveResult,depth,`describe`,pageType) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
			jobPaserComponentMoveInfo.selectParameters = Arrays.asList(jobName);
			moveInfoList.add(jobPaserComponentMoveInfo);
		}
		return moveInfoList;
	
	}

	public static List<MoveInfo> buildMoveBaseInfo(String site) {
		List<MoveInfo> moveInfoList = new ArrayList<TaskMoveToProduct.MoveInfo>();
		// 站點移動信息
		MoveInfo siteMoveInfo = new MoveInfo();
		siteMoveInfo.selectSql = "select `code`,mainurl,proxy_enable,localAddress_enable,downerType,`describe`"
				+ "  from ex_crawler_platform_site where `code`=?";
		siteMoveInfo.insertSql = "insert into ex_crawler_platform_site"
				+ "(`code`,mainurl,proxy_enable,localAddress_enable,downerType,`describe`) VALUES(?,?,?,?,?,?)";
		siteMoveInfo.selectParameters = Arrays.asList(site);
		moveInfoList.add(siteMoveInfo);

		// seedPage移動信息
		MoveInfo seedPageMoveInfo = new MoveInfo();
		seedPageMoveInfo.selectSql = "select siteCode,urlMd5,meta,`update`,depth,"
				+ "`state`,originalUrl,firstUrl,finalUrl,ancestorUrl,referer,pageNum,charset,downerType,waitJsLoadElement,`type`"
				+ "  from ex_crawler_platform_seed_page where siteCode=?";
		seedPageMoveInfo.insertSql = "insert into ex_crawler_platform_seed_page"
				+ "(siteCode,urlMd5,meta,`update`,depth,`state`,originalUrl,"
				+ "firstUrl,finalUrl,ancestorUrl,referer,pageNum,charset,downerType,waitJsLoadElement,`type`) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		seedPageMoveInfo.selectParameters = Arrays.asList(site);
		moveInfoList.add(seedPageMoveInfo);

		// 解析path移動信息
		MoveInfo pathMoveInfo = new MoveInfo();
		pathMoveInfo.selectSql = "select `name`,siteCode,ranking,`type`,path,filterPath,reslutAttName,appendHead,"
				+ "appendEnd,containKeyWord,replaceWord,replaceValue,depth,emptyExtractCount,`describe`,compareAttName"
				+ "  from ex_crawler_platform_paser_path where siteCode=?";
		pathMoveInfo.insertSql = "insert into ex_crawler_platform_paser_path"
				+ "(`name`,siteCode,ranking,`type`,path,filterPath,reslutAttName,appendHead,appendEnd,"
				+ "containKeyWord,replaceWord,replaceValue,depth,emptyExtractCount,"
				+ "`describe`,compareAttName) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		pathMoveInfo.selectParameters = Arrays.asList(site);
		moveInfoList.add(pathMoveInfo);
		return moveInfoList;
	}
	/**
	 * 查詢sql 的字段必須跟 查詢sql 的字段 順序和數量一致
	 * 
	 * @param srcConn
	 *            数据源conn
	 * @param targetConn
	 *            移动的目标conn
	 * @param moveInfo
	 *            數據移動信息
	 * @throws SQLException
	 */
	public static void doMove(Connection srcConn, Connection targetConn, MoveInfo moveInfo){
		PreparedStatement selectPreparedStatement;
		try {
			selectPreparedStatement = srcConn.prepareStatement(moveInfo.selectSql);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		PreparedStatement insertPreparedStatement;
		try {
			insertPreparedStatement = targetConn.prepareStatement(moveInfo.insertSql);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		
		try {
			int selectIndex = 1;
			for (Object parameter : moveInfo.selectParameters) {
				selectPreparedStatement.setObject(selectIndex++, parameter);
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		
		ResultSet resultSet;
		try {
			resultSet = selectPreparedStatement.executeQuery();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		String[] columns;
		try {
			columns = DbHelper.getColumn(resultSet);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		try {
			while (resultSet.next()) {
				try {
					int index = 1;
					for (String column : columns) {
						Object value = resultSet.getObject(column);
						insertPreparedStatement.setObject(index++, value);
					}
					insertPreparedStatement.executeUpdate();
				} catch (SQLException e) {
					if(e.getMessage().contains("Duplicate entry")){
						System.out.println(e);
					}else{
						throw new RuntimeException(e);
					}
				}
				
				
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public static void doMove(Connection srcConn, Connection targetConn, List<MoveInfo> moveInfos) throws SQLException {
		if(null!=moveInfos){
			for (MoveInfo moveInfo : moveInfos) {
				doMove(srcConn, targetConn, moveInfo);
			}
		}
	}
	
	/**
	 * 查詢sql 的字段必須跟 查詢sql 的字段 順序和數量一致
	 * @author six
	 * @email  359852326@qq.com
	 */
	static class MoveInfo {
		/**
		 * 查询sql
		 */
		String selectSql;
		/**
		 * 插入sql
		 */
		String insertSql;
		/**
		 * 查询参数
		 */
		List<Object> selectParameters;
	}

}
