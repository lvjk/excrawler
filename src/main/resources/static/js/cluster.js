$(function() {
	nodesTable('#nodes',"http://192.168.44.75:8080/crawler/node/getall","get","");
	// 编辑表格
});

function nodesTable(tableid,url,method,params){
	if("get"==method){
		$.get(url, function(result){
			var errCode=result.errCode;
			if(null==errCode){
				var data=result.data;
				var table=$(tableid);
				var node;
				for(var i=0;i<data.length;i++){
					node=data[i];
					var tr=$("<tr></tr>");
					$("<td>"+node.clusterName+"</td>").appendTo(tr);
					$("<td>"+node.nodeName+"</td>").appendTo(tr);
					$("<td>"+node.ip+"</td>").appendTo(tr);
					$("<td>"+node.port+"</td>").appendTo(tr);
					$("<td>"+node.cpu+"</td>").appendTo(tr);
					$("<td>"+node.mem+"</td>").appendTo(tr);
					$("<td>"+node.status+"</td>").appendTo(tr);
					$("<td>"+node.user+"</td>").appendTo(tr);
					$("<td>"+node.passwd+"</td>").appendTo(tr);
					tr.appendTo(table);
				}
			}
		  });
	}
}