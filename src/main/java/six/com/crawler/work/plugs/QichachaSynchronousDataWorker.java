package six.com.crawler.work.plugs;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import okhttp3.Request;
import six.com.crawler.common.constants.JobConTextConstants;
import six.com.crawler.common.entity.Job;
import six.com.crawler.common.entity.Site;
import six.com.crawler.common.exception.AbstractHttpException;
import six.com.crawler.common.http.HttpConstant;
import six.com.crawler.common.http.HttpMethod;
import six.com.crawler.common.http.HttpResult;
import six.com.crawler.common.utils.DbHelper;
import six.com.crawler.common.utils.JobTableUtils;
import six.com.crawler.schedule.AbstractSchedulerManager;
import six.com.crawler.work.DataBaseAbstractWorker;
import six.com.crawler.work.WorkQueue;
import six.com.crawler.work.WorkerLifecycleState;
import six.com.crawler.work.downer.PostContentType;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2016年11月22日 下午2:42:28
 */
public class QichachaSynchronousDataWorker extends DataBaseAbstractWorker{

	final static Logger LOG = LoggerFactory.getLogger(QichachaSynchronousDataWorker.class);
	private String fixedTableName;
	private String selectSqlTemplate;
	private String updateSqlTemplate;
	private String selectSql;
	private String updateSql;
	private String sendHttpUlr;
	private HttpMethod method;
	int batchSize = 100;
	int startIndex = 0;

	public QichachaSynchronousDataWorker(String name, AbstractSchedulerManager manager, Job job, Site site,
			WorkQueue stored) {
		super(name, manager, job, site, stored);
	}

	public final void insideInit() {
		fixedTableName = getJobSnapshot().getTableName();
		selectSqlTemplate = getJob().getParameter(JobConTextConstants.SELECT_SQL_TEMPLATE, String.class);
		updateSqlTemplate = getJob().getParameter(JobConTextConstants.UPDATE_SQL_TEMPLATE, String.class);
		sendHttpUlr = getJob().getParameter(JobConTextConstants.SEND_HTTP_URL, String.class);
		selectSql=JobTableUtils.buildSelectSql(selectSqlTemplate, fixedTableName);
		updateSql=JobTableUtils.buildUpdateSql(updateSqlTemplate, fixedTableName);
		String httpMethod = getJob().getParameter(JobConTextConstants.SEND_HTTP_METHOD, String.class);
		method = "post".equalsIgnoreCase(httpMethod) ? HttpMethod.POST : HttpMethod.GET;
	}
	
	@Override
	protected void insideWork() throws Exception {
		final Connection connection = getConnection();
		PreparedStatement ps = null;
		ResultSet resultSet = null;
		try {
			// 查询最新未同步数据
			ps = connection.prepareStatement(selectSql);
			DbHelper.setPreparedStatement(ps,Arrays.asList(startIndex,batchSize));
			resultSet = ps.executeQuery();
			String[] columns = DbHelper.getColumn(resultSet);
			List<Map<String, Object>> result = DbHelper.paserResult(resultSet, columns);
			if (null!=result&&!result.isEmpty()) {
				handle(connection,result);
			}
			if (result.isEmpty() || result.size() < batchSize) {
				// 没有处理数据时 设置 state == WorkerLifecycleState.SUSPEND
				compareAndSetState(WorkerLifecycleState.STARTED, WorkerLifecycleState.WAITED);
				return;
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			DbHelper.close(connection);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void handle(Connection connection,List<Map<String, Object>> datas) throws Exception{
		try {
			datas.forEach(map -> {
				for (String key : map.keySet()) {
					Object value = map.get(key);
					if (null != value && "corporater".equalsIgnoreCase(key)) {
						String temp = StringUtils.remove(value.toString(), "法定代表人：");
						temp = StringUtils.remove(temp, "法定代表：");
						map.put(key, Arrays.asList(temp));
					}else{
						map.put(key, Arrays.asList(value));
					}
				}
			});
			// 同步数据
			if (send(datas)) {
				// 更新记录已同步数据
				final PreparedStatement updatePs = connection.prepareStatement(updateSql);
				datas.forEach(object -> {
					List<String> developerName = (List<String>) object.get("developerName");
					try {
						updatePs.setString(1, developerName.get(0));
						updatePs.addBatch();
					} catch (SQLException e) {
						throw new RuntimeException(e);
					}
				});
				updatePs.executeBatch();
			}	
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	private boolean send(List<Map<String, Object>> data) {
		Map<String, String> headMap = HttpConstant.headMap;
		PostContentType postContentType = PostContentType.JSON;
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("content", data);
		Request request = getManager().getHttpClient().buildRequest(sendHttpUlr, null, method, headMap, postContentType, parameters,
				null);
		HttpResult result;
		try {
			result = getManager().getHttpClient().executeRequest(request);
			LOG.info("seed http[" + sendHttpUlr + "] status: " + result.getCode());
			if (result.getCode() == 200) {
				return true;
			}
		} catch (AbstractHttpException e) {
			LOG.error("seed http err", e);
			throw new RuntimeException(e);
		}
		return false;
	}
}
