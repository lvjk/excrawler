$(function() {
	$("#upload_siteProfile").click(function() {
		uploadSiteProfile();
	});
	// 加载数据
	loadTable();
});

function uploadSiteProfile() {
	var url="/crawler/site/upload/profile";
	var formData = new FormData();
	formData.append("file", $("#siteProfile")[0].files[0]);
	$.ajax({
		url : url,
		type : 'POST',
		data : formData,
		// 告诉jQuery不要去处理发送的数据
		processData : false,
		// 告诉jQuery不要去设置Content-Type请求头
		contentType : false,
		beforeSend : function() {
			console.log("正在进行，请稍候");
		},
		success : function(responseStr) {
			alert(responseStr.msg);
		},
		error : function(responseStr) {
			console.log("error");
		}
	});
}

function loadTable() {
	var url = "/crawler/site/query/1/0";
	$.get(url, function(result) {
		var errCode = result.errCode;
		if (null == errCode) {
			showJobTable(result.data);
		}
	});
}

function showJobTable(data) {
	var table = $('#sites');
	table.find("tr").remove();
	var site;
	var i = 0;
	for (var i = 0; i < data.length; i++) {
		site = data[i];
		var tr = $("<tr></tr>");
		$("<td>" + site.code + "</td>").appendTo(tr);
		$("<td>" + site.mainUrl + "</td>").appendTo(tr);
		$("<td>" + site.describe + "</td>").appendTo(tr);
		var siteOperationTd = $("<td></td>");
		var siteOperation = "<a href=\"/crawler/site/download/profile/"
				+ site.code + "\"  >下载配置</a>";
		$(siteOperation).appendTo(siteOperationTd);
		siteOperationTd.appendTo(tr);
		tr.appendTo(table);
	}
}
