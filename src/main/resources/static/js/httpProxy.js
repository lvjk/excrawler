$(function() {
	// 加载数据
	loadTable("/crawler/httpPorxy/list", "get", "");
});

function loadTable(url, method, params) {
	if ("get" == method) {
		$.get(url, function(result) {
			var errCode = result.errCode;
			if (null == errCode) {
				showDataTable(result.data);
			}else{
				alert(errCode);
			}
		});
	}
}

function showDataTable(httpProxys){
	var table = $('#httpProxys');
	table.find("tr").remove();
	var httpProxy;
	var i = 0;
	for (var i = 0; i < httpProxys.length; i++) {
		httpProxy = httpProxys[i];
		var tr = $("<tr></tr>");
		var httpProxyHostInputId="httpProxyHost_"+i;
		var httpProxyPortInputId="httpProxyPort_"+i;
		$("<td><input id='"+httpProxyHostInputId+"' type='text' value='"+httpProxy.host+"' /></td>")
				.appendTo(tr);
		$("<td><input id='"+httpProxyPortInputId+"' type='text' value='"+httpProxy.port+"' /></td>")
		.appendTo(tr);
		var httpProxyOperationTd = $("<td></td>");
		var httpProxyOperation= "<a href=\"javascript:testHttpProxy('" + httpProxyHostInputId+ "','" + httpProxyPortInputId+ "')\">测试</a>&nbsp;";
		httpProxyOperation += "<a href=\"javascript:delHttpProxy('" + httpProxy.host+ "','" + httpProxy.port+ "')\">删除</a>&nbsp;";
		$(httpProxyOperation).appendTo(httpProxyOperationTd);
		httpProxyOperationTd.appendTo(tr);
		tr.appendTo(table);
	}
	
	var tr = $("<tr></tr>");
	$("<td><input id='addHttpProxyHostInput' type='text' /></td>").appendTo(tr);
	$("<td><input id='addHttpProxyPortInput' type='text' /></td>").appendTo(tr);
	var httpProxyOperationTd = $("<td></td>");
	var httpProxyOperation= "<a href=\"javascript:addHttpProxy()\">保存</a>&nbsp;";
	$(httpProxyOperation).appendTo(httpProxyOperationTd);
	httpProxyOperationTd.appendTo(tr);
	tr.appendTo(table);
}

function addHttpProxy(){
	var httpProxyHost = $('#addHttpProxyHostInput').val();
	if(""==httpProxyHost){
		alert("HttpProxy host must not be empty");
		return;
	}
	var httpProxyPort = $('#addHttpProxyPortInput').val();
	if(""==httpProxyPort){
		alert("HttpProxy port must not be empty");
		return;
	}
	$.post("/crawler/httpPorxy/add", {
		host : httpProxyHost,
		port : httpProxyPort
	}, function(result) {
		alert(result.msg);
		location.reload(true);
	});
}


function testHttpProxy(httpProxyHostInputId,httpProxyPortInputId){
	var httpProxyHost = $('#'+httpProxyHostInputId).val();
	var httpProxyPort = $('#'+httpProxyPortInputId).val();
	$.post("/crawler/httpPorxy/test", {
		host : httpProxyHost,
		port : httpProxyPort
	}, function(result) {
		alert(result.msg);
		location.reload(true);
	});
}


function delHttpProxy(httpProxyHost,httpProxyPort){
	$.post("/crawler/httpPorxy/del", {
		host : httpProxyHost,
		port : httpProxyPort
	}, function(result) {
		alert(result.msg);
		location.reload(true);
	});
}