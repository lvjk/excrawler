var stompClient = null;
var connected = false;
var connectDelayTime = 1000;
var updateJobDelayTime =1000;
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
		$('#job_search').find('#pageIndex').val(0);
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
		  $('#job_search').find('#pageIndex').val(0);
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
		var jobDiv=$('#job_div');
		var table = jobDiv.find('table');
		table.find("tr[class=job]").remove();
		var job;
		var i = 0;
		for (var i = 0; i < jobs.length; i++) {
			job = jobs[i];
			var tr = $("<tr class='job' name='"+job.name+"' title='"+job.describe+"'></tr>");
			$("<input name='name' style='display:none' type='text' value='"+job.name+"' />").appendTo(tr);
			$("<input name='version' style='display:none' type='text' value='"+job.version+"' />").appendTo(tr);
			$("<input name='localNode' style='display:none' type='text' value='"+job.localNode+"' />").appendTo(tr);
			$("<input name='queueName' style='display:none' type='text' value='"+job.queueName+"' />").appendTo(tr);
			$("<input name='isScheduled' style='display:none' type='text' value='"+job.isScheduled+"' />").appendTo(tr);
			$("<td title='"+job.name+"'>"+job.name+"</td>").appendTo(tr);
			var cronTrigger=job.cronTrigger;
			var ctTd=$("<td></td");
			$("<input name='cronTrigger'  flag='"+job.name+"' style='display:none' type='text' value='"+cronTrigger+"' />").appendTo(ctTd);
			$("<input name='update_cronTrigger'  flag='"+job.name+"'  style='display:none' type='text' value='"+cronTrigger+"' />").appendTo(ctTd);
			if(cronTrigger==null||cronTrigger==""||cronTrigger=="null"){
				cronTrigger="#";
			}
			$("<a     name='cronTrigger'  style='color:#FF0000;text-decoration:none;' href=\"javascript:editCronTrigger('"+job.name+"')\">"+cronTrigger+"</a>").appendTo(ctTd);
			ctTd.appendTo(tr);
			
			var scheduledTd = $("<td name='isScheduled'></td>");
			var scheduledOperation=getIsScheduledShowHtml(job.name,job.isScheduled);
			$(scheduledOperation).appendTo(scheduledTd);
			scheduledTd.appendTo(tr);
			
			var nextJobName=job.nextJobName;
			if(nextJobName==null||nextJobName==""||nextJobName=="null"){
				nextJobName="#";
			}
			var nextJobNameTd=$("<td title='"+job.nextJobName+"'></td");
			$("<input name='nextJobName'  flag='"+job.name+"' style='display:none' type='text' value='"+job.nextJobName+"' />").appendTo(nextJobNameTd);
			$("<input name='update_nextJobName'  flag='"+job.name+"'  style='display:none' type='text' value='"+job.nextJobName+"' />").appendTo(nextJobNameTd);
			$("<a     name='nextJobName'  style='color:#FF0000;text-decoration:none;' href=\"javascript:editNextJob('"+job.name+"')\">"+nextJobName+"</a>").appendTo(nextJobNameTd);
			nextJobNameTd.appendTo(tr);
			
			var status=getState(job.status);
			var color=getStateColor(status);
			$("<td name='state' style='font-weight:bold;color:"+color+"'>"+ status + "</td>").appendTo(tr);
			$("<td name='startTime'></td>").appendTo(tr);
			var queueHtml=getQueueHtml(job.queueName,0,0,0);
			$("<td id='" + job_queue_count + job.name + "'>"+queueHtml+"</td>").appendTo(tr);
			$("<td><a href=\"javascript:cleanQueue('"+ job.queueName + "')\">清除</a></td>").appendTo(tr);
			
			$("<td id='" + job_exception_count + job.name + "'>0</td>").appendTo(tr);
			
			
			var td = $("<td id='" + job_operation + job.name + "'></td>");
			var html=masterScheduled.getOperation(job.name,job.state);
			$(html).appendTo(td);
			td.appendTo(tr);
			var otherTd = $("<td></td>");
			var otherOperation="<a href=\"javascript:showHistoryJobSnapshot('" + job.name+ "')\">记录</a>&nbsp;";
			otherOperation += "<a href=\"javascript:showJobInfo('" + job.name+ "')\">详细</a>&nbsp;";
			otherOperation += "<a href=\"/crawler/job/download/profile/"+ job.name+"\"  >下载</a>";
			$(otherOperation).appendTo(otherTd);
			otherTd.appendTo(tr);
			tr.appendTo(table);
		}
		table.find("input[name='update_cronTrigger']").bind('keydown',function(event){  
			  if(event.keyCode == "13"){ 
				  var jobName=$(this).attr("flag");
				  updateCronTrigger(jobName);  
			  }  
		});  
		table.find("input[name='update_nextJobName']").bind('keydown',function(event){  
			  if(event.keyCode == "13"){ 
				  var jobName=$(this).attr("flag");
				  updateNextJobName(jobName);  
			  }  
		}); 
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
			stompClient.subscribe('/topic/job/jobSnapshot', function(responseMsg) {
				var data = JSON.parse(responseMsg.body);
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


/**
 * 动态显示job 运行信息
 * @param jobSnapshots
 * @returns
 */
function showJobSnapshots(jobSnapshots) {
	if(null!=jobSnapshots){
		for (var i = 0; i < jobSnapshots.length; i++) {
			var jobSnapshot = jobSnapshots[i];
			var jobName = jobSnapshot.name;
			var jobTr=$("tr[name='"+jobName+"']");
			var oldStatus=jobTr.find("[name='state']").text();
			var newStatus=getState(jobSnapshot.status);
			//只有当状态发生改变时才更新一下内容
			if(oldStatus!=newStatus){
				var color=getStateColor(newStatus);
				if(jobSnapshot.status==3||jobSnapshot.status==4){
					newStatus="<a  style='color:"+color+"' href=\"javascript:masterScheduled.showWorkerInfo('" + jobName+ "')\">"+newStatus+"</a>";	
				}
				jobTr.find("[name='state']").css("color",color).html(newStatus);
				var startTime=jobSnapshot.startTime;
				var startTimeTd =jobTr.find("[name='startTime']");
				var oldStartTime=startTimeTd.html();
				if(""==oldStartTime&&startTime!=null&&startTime!="null"&&startTime!=""){
					startTime=startTime.substring(5,startTime.length);
					startTimeTd.html(startTime);
				}
				if(""!=oldStartTime&&(startTime==null||startTime=="null"||startTime=="")){
					startTimeTd.html("");
				}
				
				var opetationHtml=masterScheduled.getOperation(jobSnapshot.name,jobSnapshot.status);
				$("#" + job_operation + jobName).html(opetationHtml);
			}
			var queueShowStr=getQueueHtml(jobSnapshot.queueName,jobSnapshot.totalProcessCount,jobSnapshot.realQueueCount,jobSnapshot.errQueueCount);
			$("#" + job_queue_count + jobName).html(queueShowStr);
			$("#" + job_exception_count + jobName).html(jobSnapshot.errCount);	
		}
	}
}

function updateJobInfo() {
	var jobTrs = $('#jobs').find("tr");
	var jobs = new Array();
	var job;
	for (var i = 0; i < jobTrs.length; i++) {
		var jobTr=$(jobTrs[i]);
		job=new Object();
		job.name=jobTr.find("[name='name']").val();
		job.queueName=jobTr.find("[name='queueName']").val();
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



function editCronTrigger(jobName){
	var allTr=$('#jobs').find("tr");
	var tr=$("tr[name='"+jobName+"']");
	var ctInput=tr.find("input[name='cronTrigger']");
	var updateCtInput=tr.find("input[name='update_cronTrigger']");
	var ctA=tr.find("a[name='cronTrigger']");
	var cronTrigger=ctInput.val();
	updateCtInput.val(cronTrigger);
	ctA.css("display","none");
	updateCtInput.css("display","inline");
	updateCtInput.focus();
	liveInput(allTr,updateCtInput,"cronTrigger");
	liveInput(allTr,updateCtInput,"nextJobName");
}

function editNextJob(jobName){
	var allTr=$('#jobs').find("tr");
	var tr=$("tr[name='"+jobName+"']");
	var nextJobInput=tr.find("input[name='nextJobName']");
	var updateNextJobInput=tr.find("input[name='update_nextJobName']");
	var nextJobA=tr.find("a[name='nextJobName']");
	var nextJob=nextJobInput.val();
	updateNextJobInput.val(nextJob);
	updateNextJobInput.css("display","inline");
	nextJobA.css("display","none");
	updateNextJobInput.focus();
	liveInput(allTr,updateNextJobInput,"cronTrigger");
	liveInput(allTr,updateNextJobInput,"nextJobName");
}

function updateCronTrigger(jobName){
	var allTr=$('#jobs').find("tr");
	var tr=$("tr[name='"+jobName+"']");
	var ctInput=tr.find("input[name='cronTrigger']");
	var updateCtInput=tr.find("input[name='update_cronTrigger']");
	var ctA=tr.find("a[name='cronTrigger']");
	var newCronTrigger=updateCtInput.val();
	var oldCronTrigger=ctInput.val();
	if(newCronTrigger!=oldCronTrigger){
		var version=tr.find("input[name='version']").val();
		if (window.confirm("你确定要保存job["+jobName+"]的调度时间["+newCronTrigger+"]")) {
			var url = "/crawler/job/updateCronTrigger";
			$.post(url, {
				version:version,
				name : jobName,
				cronTrigger : newCronTrigger
			}, function(responseMsg) {
				if (responseMsg.isOk == 1) {
					ctInput.val(newCronTrigger);
					updateCtInput.html(newCronTrigger);
					ctA.html(newCronTrigger);
					liveInput(allTr,null,"cronTrigger");
					liveInput(allTr,null,"nextJobName");
					
					var newVersion=responseMsg.data;
					updateVersion(newVersion,tr);
				}
				alert(responseMsg.msg);
			});
		}	
	}
}

function updateNextJobName(jobName){
	var allTr=$('#jobs').find("tr");
	var tr=$("tr[name='"+jobName+"']");
	var nextJobNameInput=tr.find("input[name='nextJobName']");
	var updateNextJobNameInput=tr.find("input[name='update_nextJobName']");
	var nextJobNameA=tr.find("a[name='nextJobName']");
	var newValue=updateNextJobNameInput.val();
	var oldValue=nextJobNameInput.val();
	if(newValue==jobName){
		alert("job["+jobName+"]下个执行任务不等于本身");
		return;
	}
	if(oldValue!=newValue){
		var version=tr.find("input[name='version']").val();
		if (window.confirm("你确定要保存job["+jobName+"]的下个执行任务为["+newValue+"]")) {
			var url = "/crawler/job/updateNextJobName";
			$.post(url, {
				version:version,
				name : jobName,
				nextJobName : newValue
			}, function(responseMsg) {
				if (responseMsg.isOk == 1) {
					nextJobNameInput.val(newValue);
					updateNextJobNameInput.val(newValue);
					if(""==newValue){
						newValue="#";
					}
					nextJobNameA.html(newValue);
					liveInput(allTr,null,"cronTrigger");
					liveInput(allTr,null,"nextJobName");
					var newVersion=responseMsg.data;
					updateVersion(newVersion,tr);
				}
				alert(responseMsg.msg);
			});
		}	
	}
}

function getIsScheduledShowHtml(jobName, isScheduled) {
	var scheduledOperation;
	if (isScheduled == 1) {
		scheduledOperation = "<a style='color:#FF0000' href=\"javascript:updateIsScheduled('"
				+ jobName + "')\">取消</a>&nbsp;";
	} else {
		scheduledOperation = "<a style='color:#227700' href=\"javascript:updateIsScheduled('"
				+ jobName + "')\">开启</a>&nbsp;";
	}
	return scheduledOperation;
}

function updateIsScheduled(jobName) {
	var jobTr = $("tr[name='" + jobName + "']");
	var version = jobTr.find("input[name='version']").val();
	var isScheduledInput = jobTr.find("input[name='isScheduled']");
	var isScheduledTd = jobTr.find("td[name='isScheduled']");
	var isScheduled = isScheduledInput.val();
	if (1 == isScheduled || "1" == isScheduled) {
		msg = '你确定要关闭任务[' + jobName + ']调度吗？';
		isScheduled=0;
	} else {
		msg = '你确定要开启任务[' + jobName + ']调度吗？';
		isScheduled=1
	}
	if (window.confirm(msg)) {
		var url = "/crawler/job/updateIsScheduled";
		$.post(url, {
			version : version,
			name : jobName,
			isScheduled:isScheduled
		}, function(responseMsg) {
			if (responseMsg.isOk == 1) {
				var scheduledOperation =getIsScheduledShowHtml(jobName,
						isScheduled);
				isScheduledTd.html(scheduledOperation);
				isScheduledInput.val(isScheduled);
			}
			updateJobVersion(jobName,responseMsg);
			alert(responseMsg.msg);
		});
	}
}



function showHistoryJobSnapshot(jobName) {
	var url = "/crawler/job/getHistoryJobSnapshot/" + jobName;
	var jobSnapshotDiv = $("#jobSnapshot_div");
	var table=jobSnapshotDiv.find("table");
	table.find("tr[class='jobSnapshot']").remove();
	$.get(url, function(result) {
		var jobSnapshots=result.data;
		jobSnapshotDiv.find("span[id='jobName']").html("任务[<span style='color:#FF0000'>"+jobName+"</span>]运行历史记录");
		for (var i = 0; i < jobSnapshots.length; i++) {
			var jobSnapshot=jobSnapshots[i];
			var tr = $("<tr class='jobSnapshot' name='"+jobSnapshot.id+"' ></tr>");
			$("<input name='version' style='display:none' type='text' value='"+jobSnapshot.version+"' />").appendTo(tr);
			var startTime = new Date(jobSnapshot.startTime);
			var endTime = new Date(jobSnapshot.endTime);
			$("<td>" + startTime.Format("yyyy/MM/dd hh:mm:ss") + "</td>").appendTo(tr);
			$("<td>" + endTime.Format("yyyy/MM/dd hh:mm:ss") + "</td>").appendTo(tr);
			var statusTd=$("<td></td>");
			var status=jobSnapshot.status;
			$("<input name='status'  flag='"+jobSnapshot.id+"' style='display:none' type='text' value='"+status+"' />").appendTo(statusTd);
			$("<input name='update_status'  flag='"+jobSnapshot.id+"' style='display:none' type='text' value='"+status+"' />").appendTo(statusTd);
			$("<a name='status' href=\"javascript:editJobSnapshot('"+jobSnapshot.id+"')\" style='color:#FF0000;font-size: 14px; font-weight: bold;'>" + getState(status) + "</a>").appendTo(statusTd);
			statusTd.appendTo(tr);
			
			$("<td>" + jobSnapshot.avgProcessTime + "</td>").appendTo(tr);
			$("<td>" + jobSnapshot.maxProcessTime + "</td>").appendTo(tr);
			$("<td>" + jobSnapshot.minProcessTime + "</td>").appendTo(tr);
			$("<td>" + jobSnapshot.totalProcessCount + "</td>").appendTo(tr);
			$("<td>" + jobSnapshot.errCount + "</td>").appendTo(tr);
			$("<td>查看异常</td>").appendTo(tr);
			tr.appendTo(table);
		}
		table.find("input[name='update_status']").bind('keydown',function(event){  
			  if(event.keyCode == "13"){ 
				  var jobSnapshotId=$(this).attr("flag");
				  updateJobSnapshotStatus(jobSnapshotId);  
			  }  
		}); 
		showLayer(jobSnapshotDiv);
	});
}

function editJobSnapshot(jobSnapshotId){
	var allTr=$("tr[class=jobSnapshot]");
	var statusInput=$("tr[name='"+jobSnapshotId+"']").find("input[name='status']");
	var updateStatusInput=$("tr[name='"+jobSnapshotId+"']").find("input[name='update_status']");
	var statusA=$("tr[name='"+jobSnapshotId+"']").find("a[name='status']");
	var status=statusInput.val();
	updateStatusInput.val(status);
	updateStatusInput.css("display","inline");
	statusA.css("display","none");
	updateStatusInput.focus();
	liveInput(allTr,updateStatusInput,"status");
	
}

function updateJobSnapshotStatus(jobSnapshotId){
	var allTr=$("tr[class=jobSnapshot]");
	var tr=$("tr[name='"+jobSnapshotId+"']");
	var statusInput=tr.find("input[name='status']");
	var updateStatusInput=tr.find("input[name='update_status']");
	var statusA=tr.find("a[name='status']");
	var newValue=updateStatusInput.val();
	if(newValue<1||newValue>6){
		alert("input status is invalid");
		return;
	}
	var oldValue=statusInput.val();
	if(oldValue!=newValue){
		var version=tr.find("input[name='version']").val();
		if (window.confirm("你确定要保存jobSnapshot["+jobSnapshotId+"]的状态为["+newValue+"]")) {
			var url = "/crawler/job/updateJobSnapshotStatus";
			$.post(url, {
				version:version,
				id : jobSnapshotId,
				status : newValue
			}, function(responseMsg) {
				if (responseMsg.isOk == 1) {
					statusInput.val(newValue);
					updateStatusInput.val(newValue);
					statusA.html(getState(newValue));
					liveInput(allTr,null,"status");
					var newVersion=responseMsg.data;
					updateVersion(newVersion,tr);
				}
				alert(responseMsg.msg);
			});
		}	
	}
}





