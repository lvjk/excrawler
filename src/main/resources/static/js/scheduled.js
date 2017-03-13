function scheduled(jobName) {
	var bt = $("#scheduled_job_bt_" + jobName);
	var isScheduled = bt.attr("isScheduled");
	var msg;
	if (1 == isScheduled) {
		msg = '你确定要关闭任务[' + jobName + ']调度吗？';
	} else {
		msg = '你确定要开启任务[' + jobName + ']调度吗？';
	}
	if (window.confirm(msg)) {
		var url = "/crawler/scheduled/scheduled/" + jobName + "/" + isScheduled;
		$.get(url, function(result) {
			if (1 == isScheduled) {
				// 隐藏取消任务调度按钮
				// 显示任务调度按钮
				bt.html("开启调度");
				bt.attr("isScheduled", 0);
			} else {
				// 隐藏任务调度按钮
				// 显示取消任务调度按钮
				bt.html("取消调度");
				bt.attr("isScheduled", 1);
			}
		});
	}
}

/**
 * 执行后只显示 暂停和停止
 * 
 * @param jobName
 * @returns
 */
function execute(jobName) {
	if (window.confirm('你确定要执行任务[' + jobName + ']吗？')) {
		var url = "/crawler/scheduled/execute/"+ jobName;
		$.get(url, function(result) {
			$("#execute_job_bt_" + jobName).hide();
			$("#suspend_job_bt_" + jobName).show();
			$("#goon_job_bt_" + jobName).hide();
			$("#stop_job_bt_" + jobName).show();
			var state="运行";
			var color=getStateColor(state);
			$("#"+job_state + jobName).css("color",color).html(state);
			alert(result.msg);
		});
	}
}

/**
 * 暂停后只显示 继续和停止
 * 
 * @param jobName
 * @returns
 */
function suspend(jobName) {
	if (window.confirm('你确定要暂停任务[' + jobName + ']吗？')) {
		var url = "/crawler/scheduled/suspend/"+jobName;
		$.get(url, function(result) {
			$("#execute_job_bt_" + jobName).hide();
			$("#suspend_job_bt_" + jobName).hide();
			$("#goon_job_bt_" + jobName).show();
			$("#stop_job_bt_" + jobName).show();
			var state="暂停";
			var color=getStateColor(state);
			$("#"+job_state + jobName).css("color",color).html(state);
			alert(result.msg);
		});
	}
}

/**
 * goon 后 只显示 暂停 和 停止功能
 * 
 * @param jobName
 * @returns
 */
function goOn(jobName) {
	if (window.confirm('你确定要继续执行任务[' + jobName + ']吗？')) {
		var url = "/crawler/scheduled/goon/"+jobName;
		$.get(url, function(result) {
			$("#execute_job_bt_" + jobName).hide();
			$("#goon_job_bt_" + jobName).hide();
			$("#suspend_job_bt_" + jobName).show();
			$("#stop_job_bt_" + jobName).show();
			var state="运行";
			var color=getStateColor(state);
			$("#"+job_state + jobName).css("color",color).html(state);
			alert(result.msg);
		});
	}
}
/**
 * stop 后只显示 执行 功能
 * 
 * @param jobName
 * @returns
 */
function stop(jobName) {
	if (window.confirm('你确定要终止任务[' + jobName + ']吗？')) {
		var url = "/crawler/scheduled/stop/"+jobName;
		$.get(url, function(result) {
			$("#execute_job_bt_" + jobName).show();
			$("#goon_job_bt_" + jobName).hide();
			$("#suspend_job_bt_" + jobName).hide();
			$("#stop_job_bt_" + jobName).hide();
			var state="准备";
			var color=getStateColor(state);
			$("#"+job_state + jobName).css("color",color).html(state);
			alert(result.msg);
		});
	}
}

function showWorkerInfo(jobName){
	var url = "/crawler/scheduled/getWorkerInfo/" + jobName;
	var job_worker_div = $("#job_worker_div");
	var job_worker_div_table=job_worker_div.find("table");
	$.get(url, function(result) {
		var workerSnapshots=result.data;
		if(null!=workerSnapshots&&workerSnapshots.length>0){
			job_worker_div_table.find("[name='job_worker']").remove();
			job_worker_div.find("[id='job_worker_div_name']").html("任务[<span style='color:#FF0000'>"+jobName+"</span>]worker信息");
			for (var i = 0; i < workerSnapshots.length; i++) {
				var workerSnapshot=workerSnapshots[i];
				var tr = $("<tr name='job_worker'></tr>");
				$("<td><span style='color:#FF0000;font-size:16px;'>" + workerSnapshot.name + "</span></td>").appendTo(tr);
				$("<td><span>" + workerSnapshot.localNode + "</span></td>").appendTo(tr);
				var state=getWorkerStateText(workerSnapshot.state);
				$("<td><span>" + state + "</span></td>").appendTo(tr);
				$("<td><span>" + workerSnapshot.startTime + "</span></td>").appendTo(tr);
				$("<td><span>" + workerSnapshot.endTime + "</span></td>").appendTo(tr);
				$("<td><span>" + workerSnapshot.totalProcessCount + "</span></td>").appendTo(tr);
				$("<td><span>" + workerSnapshot.totalResultCount + "</span></td>").appendTo(tr);
				$("<td><span>" + workerSnapshot.totalProcessTime + "</span></td>").appendTo(tr);
				$("<td><span>" + workerSnapshot.maxProcessTime + "</span></td>").appendTo(tr);
				$("<td><span>" + workerSnapshot.minProcessTime + "</span></td>").appendTo(tr);
				$("<td><span>" + workerSnapshot.avgProcessTime + "</span></td>").appendTo(tr);
				$("<td><span>" + workerSnapshot.errCount + "</span></td>").appendTo(tr);
				tr.appendTo(job_worker_div_table);
			}
			showLayer(job_worker_div);
		}
	});
}

function getWorkerStateText(state){
	var stateText;
	if("READY"==state){
		stateText="准备";
	}else if("STARTED"==state){
		stateText="运行";
	}else if("WAITED"==state){
		stateText="等待";
	}else if("SUSPEND"==state){
		stateText="暂停";
	}else if("STOPED"==state){
		stateText="停止";
	}else if("FINISHED"==state){
		stateText="完成";
	}else if("DESTROY"==state){
		stateText="销毁";
	}
	return stateText;
}
