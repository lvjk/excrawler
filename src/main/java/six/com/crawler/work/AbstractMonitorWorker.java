package six.com.crawler.work;

import six.com.crawler.entity.JobSnapshot;
import six.com.crawler.work.space.Index;
import six.com.crawler.work.space.WorkSpaceData;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年6月6日 上午10:56:46
 * 
 *       监控任务worker
 * 
 */
public abstract class AbstractMonitorWorker extends AbstractWorker<WorkSpaceData> {

	public AbstractMonitorWorker() {
		super(WorkSpaceData.class);
	}

	@Override
	protected void initWorker(JobSnapshot jobSnapshot) {
		getWorkSpace().push(new WorkSpaceData() {
			@Override
			public void setIndex(Index index) {
			}

			@Override
			public String getKey() {
				return null;
			}

			@Override
			public Index getIndex() {
				return null;
			}
		});
	}
}
