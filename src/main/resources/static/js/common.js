function showLayer(content){
	layer.open({
		type : 1, // page层
		area : [ '1200px', '800px' ],
		title : null,
		shade : 0.6, // 遮罩透明度
		moveType : 1, // 拖拽风格，0是默认，1是传统拖动
		shift : 1, // 0-6的动画形式，-1不开启
		content : content
	});
}

function checkStr(str){
	if(null==str||"null"==str){
		return "";
	}else{
		return str;
	}
}

function uploadFile(url, file) {
	var formData = new FormData();
	formData.append("file", file);
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