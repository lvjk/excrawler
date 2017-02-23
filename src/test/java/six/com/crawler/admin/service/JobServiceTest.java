package six.com.crawler.admin.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.junit.Test;

import six.com.crawler.BaseTest;
import six.com.crawler.common.DateFormats;
import six.com.crawler.common.entity.JobSnapshot;
import six.com.crawler.common.entity.JobSnapshotState;
import six.com.crawler.common.entity.WorkerErrMsg;
import six.com.crawler.common.entity.WorkerSnapshot;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2016年9月8日 下午5:18:02
 */
public class JobServiceTest extends BaseTest {

	int count = 5;

	@Test
	public void test() {
		saveJobSnapshot();
		// Node currentNode = jobWorkerManager.getCurrentNode();
		// String nodeName = currentNode.getName();
		// Map<String, Object> parameters = new HashMap<>();
		// parameters.put("hostNode", nodeName);
		// parameters.put("isScheduled", 0);
		// List<Job> list=jobService.query(parameters);
		// System.out.println(list);
	}

	public void saveJobSnapshot() {
		String jobName = "test_name";
		String jobSnapshotid = jobName + "_" + System.currentTimeMillis();
		String hostNodeName = "test_hostNode";
		JobSnapshot jobSnapshot = buildJobSnapshot(jobSnapshotid, jobName, hostNodeName);
		jobService.registerJobSnapshotToRegisterCenter(jobSnapshot);
		jobService.reportJobSnapshot(hostNodeName,jobName);
		jobService.delJobSnapshotFromRegisterCenter(hostNodeName, jobName);
	}


	public void queryJobSnapshot() {
		String jobName = "test_name";
		List<JobSnapshot> result = jobService.queryJobSnapshotsFromHistory(jobName);
		for (JobSnapshot JobSnapshot : result) {
			LOG.info(JobSnapshot.toString());
		}
		LOG.info("query size:" + result.size());
	}

	protected WorkerSnapshot buildWorkerSnapshot(String jobSnapshotid, String jobName, String workerName,
			String hostNodeName) {
		WorkerSnapshot workerSnapshot = new WorkerSnapshot();
		workerSnapshot.setJobSnapshotId(jobSnapshotid);
		workerSnapshot.setJobName(jobName);
		workerSnapshot.setName(workerName);
		workerSnapshot.setJobHostNode(hostNodeName);
		workerSnapshot.setStartTime(DateFormatUtils.format(System.currentTimeMillis(), DateFormats.DATE_FORMAT_1));
		workerSnapshot.setEndTime(DateFormatUtils.format(System.currentTimeMillis(), DateFormats.DATE_FORMAT_1));
		workerSnapshot.setTotalProcessCount(50);
		workerSnapshot.setTotalResultCount(45);
		workerSnapshot.setMaxProcessTime(500);
		workerSnapshot.setMinProcessTime(100);
		workerSnapshot.setAvgProcessTime(300);
		workerSnapshot.setErrCount(5);
		List<WorkerErrMsg> workerErrMsgs = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			WorkerErrMsg workerErrMsg=new WorkerErrMsg();
			workerErrMsg.setJobSnapshotId(jobSnapshotid);
			workerErrMsg.setJobName(jobName);
			workerErrMsg.setWorkerName(workerName);
			workerErrMsg.setStartTime(DateFormatUtils.format(System.currentTimeMillis(), DateFormats.DATE_FORMAT_1));
			workerErrMsg.setMsg("test err msg");
			workerErrMsgs.add(workerErrMsg);
		}
		workerSnapshot.setWorkerErrMsgs(workerErrMsgs);
		return workerSnapshot;
	}

	protected JobSnapshot buildJobSnapshot(String jobSnapshotid, String jobName, String hostNodeName) {
		String workerName = "test_workerName_" + jobName;
		JobSnapshot jobSnapshot = new JobSnapshot();
		jobSnapshot.setId(jobName + "_" + System.currentTimeMillis());
		jobSnapshot.setName(jobName);
		jobSnapshot.setHostNode(hostNodeName);
		jobSnapshot.setState(JobSnapshotState.READY.value());
		jobSnapshot.setStartTime(DateFormatUtils.format(System.currentTimeMillis(), DateFormats.DATE_FORMAT_1));
		jobSnapshot.setEndTime(DateFormatUtils.format(System.currentTimeMillis(), DateFormats.DATE_FORMAT_1));
		jobSnapshot.setTotalProcessCount(50);
		jobSnapshot.setTotalResultCount(45);
		jobSnapshot.setMaxProcessTime(500);
		jobSnapshot.setMinProcessTime(100);
		jobSnapshot.setAvgProcessTime(300);
		jobSnapshot.setErrCount(5);
		List<WorkerSnapshot> WorkerSnapshots = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			WorkerSnapshot workerSnapshot = buildWorkerSnapshot(jobSnapshotid, jobName, workerName + "_"+i,
					hostNodeName);
			WorkerSnapshots.add(workerSnapshot);
		}
		jobSnapshot.setWorkerSnapshots(WorkerSnapshots);
		return jobSnapshot;
	}
}
