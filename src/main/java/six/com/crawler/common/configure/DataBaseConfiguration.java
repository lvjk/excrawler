package six.com.crawler.common.configure;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.bind.RelaxedPropertyResolver;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.alibaba.druid.pool.DruidDataSource;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2016年9月8日 下午4:47:43
 */
@Configuration
@EnableTransactionManagement
public class DataBaseConfiguration implements EnvironmentAware {

	private static Logger LOG = LoggerFactory.getLogger(DataBaseConfiguration.class);

	private RelaxedPropertyResolver propertyResolver;

	@Override
	public void setEnvironment(Environment env) {
		this.propertyResolver = new RelaxedPropertyResolver(env, "jdbc.");
	}

	@Bean(name = "dataSource", destroyMethod = "close", initMethod = "init")
	@Primary
	public DataSource initDataSource() {
		LOG.debug("Configruing dataSource");
		DruidDataSource datasource = new DruidDataSource();
		String jdbcUrl=propertyResolver.getProperty("url");
		String driverClassName=propertyResolver.getProperty("driver");
		String username=propertyResolver.getProperty("username");
		String password=propertyResolver.getProperty("password");
		datasource.setUrl(jdbcUrl);
		datasource.setDriverClassName(driverClassName);
		datasource.setUsername(username);
		datasource.setPassword(password);
		datasource.setMaxActive(10);
		return datasource;
	}
}