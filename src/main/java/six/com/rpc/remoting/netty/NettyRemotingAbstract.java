package six.com.rpc.remoting.netty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import six.com.crawler.common.configure.SpiderConfigure;

/**
 * @author sixliu E-mail:359852326@qq.com
 * @version 创建时间：2016年8月27日 下午5:30:34 类说明 Server与Client公用抽象类
 */
public abstract class NettyRemotingAbstract {
	static final Logger LOG = LoggerFactory.getLogger(NettyRemotingAbstract.class);

	private SpiderConfigure configure;

	public SpiderConfigure getConfigure() {
		return configure;
	}

	public void setConfigure(SpiderConfigure configure) {
		this.configure = configure;
	}

}
