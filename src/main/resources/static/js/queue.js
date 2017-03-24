function loadQueueTable(url, method, params) {
	if ("get" == method) {
		$.get(url, function(result) {
			var errCode = result.errCode;
			if (null == errCode) {
				showQueueTable(result.data);
			}
		});
	}
}

function showQueueTable(data) {
	var table = $('#queues');
	table.find("tr").remove();
	var queueInfo;
	var i = 0;
	for (var i = 0; i < data.length; i++) {
		queueInfo = data[i];
		var tr = $("<tr></tr>");
		$(
				"<td name='queueName' id='" + queueInfo.queueName + "' >"
						+ queueInfo.queueName + "</td>").appendTo(tr);
		$("<td>" + queueInfo.proxyQueueSize + "</td>").appendTo(tr);
		$("<td>" + queueInfo.realQueueSize + "</td>").appendTo(tr);
		$(
				"<td><a href=\"javascript:repairQueue('"
						+ queueInfo.queueName
						+ "')\">repair</a>&nbsp;<a href=\"javascript:cleanQueue('"
						+ queueInfo.queueName + "')\">clean</a></td>")
				.appendTo(tr);
		tr.appendTo(table);
	}
}

function showQueueInfo(queueName) {
	var queueCursor = $("#queueCursor").val();
	var url = "/crawler/queue/getQueueInfo/" + queueName + "/" + queueCursor;
	var job_queue_div = $("#job_queue_div");
	var job_queue_table = job_queue_div.find("table");
	$.get(url, function(result) {
		var dataMap = result.data;
		var queueCursor = dataMap.queueCursor;
		var queueData = dataMap.list;
		if (null != queueData && queueData.length > 0) {
			$("#queueCursor").val(queueCursor);
			job_queue_table.find("[name='job_queue_data']").remove();
			job_queue_div.find("[id='job_queue_div_name']").html(
					"队列[<span style='color:#FF0000'>" + queueName
							+ "</span>]信息");
			for (var i = 0; i < queueData.length; i++) {
				var page = queueData[i];
				var tr = $("<tr name='job_queue_data'></tr>");
				$(
						"<td><span style='color:#FF0000;font-size:6px;'>"
								+ page.originalUrl + "</span></td>").appendTo(
						tr);

				var operationTd = $("<td></td>");
				var operation = "<a  href=\"javascript:delErrQueue('"
						+ page.pageKey + "')\">删除</a>&nbsp;";
				operation += "<a href=\"javascript:fillQueue('" + page.pageKey
						+ "')\">处理</a>&nbsp;";
				$(operation).appendTo(operationTd);
				operationTd.appendTo(tr);
				tr.appendTo(job_queue_table);
			}
			showLayer(job_queue_div);
		}
	});
}

function showErrQueueInfo(queueName) {
	var errQueueIndex = $("#errQueueIndex").val();
	var url = "/crawler/queue/getErrQueueInfo/" + queueName + "/"
			+ errQueueIndex;
	var job_queue_div = $("#job_queue_div");
	var job_queue_table = job_queue_div.find("table");
	$.get(url, function(result) {
		var queueData = result.data;
		if (null != queueData && queueData.length > 0) {
			job_queue_table.find("[name='job_queue_data']").remove();
			job_queue_div.find("[id='job_queue_div_name']").html(
					"异常队列[<span style='color:#FF0000'>" + queueName
							+ "</span>]信息");
			for (var i = 0; i < queueData.length; i++) {
				var page = queueData[i];
				var tr = $("<tr name='job_queue_data'></tr>");
				$(
						"<td><span style='color:#FF0000;font-size:6px;'>"
								+ page.originalUrl + "</span></td>").appendTo(
						tr);

				var operationTd = $("<td></td>");
				var operation = "<a  href=\"javascript:delErrQueue('"
						+ page.pageKey + "')\">删除</a>&nbsp;";
				operation += "<a href=\"javascript:againDoErrQueue('"+queueName+"')\">全部处理</a>&nbsp;";
				$(operation).appendTo(operationTd);
				operationTd.appendTo(tr);
				tr.appendTo(job_queue_table);
			}
			showLayer(job_queue_div);
		}
	});
}

function delErrQueue(pageKey) {
	var url = "/crawler/queue/delErrQueue/" + pageKey;
	$.get(url, function(result) {
		alert(result.msg);
	});
}

function doErrQueue(pageKey) {

}

function repairQueue(queueName) {
	if (window.confirm("do you repair queue:" + queueName)) {
		var url = "/crawler/queue/repairQueue/" + queueName;
		$.get(url, function(result) {
			alert(result.msg);
		});
	}
}

function cleanQueue(queueName) {
	if (window.confirm("do you clean queue:" + queueName)) {
		var url = "/crawler/queue/cleanQueue/" + queueName;
		$.get(url, function(result) {
			alert(result.msg);
		});
	}
}

function againDoErrQueue(queueName) {
	if (window.confirm("do you againDoErrQueue queue:" + queueName)) {
		var url = "/crawler/queue/againDoErrQueue/" + queueName;
		$.get(url, function(result) {
			alert(result.msg);
		});
	}
}
