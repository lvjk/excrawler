$(function() {
	$("#testExtractPath_bt").click(function() {
		testExtractPath();
	});
	$("#addExtractPath_bt").click(function() {
		addExtractPath();
	});
	$("#search_bt").click(function() {
		var siteCode=$("#search_siteCode").val();
		if(null==siteCode||siteCode==""){
			alert("请输入搜索站点code");
		}else{
			showExtractPathTable(siteCode);
		}
	});
});

function showExtractPathTable(siteCode){
	var url = "/crawler/extracter/getExtractPaths/"+siteCode;
	$.get(url, function(result) {
		var extractPaths=result.data;
		if(null!=extractPaths&&extractPaths.length>0){
			var extractPath_div = $("#extractPath_div");
			var extractPath_div_table=extractPath_div.find("table");
			extractPath_div_table.find("[name='extractPath']").remove();
			extractPath_div.find("[id='siteCode']").html("站点[<span style='color:#FF0000'>"+siteCode+"</span>]抽取路径");
			for(var i=0;i<extractPaths.length;i++){
				var extractPath = extractPaths[i];
				var tr = $("<tr name='extractPath' title='"+extractPath.describe+"'></tr>");
				$("<td><span style='font-size:10px;'>" + extractPath.name + "</span></td>").appendTo(tr);
				$("<td><span style='font-size:6px;'>" + extractPath.ranking + "</span></td>").appendTo(tr);
				$("<td><span style='font-size:6px;'>" + extractPath.path + "</span></td>").appendTo(tr);
				$("<td><span style='font-size:6px;'>" + extractPath.filterPath + "</span></td>").appendTo(tr);
				$("<td><span style='font-size:6px;'>" + extractPath.extractAttName + "</span></td>").appendTo(tr);
				$("<td><span style='font-size:6px;'>" + extractPath.substringStart + "</span></td>").appendTo(tr);
				$("<td><span style='font-size:6px;'>" + extractPath.substringEnd + "</span></td>").appendTo(tr);
				$("<td><span style='font-size:6px;'>" + extractPath.compareAttName + "</span></td>").appendTo(tr);
				$("<td><span style='font-size:6px;'>" + extractPath.containKeyWord + "</span></td>").appendTo(tr);
				$("<td><span style='font-size:6px;'>" + extractPath.replaceWord + "</span></td>").appendTo(tr);
				$("<td><span style='font-size:6px;'>" + extractPath.replaceValue + "</span></td>").appendTo(tr);
				$("<td><span style='font-size:6px;'>" + extractPath.appendHead + "</span></td>").appendTo(tr);
				$("<td><span style='font-size:6px;'>" + extractPath.appendEnd + "</span></td>").appendTo(tr);
				$("<td><span style='font-size:6px;'>" + extractPath.extractEmptyCount + "</span></td>").appendTo(tr);
				tr.appendTo(extractPath_div_table);
			}
		}else{
			alert("没有查到此站点的抽取路径")
		}
	});
}

function validExtractPath(){
	var extractPath=new Object();
	extractPath.name = $("#name").val();
	extractPath.siteCode = $("#siteCode").val();
	extractPath.ranking = $("#ranking").val();
	if(null==extractPath.ranking||""==extractPath.ranking){
		extractPath.ranking=-1;
	}
	extractPath.path = $("#path").val();
	extractPath.filterPath = $("#filterPath").val();
	extractPath.extractAttName = $("#extractAttName").val();
	extractPath.substringStart = $("#substringStart").val();
	extractPath.substringEnd = $("#substringEnd").val();
	extractPath.compareAttName = $("#compareAttName").val();
	extractPath.containKeyWord = $("#containKeyWord").val();
	extractPath.replaceWord = $("#replaceWord").val();
	extractPath.replaceValue = $("#replaceValue").val();
	extractPath.appendHead = $("#appendHead").val();
	extractPath.appendEnd = $("#appendEnd").val();
	extractPath.describe = $("#describe").val();
	extractPath.testHtml = $("#testHtml").val();
	extractPath.testUrl = $("#testUrl").val();
	return extractPath;
}

function testExtractPath() {
	var extractPath=validExtractPath();
	if(null!=extractPath){
		var url = "/crawler/extracter/testExtract";
		$.post(url, {
			name:extractPath.name,
			siteCode:extractPath.siteCode,
			ranking:extractPath.ranking,
			path:extractPath.path,
			filterPath:extractPath.filterPath,
			extractAttName:extractPath.extractAttName,
			substringStart:extractPath.substringStart,
			substringEnd:extractPath.substringEnd,
			compareAttName:extractPath.compareAttName,
			containKeyWord:extractPath.containKeyWord,
			replaceWord:extractPath.replaceWord,
			replaceValue:extractPath.replaceValue,
			appendHead:extractPath.appendHead,
			appendEnd:extractPath.appendEnd,
			describe:extractPath.describe,
			testUrl:extractPath.testUrl,
			testHtml:extractPath.testHtml
		}, function(data) {
			showTestExtractPathResult(data.data);
		});
	}
}

function showTestExtractPathResult(testExtractPathResults){
	if(null!=testExtractPathResults&&testExtractPathResults.length>0){
		var show_div = $("#test_extractPath_result_div");
		var show_div_table=show_div.find("table");
		show_div_table.find("[name='test_extractPath_result']").remove();
		for (var i = 0; i < testExtractPathResults.length; i++) {
			var testExtractPathResult=testExtractPathResults[i];
			var tr = $("<tr name='test_extractPath_result'></tr>");
			$("<td><span style='color:#FF0000;font-size:16px;'>" + testExtractPathResult + "</span></td>").appendTo(tr);
			tr.appendTo(show_div_table);
		}
		showLayer(show_div);
	}
}

function addExtractPath() {

}