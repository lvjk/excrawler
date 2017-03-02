$(function() {
	$("#testExtractPath_bt").click(function() {
		testExtractPath();
	});
	$("#addExtractPath_bt").click(function() {
		addExtractPath();
	});
});

function testExtractPath() {
	var extractPath = new Object();
	extractPath.name = $("#name").val();
	extractPath.siteCode = $("#siteCode").val();
	extractPath.ranking = $("#ranking").val();
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
	var testHtml = $("#testHtml").val();
	var testUrl = $("#testUrl").val();
	var url = "/crawler/extracter/testExtract";
	$.post(url, {
		extractPath : JSON.stringify(extractPath),
		testHtml :testHtml,
		testUrl :testUrl
	}, function(data) {
		alert("Data Loaded: " + data);
	});

}

function addExtractPath() {

}