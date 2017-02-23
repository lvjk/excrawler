var stompClient = null;
var connected = false;
var connectDelayTime = 1000;
var updateJobDelayTime = 1000;
var job_state = "job_state_";
var job_start_time = "job_start_time_";
var job_end_time = "job_end_time_";
var job_queue_count = "job_queue_count_";
var job_total_process_count = "job_total_process_count_";
var job_total_result_count = "job_total_result_count_";
var job_exception_count = "job_total_process_count_";
var job_avg_process_time = "job_avg_process_time_";
var job_max_process_time = "job_max_process_time_";
var job_min_Process_time = "job_min_Process_time_";
var job_cronTrigger="job_cronTrigger_";
var job_queue_operation="job_queue_operation_";
var job_operation="job_operation_";
var job_scheduled="job_scheduledTd_";

$(function() {
	$("#search_job_bt").click(function(){
		searchJob();
	});
	$("#create_job_bt").click(function(){
		searchJob();
	});
	// 加载数据
	loadTable("/crawler/job/getDefault", "get", "");
});

function loadTable(url, method, params) {
	if ("get" == method) {
		$.get(url, function(result) {
			var errCode = result.errCode;
			if (null == errCode) {
				showJobTable(result.data);
			}
		});
	}
}

function searchJob() {
	var jobName = $('#search_job_name').val();
	if(""==jobName){
		alert("请输入模糊搜索任务名字")
	}else{
		var url="/crawler/job/query/"+jobName;
		loadTable(url, "get", "");
	}
}


function showJobTable(data) {
	var table = $('#jobs');
	table.find("tr").remove();
	var job;
	var i = 0;
	for (var i = 0; i < data.length; i++) {
		job = data[i];
		var tr = $("<tr></tr>");
		$("<td name='jobName' id='"+job.name + "' hostNode='" + job.hostNode + "' queueName='" + job.queueName + "' >" + job.name + "</td>")
				.appendTo(tr);
		var cronTrigger=job.cronTrigger;
		if(cronTrigger==null||cronTrigger==""||cronTrigger=="null"){
			cronTrigger="#";
		}
		$("<td id='" + job_cronTrigger + job.name + "'>"+cronTrigger+"</td>")
		.appendTo(tr);
		$("<td>" + job.level + "</td>")
		.appendTo(tr);
		var state=getState(job.state);
		var color=getStateColor(state);
		$("<td name='jobState' id='"+job_state+ job.name + "' style='font-weight:bold;color:"+color+"'>"
						+ state + "</td>").appendTo(tr);
		$("<td id='" + job_start_time + job.name + "'></td>").appendTo(tr);
		$("<td id='" + job_queue_count + job.name + "'>0/0/0</td>").appendTo(tr);
		$("<td id='" + job_exception_count + job.name + "'>0</td>").appendTo(tr);
		
		
		var queueOperationTd = $("<td id='" + job_queue_operation + job.name + "'></td>");
		var queueOperation= "<a href=\"javascript:repairQueue('" + job.queueName
		+ "')\">修复</a>&nbsp;";
		queueOperation += "<a href=\"javascript:cleanQueue('" + job.queueName
		+ "')\">清除</a>&nbsp;";
		$(queueOperation).appendTo(queueOperationTd);
		queueOperationTd.appendTo(tr);
		
		
		var scheduledTd = $("<td id='" + job_scheduled + job.name + "'></td>");
		var scheduledOperation;
		if (job.isScheduled == 1) {
			scheduledOperation = "<a id='scheduled_job_bt_" + job.name + "' isScheduled='"
			+ job.isScheduled + "' style='color:#FF0000' href=\"javascript:scheduled('"
			+ job.name + "')\">取消</a>&nbsp;";
		}else{
			scheduledOperation = "<a id='scheduled_job_bt_" + job.name + "' isScheduled='"
			+ job.isScheduled + "' style='color:#227700' href=\"javascript:scheduled('"
			+ job.name + "')\">开启</a>&nbsp;";
		}
		$(scheduledOperation).appendTo(scheduledTd);
		scheduledTd.appendTo(tr);
		
		var td = $("<td id='" + job_operation + job.name + "'></td>");
		var html=getOperation(job.state,job.hostNode,job.name);
		$(html).appendTo(td);
		td.appendTo(tr);

		var otherTd = $("<td></td>");
		var otherOperation = "<a  href=\"javascript:showJobRunningRecord('" + job.name
		+ "')\">记录</a>&nbsp;";
		otherOperation += "<a href=\"javascript:showJobInfo('" + job.name
				+ "')\">详细</a>";
		$(otherOperation).appendTo(otherTd);
		otherTd.appendTo(tr);
		
		tr.appendTo(table);
	}
	window.setTimeout(connection, connectDelayTime);
}

function getOperation(state,jobHostNode,jobName){
	var html="";
	if (state== 1) {
		html += "<a id='execute_job_bt_" + jobName
				+ "' href=\"javascript:execute('" + jobHostNode
				+ "','" + jobName
				+ "')\">执行</a>&nbsp;<a  id='suspend_job_bt_" + jobName
				+ "' style='display: none;' href=\"javascript:suspend('" + jobHostNode
				+ "','" + jobName + "')\">暂停</a>&nbsp;<a  id='goon_job_bt_"
				+ jobName
				+ "' style='display: none;' href=\"javascript:goOn('" + jobHostNode
				+ "','" + jobName + "')\">继续</a>&nbsp;<a  id='stop_job_bt_"
				+ jobName
				+ "' style='display: none;' href=\"javascript:stop('" + jobHostNode
				+ "','" +  jobName + "')\">终止</a>&nbsp;";
		/**
		 * 当job.state=0||1 没有调度或者调度状态下 只显示 执行 操作
		 */
	} else if (state == 2) {
		html += "<a id='execute_job_bt_" + jobName
				+ "' href=\"javascript:execute('" + jobHostNode
				+ "','" + jobName
				+ "')\">执行</a>&nbsp;<a  id='suspend_job_bt_" + jobName
				+ "' style='display: none;' href=\"javascript:suspend('" + jobHostNode
				+ "','" + jobName + "')\">暂停</a>&nbsp;<a  id='goon_job_bt_"
				+ jobName
				+ "' style='display: none;' href=\"javascript:goOn('" + jobHostNode
				+ "','" + jobName + "')\">继续</a>&nbsp;<a  id='stop_job_bt_"
				+ jobName
				+ "' style='display: none;' href=\"javascript:stop('" + jobHostNode
				+ "','" +  jobName + "')\">终止</a>&nbsp;";
		/**
		 * 当job.state=3 正在执行状态下 显示 暂停 停止 操作
		 */
	} else if (state == 3) {
		html += "<a id='execute_job_bt_" + jobName
				+ "' style='display: none;' href=\"javascript:execute('" + jobHostNode
				+ "','" + jobName + "')\">执行</a>&nbsp;<a  id='suspend_job_bt_"
				+ jobName + "' href=\"javascript:suspend('" + jobHostNode
				+ "','" + jobName
				+ "')\">暂停</a>&nbsp;<a  id='goon_job_bt_" + jobName
				+ "' style='display: none;' href=\"javascript:goOn('" + jobHostNode
				+ "','" + jobName + "')\">继续</a>&nbsp;<a  id='stop_job_bt_"
				+ jobName + "' href=\"javascript:stop('" + jobHostNode
				+ "','" + jobName
				+ "')\">终止</a>&nbsp;";
		/**
		 * 当job.state=4 正在暂停状态下 显示 继续 停止 操作
		 */
	} else if (state == 4) {
		html += "<a id='execute_job_bt_" + jobName
				+ "' style='display: none;' href=\"javascript:execute('" + jobHostNode
				+ "','" +  jobName + "')\">执行</a>&nbsp;<a  id='suspend_job_bt_"
				+ jobName
				+ "' style='display: none;' href=\"javascript:suspend('" + jobHostNode
				+ "','" + jobName + "')\">暂停</a>&nbsp;<a  id='goon_job_bt_"
				+ jobName + "' href=\"javascript:goOn('" + jobHostNode
				+ "','" +  jobName
				+ "')\">继续</a>&nbsp;<a  id='stop_job_bt_" + jobName
				+ "' href=\"javascript:stop('" + jobHostNode
				+ "','" + jobName
				+ "')\">终止</a>&nbsp;";
	}else{
		html += "<a id='execute_job_bt_" + jobName
		+ "' href=\"javascript:execute('" + jobHostNode
		+ "','" + jobName
		+ "')\">执行</a>&nbsp;<a  id='suspend_job_bt_" + jobName
		+ "' style='display: none;' href=\"javascript:suspend('" + jobHostNode
		+ "','" + jobName + "')\">暂停</a>&nbsp;<a  id='goon_job_bt_"
		+ jobName
		+ "' style='display: none;' href=\"javascript:goOn('" + jobHostNode
		+ "','" + jobName + "')\">继续</a>&nbsp;<a  id='stop_job_bt_"
		+ jobName
		+ "' style='display: none;' href=\"javascript:stop('" + jobHostNode
		+ "','" +  jobName + "')\">终止</a>&nbsp;";
	}
	return html;
}
/**
	READY(1),
	WAITING_EXECUTED(2),
	EXECUTING(3),
	SUSPEND(4),
	STOP(5),
	FINISHED(6);
 */
function getState(state) {
	var stateStr = "准备";
	if (1 == state) {
		stateStr = "准备";
	} else if (2 == state) {
		stateStr = "队列";
	} else if (3 == state) {
		stateStr = "运行";
	} else if (4 == state) {
		stateStr = "暂停";
	}else if (5 == state) {
		stateStr = "停止";
	}else if (6 == state) {
		stateStr = "完成";
	}
	return stateStr;
}
function getStateColor(state){
	var color = "#227700";
	if ("准备" == state) {
		color = "#227700";
	} else if ("队列" == state) {
		color = "#FF8888";
	} else if ("运行" == state) {
		color = "#FF0000";
	} else if ("暂停" == state) {
		color = "#FF3333";
	}
	return color;
}
function connection() {
	if (null == stompClient) {
		var socket = new SockJS('/crawler/websocket');
		stompClient = Stomp.over(socket);
		stompClient.connect({}, function(frame) {
			connected = true;
			stompClient.subscribe('/topic/job/jobSnapshot', function(msg) {
				var data = JSON.parse(msg.body);
				showJobActivitiesInfo(data.data);
			});
		});
	}
	if (connected) {
		window.setTimeout(updateJobInfo, updateJobDelayTime);
	} else {
		window.setTimeout(connection, connectDelayTime);
	}
}

// 对Date的扩展，将 Date 转化为指定格式的String
// 月(M)、日(d)、小时(h)、分(m)、秒(s)、季度(q) 可以用 1-2 个占位符，
// 年(y)可以用 1-4 个占位符，毫秒(S)只能用 1 个占位符(是 1-3 位的数字)
// 例子：
// (new Date()).Format("yyyy-MM-dd hh:mm:ss.S") ==> 2006-07-02 08:09:04.423
// (new Date()).Format("yyyy-M-d h:m:s.S") ==> 2006-7-2 8:9:4.18
Date.prototype.Format = function(fmt){ // author: meizz
	var o = {   
			"M+" : this.getMonth()+1,                 // 月份
			"d+" : this.getDate(),                    // 日
			"h+" : this.getHours(),                   // 小时
			"m+" : this.getMinutes(),                 // 分
			"s+" : this.getSeconds(),                 // 秒
			"q+" : Math.floor((this.getMonth()+3)/3), // 季度
			"S"  : this.getMilliseconds()             // 毫秒
	};   
	if(/(y+)/.test(fmt)){
		fmt=fmt.replace(RegExp.$1, (this.getFullYear()+"").substr(4 - RegExp.$1.length));   
	}   	
	for(var k in o){
		if(new RegExp("("+ k +")").test(fmt)){
			fmt = fmt.replace(RegExp.$1, (RegExp.$1.length==1) ? (o[k]) : (("00"+ o[k]).substr((""+ o[k]).length)));  
		}
	}    
	return fmt;   
}
function showJobActivitiesInfo(infos) {
	for (var i = 0; i < infos.length; i++) {
		var info = infos[i];
		var jobName = info.name;
		var state=getState(info.state);
		var color=getStateColor(state);
		$("#"+job_state + jobName).css("color",color).html(state);
		var startTime = $("#" + job_start_time + jobName);
		if(info.startTime!=null||info.startTime!="null"||info.startTime!=""){
			startTime.html(info.startTime);
		}else{
			startTime.html("");
		}
		var totalProcessCountHtml="<span style='color:#FF0000;font-size: 14px; font-weight: bold;'>"+info.totalProcessCount+"</span>";
		var proxyQueueCountHtml="<span style='color:#227700;font-size: 14px; font-weight: bold;'>"+info.proxyQueueCount+"</span>";
		var realQueueCountCountHtml="<span style='color:#227700;font-size: 14px; font-weight: bold;'>"+info.realQueueCount+"</span>";
		$("#" + job_queue_count + jobName).html(totalProcessCountHtml+"/&nbsp;("+proxyQueueCountHtml+"&nbsp;|&nbsp;"+realQueueCountCountHtml+")");
		$("#" + job_exception_count + jobName).html(info.errCount);
		var opetationHtml=getOperation(info.state,info.hostNode,info.name);
		$("#" + job_operation + jobName).html(opetationHtml);
		
	}
}

function updateJobInfo() {
	var jobTds = $("td[name='jobName']");
	var jobs = new Array();
	var job;
	for (var i = 0; i < jobTds.length; i++) {
		job=new Object();
		job.name=$(jobTds[i]).attr("id");
		job.hostNode=$(jobTds[i]).attr("hostNode");
		job.queueName=$(jobTds[i]).attr("queueName");
		jobs[i] =job;
	}
	var josn=JSON.stringify(jobs);
	try {
		stompClient.send("/crawler/jobSnapshot", {},josn);
	} catch (e) {
		console.log('websocket err: ' + e);
		if (null != stompClient) {
			stompClient.disconnect();
		}
		connected = false;
		window.setTimeout(connection, connectDelayTime);
		return;
	}
	window.setTimeout(updateJobInfo, updateJobDelayTime);
}

function showJobInfo(jobName) {
	var url = "/crawler/job/queryjobinfo/" + jobName;
	$.get(url, function(result) {
		var job_detail_div = $("#job_detail_div");
		var job=result.data.job;
		var jobParameter=result.data.jobParameter;
		var paserItems=result.data.paserItems;
		var eee=job_detail_div.find("[name='name']");
		job_detail_div.find("[name='name']").val(job.name);
		job_detail_div.find("[name='siteCode']").val(job.siteCode);
		job_detail_div.find("[name='queueName']").val(job.queueName);
		job_detail_div.find("[name='workerClass']").val(job.workerClass);
		job_detail_div.find("[name='resultStoreClass']").val(job.resultStoreClass);
		job_detail_div.find("[name='level']").find("option[value='"+job.level+"']").attr("selected",true);
		job_detail_div.find("[name='hostNode']").val(job.hostNode);
		var cronTrigger=job.cronTrigger;
		if(cronTrigger==null||cronTrigger==""||cronTrigger=="null"){
			cronTrigger="";
		}
		job_detail_div.find("[name='triggerTime']").val(cronTrigger);
		job_detail_div.find("[name='needNodes']").find("option[value='"+job.needNodes+"']").attr("selected",true);
		job_detail_div.find("[name='threads']").find("option[value='"+job.threads+"']").attr("selected",true);
		
		layer.open({
			type : 1, // page层
			area : [ '1080px', '600px' ],
			title : null,
			shade : 0.6, // 遮罩透明度
			moveType : 1, // 拖拽风格，0是默认，1是传统拖动
			shift : 1, // 0-6的动画形式，-1不开启
			content : job_detail_div
		});
	});
}

function showJobRunningRecord(jobName) {
	var url = "/crawler/job/activity/history/" + jobName;
	var job_running_records_div = $("#job_running_records_div");
	var job_running_records_table=job_running_records_div.find("table");
	job_running_records_table.find("[name='job_running_record_data']").remove();
	$.get(url, function(result) {
		var jobActivityInfos=result.data;
		job_running_records_div.find("[id='job_running_records_div_name']").html("任务[<span style='color:#FF0000'>"+jobName+"</span>]运行历史记录");
		for (var i = 0; i < jobActivityInfos.length; i++) {
			var info=jobActivityInfos[i];
			var tr = $("<tr name='job_running_record_data'></tr>");
			var startTime = new Date(info.startTime);
			var endTime = new Date(info.endTime);
			$("<td>" + startTime.Format("yyyy/MM/dd hh:mm:ss") + "</td>").appendTo(tr);
			$("<td>" + endTime.Format("yyyy/MM/dd hh:mm:ss") + "</td>").appendTo(tr);
			$("<td>" + info.avgProcessTime + "</td>").appendTo(tr);
			$("<td>" + info.maxProcessTime + "</td>").appendTo(tr);
			$("<td>" + info.minProcessTime + "</td>").appendTo(tr);
			$("<td>" + info.totalProcessCount + "</td>").appendTo(tr);
			$("<td>" + info.exceptionCount + "</td>").appendTo(tr);
			$("<td>查看异常</td>").appendTo(tr);
			tr.appendTo(job_running_records_table);
		}
		layer.open({
			type : 1, // page层
			area : [ '980px', '600px' ],
			title : null,
			shade : 0.6, // 遮罩透明度
			moveType : 1, // 拖拽风格，0是默认，1是传统拖动
			shift : 1, // 0-6的动画形式，-1不开启
			content : job_running_records_div
		});
	});
}





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
		var url = "/crawler/job/scheduled/" + jobName + "/" + isScheduled;
		$.get(url, function(result) {
			if (1 == isScheduled) {
				// 隐藏取消任务调度按钮
				// 显示任务调度按钮
				bt.html("开启调度");
				bt.attr("isScheduled", 0);
				$("#"+job_state + jobName).html("准备");
			} else {
				// 隐藏任务调度按钮
				// 显示取消任务调度按钮
				bt.html("取消调度");
				bt.attr("isScheduled", 1);
				$("#"+job_state + jobName).html("调度中");
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
function execute(jobHostNode,jobName) {
	if (window.confirm('你确定要执行任务[' + jobName + ']吗？')) {
		var url = "/crawler/job/execute/" +jobHostNode+"/"+ jobName;
		$.get(url, function(result) {
			alert(result.data);
			$("#execute_job_bt_" + jobName).hide();
			$("#suspend_job_bt_" + jobName).show();
			$("#goon_job_bt_" + jobName).hide();
			$("#stop_job_bt_" + jobName).show();
			var state="运行";
			var color=getStateColor(state);
			$("#"+job_state + jobName).css("color",color).html(state);
		});
	}
}

/**
 * 暂停后只显示 继续和停止
 * 
 * @param jobName
 * @returns
 */
function suspend(jobHostNode,jobName) {
	if (window.confirm('你确定要暂停任务[' + jobName + ']吗？')) {
		var url = "/crawler/job/suspend/" +jobHostNode+"/"+ jobName;
		$.get(url, function(result) {
			alert(result.data);
			$("#execute_job_bt_" + jobName).hide();
			$("#suspend_job_bt_" + jobName).hide();
			$("#goon_job_bt_" + jobName).show();
			$("#stop_job_bt_" + jobName).show();
			var state="暂停";
			var color=getStateColor(state);
			$("#"+job_state + jobName).css("color",color).html(state);
		});
	}
}

/**
 * goon 后 只显示 暂停 和 停止功能
 * 
 * @param jobName
 * @returns
 */
function goOn(jobHostNode,jobName) {
	if (window.confirm('你确定要继续执行任务[' + jobName + ']吗？')) {
		var url = "/crawler/job/goon/"+jobHostNode+"/"+ jobName;
		$.get(url, function(result) {
			alert(result.data);
			$("#execute_job_bt_" + jobName).hide();
			;
			$("#goon_job_bt_" + jobName).hide();
			$("#suspend_job_bt_" + jobName).show();
			$("#stop_job_bt_" + jobName).show();
			var state="运行";
			var color=getStateColor(state);
			$("#"+job_state + jobName).css("color",color).html(state);
		});
	}
}

/**
 * stop 后只显示 执行 功能
 * 
 * @param jobName
 * @returns
 */
function stop(jobHostNode,jobName) {
	if (window.confirm('你确定要终止任务[' + jobName + ']吗？')) {
		var url = "/crawler/job/stop/" +jobHostNode+"/"+ jobName;
		$.get(url, function(result) {
			alert(result.data);
			$("#execute_job_bt_" + jobName).show();
			;
			$("#goon_job_bt_" + jobName).hide();
			$("#suspend_job_bt_" + jobName).hide();
			$("#stop_job_bt_" + jobName).hide();
			var state="准备";
			var color=getStateColor(state);
			$("#"+job_state + jobName).css("color",color).html(state);
		});
	}
}


function repairQueue(queueName) {
	if (window.confirm("do you repair queue:"+queueName)) {
		var url = "/crawler/job/queue/repair/" + queueName;
		$.get(url, function(result) {
			alert(result.msg);
			location.reload(true);   
		});
	}
}

function cleanQueue(queueName) {
	if (window.confirm("do you clean queue:"+queueName)) {
		var url = "/crawler/job/queue/clean/" + queueName;
		$.get(url, function(result) {
			alert(result.msg);
			location.reload(true); 
		});
	}
}