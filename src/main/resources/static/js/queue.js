
$(function() {
	// 加载数据
	loadTable("/crawler/job/queues", "get", "");
});

function loadTable(url, method, params) {
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
		$("<td name='queueName' id='" + queueInfo.queueName + "' >" + queueInfo.queueName + "</td>")
				.appendTo(tr);
		$("<td>" + queueInfo.proxyQueueSize + "</td>")
		.appendTo(tr);
		$("<td>" + queueInfo.realQueueSize + "</td>")
		.appendTo(tr);
		$("<td><a href=\"javascript:repairQueue('" + queueInfo.queueName
				+ "')\">repair</a>&nbsp;<a href=\"javascript:cleanQueue('"
				+ queueInfo.queueName + "')\">clean</a></td>")
		.appendTo(tr);
		tr.appendTo(table);
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















