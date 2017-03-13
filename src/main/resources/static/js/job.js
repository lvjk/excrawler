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
	$("#upload_jobProfile").click(function() {
		uploadJobProfile();
	});
	$("#search_job_name").bind('keydown',function(event){  
	  if(event.keyCode == "13"){  
		  searchJob();
	  }  
	});  
	// 加载数据
	searchJob();
});

function uploadJobProfile(url,file) {
	var url="/crawler/job/upload/profile";
	uploadFile(url,$("#jobProfile")[0].files[0]);
}


function searchJob() {
	var jobName = $('#search_job_name').val();
	if(null==jobName||""==jobName){
		jobName="*";
	}
	var jobSearch=$('#job_search');
	var pageIndex=jobSearch.find('#pageIndex').val();
	var pageSize=jobSearch.find('#pageSize').val();
	var url="/crawler/job/query/"+pageIndex+"/"+pageSize+"/"+jobName;
	$.get(url, function(result) {
		showJobTable(result);
	});
}

function nextSearchJob() {
	var jobSearch=$('#job_search');
	var pageIndex=jobSearch.find('#pageIndex').val();
	pageIndex=parseInt(pageIndex);
	pageIndex=pageIndex+1;
	jobSearch.find('#pageIndex').val(pageIndex)
	searchJob();
}

function lastSearchJob() {
	var jobSearch=$('#job_search');
	var pageIndex=jobSearch.find('#pageIndex').val();
	pageIndex=parseInt(pageIndex);
	pageIndex=pageIndex-1;
	jobSearch.find('#pageIndex').val(pageIndex)
	searchJob();
}

function showJobTable(result) {
	var jobs=result.data.list;
	if(null!=jobs&&jobs.length>0){
		var pageIndex=result.data.pageIndex;
		var pageSize=result.data.pageSize;
		var totalPage=result.data.totalPage;
		var totalSize=result.data.totalSize;
		var table = $('#jobs');
		table.find("tr").remove();
		var job;
		var i = 0;
		for (var i = 0; i < jobs.length; i++) {
			job = jobs[i];
			var tr = $("<tr title='"+job.describe+"'></tr>");
			$("<td name='jobName' " +
					"id='"+job.name + "' " +
					"hostNode='"+job.localNode + "' " +
					"queueName='" + job.queueName + "'>" + job.name + "</td>")
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
			$("<td name='jobState' id='"+job_state+ job.name + "' style='font-weight:bold;color:"+color+"'>"+ state + "</td>").appendTo(tr);
			$("<td id='" + job_start_time + job.name + "'></td>").appendTo(tr);
			var queueHtml=getQueueHtml(job.queueName,0,0,0);
			$("<td id='" + job_queue_count + job.name + "'>"+queueHtml+"</td>").appendTo(tr);
			$("<td><a href=\"javascript:cleanQueue('"+ job.queueName + "')\">清除</a></td>").appendTo(tr);
			$("<td id='" + job_exception_count + job.name + "'>0</td>").appendTo(tr);
			var scheduledTd = $("<td id='" + job_scheduled + job.name + "'></td>");
			var scheduledOperation;
			if (job.isScheduled == 1) {
				scheduledOperation = "<a id='scheduled_job_bt_" + job.name + "' isScheduled='"
				+ job.isScheduled + "' style='color:#FF0000' href=\"javascript:scheduled('"
				+ job.name + "')\">取消调度</a>&nbsp;";
			}else{
				scheduledOperation = "<a id='scheduled_job_bt_" + job.name + "' isScheduled='"
				+ job.isScheduled + "' style='color:#227700' href=\"javascript:scheduled('"
				+ job.name + "')\">开启调度</a>&nbsp;";
			}
			$(scheduledOperation).appendTo(scheduledTd);
			scheduledTd.appendTo(tr);
			var td = $("<td id='" + job_operation + job.name + "'></td>");
			var html=getScheduledOperation(job.state,job.name);
			$(html).appendTo(td);
			td.appendTo(tr);

			var otherTd = $("<td></td>");
			var otherOperation = "<a  href=\"javascript:showHistoryJobSnapshot('" + job.name
			+ "')\">记录</a>&nbsp;";
			otherOperation += "<a href=\"javascript:showJobInfo('" + job.name
					+ "')\">详细</a>&nbsp;";
			otherOperation += "<a href=\"/crawler/job/download/profile/"+ job.name+"\"  >下载</a>";
			$(otherOperation).appendTo(otherTd);
			otherTd.appendTo(tr);
			tr.appendTo(table);
		}
		$('#pageInfo').html(pageInfo(totalPage,totalSize));
		window.setTimeout(connection, connectDelayTime);
	}
}

function pageInfo(totalPage,totalSize){
	var jobSearch=$('#job_search');
	var pageIndex=jobSearch.find('#pageIndex').val();
	var pageSize=jobSearch.find('#pageSize').val();
	jobSearch.find('#totalPage').val(totalPage);
	jobSearch.find('#totalSize').val(totalSize);
	var pageInfoHtml="<span>";
	pageInfoHtml+="总共<span style='color:#FF0000;font-weight:bold'>"+totalSize+"</span>条记录";
	pageInfoHtml+="每页<span style='color:#FF0000;font-weight:bold'>"+pageSize+"</span>条";
	pageInfoHtml+="总页数:<span style='color:#FF0000;font-weight:bold'>"+totalPage+"</span>";
	pageInfoHtml+="当前页:<span style='color:#FF0000;font-weight:bold'>"+(parseInt(pageIndex)+1)+"</span>";
	pageInfoHtml+="</span>";
	if(totalPage>1){
		if(pageIndex>=totalPage-1){
			pageInfoHtml+="<a class='pull-right' style='color:#FF0000;font-weight:bold' href='javascript:lastSearchJob()'>上一页</a>";
		}else if(pageIndex==0){
			pageInfoHtml+="<a class='pull-right' style='color:#FF0000;font-weight:bold' href='javascript:nextSearchJob()'>下一页</a>";
		}else{
			pageInfoHtml+="<a class='pull-right' style='color:#FF0000;font-weight:bold' href='javascript:nextSearchJob()'>下一页</a>";
			pageInfoHtml+="<a class='pull-right' style='color:#FF0000;font-weight:bold' href='javascript:lastSearchJob()'>上一页&nbsp;&nbsp;&nbsp;&nbsp;</a>";
		}
	}
	return pageInfoHtml;
}

function getScheduledOperation(state,jobName){
	var html="";
	var executeDisplay="inline";
	var suspendDisplay="none";
	var goOnDisplay="none";
	var stopDisplay="none";
	if (state== 1) {// 当job.state=1 没有调度或者调度状态下 只显示 执行 操作
		executeDisplay="inline";
		suspendDisplay="none";
		goOnDisplay="none";
		stopDisplay="none";
	}if (state == 2) {// 当job.state=2 处于等待被执行 不显示任何操作
		executeDisplay="none";
		suspendDisplay="none";
		goOnDisplay="none";
		stopDisplay="none";
	}else if (state == 3) { // 当job.state=3 正在执行状态下 显示 暂停 停止 操作
		executeDisplay="none";
		suspendDisplay="inline";
		goOnDisplay="none";
		stopDisplay="inline";
	} else if (state == 4) {// 当job.state=4 正在暂停状态下 显示 继续 停止 操作
		executeDisplay="none";
		suspendDisplay="none";
		goOnDisplay="inline";
		stopDisplay="inline";
	}
	html +="<a   id='execute_job_bt_" + jobName+ "' style='display:"+executeDisplay+";' href=\"javascript:execute('"+ jobName+ "')\">执行</a>&nbsp;" 
			+"<a id='suspend_job_bt_" + jobName+ "' style='display: "+suspendDisplay+";' href=\"javascript:suspend('"+ jobName + "')\">暂停</a>&nbsp;" 
			+"<a id='goon_job_bt_"+ jobName+ "'     style='display: "+goOnDisplay+";' href=\"javascript:goOn('"+ jobName + "')\">继续</a>&nbsp;" 
			+"<a id='stop_job_bt_"+ jobName+ "'     style='display: "+stopDisplay+";' href=\"javascript:stop('" +jobName + "')\">终止</a>&nbsp;";
	return html;
}
/**
 * READY(1), WAITING_EXECUTED(2), EXECUTING(3), SUSPEND(4), STOP(5),
 * FINISHED(6);
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
				showJobSnapshots(data.data);
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

function getQueueHtml(queueName,totalProcessCount,queueCountCount,errQueueCount){
	var totalProcessCountHtml="<span title='任务处理数量' style='color:#FF0000;font-size: 14px; font-weight: bold;'>"+totalProcessCount+"</span>";
	var realQueueCountCountHtml="0";
	if(queueCountCount>0){
		realQueueCountCountHtml="<a  title='任务队列数量' href=\"javascript:showQueueInfo('" +queueName+ "')\">" 
				+"<span style='color:#227700;font-size:14px; font-weight: bold;'>"+queueCountCount+"</span>"
				+"</a>&nbsp;";
	}else{
		realQueueCountCountHtml="<span title='任务队列数量' style='color:#227700;font-size: 14px; font-weight: bold;'>"+queueCountCount+"</span>";
	}
	var errQueueCountHtml="0";
	if(errQueueCount>0){
		errQueueCountHtml="<a  title='任务错误队列数量' href=\"javascript:showErrQueueInfo('" + queueName+ "')\">" 
				+"<span style='color:#FF0000;font-size: 14px; font-weight: bold;'>"+errQueueCount+"</span>"
				+"</a>&nbsp;";
	}else{
		errQueueCountHtml="<span title='任务错误队列数量' style='color:#FF0000;font-size: 14px; font-weight: bold;'>"+errQueueCount+"</span>&nbsp;";
	}
	var queueShowStr=totalProcessCountHtml+"/&nbsp;("+realQueueCountCountHtml+"&nbsp;|&nbsp;"+errQueueCountHtml+")";
	return queueShowStr;
}


function showJobSnapshots(jobSnapshots) {
	if(null!=jobSnapshots){
		for (var i = 0; i < jobSnapshots.length; i++) {
			var jobSnapshot = jobSnapshots[i];
			var jobName = jobSnapshot.name;
			var state=getState(jobSnapshot.state);
			var color=getStateColor(state);
			if(jobSnapshot.state==3||jobSnapshot.state==4){
				state="<a  style='color:"+color+"' href=\"javascript:showWorkerInfo('" + jobName+ "')\">"+state+"</a>";
				$("#"+job_state + jobName).html(state);
			}else{
				$("#"+job_state + jobName).css("color",color).html(state);
			}
			var startTime = $("#" + job_start_time + jobName);
			if(jobSnapshot.startTime!=null||jobSnapshot.startTime!="null"||jobSnapshot.startTime!=""){
				startTime.html(jobSnapshot.startTime);
			}else{
				startTime.html("");
			}
			var queueShowStr=getQueueHtml(jobSnapshot.queueName,jobSnapshot.totalProcessCount,jobSnapshot.realQueueCount,jobSnapshot.errQueueCount);
			$("#" + job_queue_count + jobName).html(queueShowStr);
			$("#" + job_exception_count + jobName).html(jobSnapshot.errCount);
			var opetationHtml=getScheduledOperation(jobSnapshot.state,jobSnapshot.name);
			$("#" + job_operation + jobName).html(opetationHtml);
			
		}
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
		showLayer(job_detail_div);
	});
}

function showHistoryJobSnapshot(jobName) {
	var url = "/crawler/job/getHistoryJobSnapshot/" + jobName;
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
			$("<td><span style='color:#FF0000;font-size: 14px; font-weight: bold;'>" + getState(info.state) + "</span></td>").appendTo(tr);
			$("<td>" + info.avgProcessTime + "</td>").appendTo(tr);
			$("<td>" + info.maxProcessTime + "</td>").appendTo(tr);
			$("<td>" + info.minProcessTime + "</td>").appendTo(tr);
			$("<td>" + info.totalProcessCount + "</td>").appendTo(tr);
			$("<td>" + info.errCount + "</td>").appendTo(tr);
			$("<td>查看异常</td>").appendTo(tr);
			tr.appendTo(job_running_records_table);
		}
		showLayer(job_running_records_div);
	});
}



