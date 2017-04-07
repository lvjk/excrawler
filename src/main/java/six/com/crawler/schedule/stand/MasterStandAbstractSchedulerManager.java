package six.com.crawler.schedule.stand;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import six.com.crawler.schedule.AbstractSchedulerManager;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月28日 下午3:38:15
 * 
 *       备份主节点调度管理 监控和记录master 调度的所有操作，当master调度挂掉，随时切换为master调度 暂时未实现
 */
public abstract class MasterStandAbstractSchedulerManager extends AbstractSchedulerManager {

	final static Logger log = LoggerFactory.getLogger(MasterStandAbstractSchedulerManager.class);

}
