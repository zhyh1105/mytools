<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>获取账单</title>
<script type="text/javascript" src="<%=request.getContextPath()%>/static/js/jquery-1.8.0.js"></script>
</head>
<body>
	<form id="form">
		<div>手机号：<input type="text" name="phone" value="" id="txt_phone"/></div>
		<div>密码：<input type="text" name="password" value="" id="txt_password"/></div>
		<div><input type="button" id="btn_submit" value="提交"></div>
	</form>
	<script type="text/javascript">
		function changeCodeImage(img){
			img.src = "<%=request.getContextPath()%>/cm/rnum?" + new Date().valueOf();
		}
		$(document).ready(function(){
			$("#btn_submit").click(function(){
				var params = new Object();
				$("#form input[type='text']").each(function(){
					var value = $(this).val();
					if(value == ""){
						alert("一个都不能少！！！");
						return;
					}
					params[$(this).attr("name")] = value;
				});
				$.post("<%=request.getContextPath()%>/cu/bills", params, function(data){
					if(data == true){
						alert("获取账单成功");
					}else{
						alert("获取账单失败");
					}
				}, "json");
			});
		});
	</script>
</body>
</html>