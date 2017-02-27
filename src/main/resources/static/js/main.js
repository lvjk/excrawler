var map = {};
map["节点信息"] = "/html/node.html";
map["节点新增"] = "/html/node.html";
map["任务信息"] = "/html/job.html";
map["已处理数据"] = "/html/done.html";
map["任务新增"] = "/html/job_add.html";
map["站点管理"] = "/html/site.html";
map["HTTP代理管理"] = "/html/httpProxy.html";
//map["配置管理"] = "/html/configs.html";
//map["日志管理"] = "/html/logs.html";
$(function() {
	showSystemDate();
	$(".menu-a").click(function() {
		var key = $(this).text();
		var url = map[key];
		if(null!=url){
			$("#content-iframe").attr("src", url);
		}
	});
	// 编辑表格
});

function showSystemDate() {
	var now = new Date();
	var year=now.getFullYear();
	var month=now.getMonth()+1;//月份从0开始
	var day = now.getDate();
	var hours = now.getHours();
	var minutes = now.getMinutes();
	var seconds = now.getSeconds();
	var date= "" + year + "年" + month + "月" + day + "日 "
	+ hours + ":" + minutes + ":" + seconds + "";
	$(".system-data").text(date);
	var timeID = setTimeout(showSystemDate, 1000);
}