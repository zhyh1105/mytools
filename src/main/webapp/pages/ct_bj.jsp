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
		<div>验证码：<input type="text" name="rnum" value="" id="txt_rnum"/><img src="<%=request.getContextPath()%>/cdma/rnum" alt="验证码" onclick="changeCodeImage(this)"></img></div>
		<div>验证码：<input type="button" id="btn_submit" value="登录"></div>
	</form>
	
	<div><br></div>
		
	<form id="form1">
		<div>輸入機驗證碼：<input type="text" name="phone" value="" id="txt_random_code"/><input type="button" value="發送" id="btn_send_random_code"/></div>
		<div>验证码：<input type="button" id="btn_getBills" value="獲取賬單"></div>
	</form>
	
	<script type="text/javascript">
		function changeCodeImage(img){
			img.src = "<%=request.getContextPath()%>/cdma/rnum?" + new Date().valueOf();
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
				$.post("<%=request.getContextPath()%>/cdma/login", params, function(data){
					if(data == true){
						console.log("登录成功");
					}else{
						console.log("登录失败");
					}
				}, "json");
			});
			$("#btn_send_random_code").click(function(){
				$.ajax({
					type:"POST",
					url:"<%=request.getContextPath()%>/cdma/sendRandomcode",
					cache:false,
					dataType:"json",
					success:function(json){
						if(json){
							console.log("随机码短信发送成功，请查收。");
						}else{
							console.log("随机码短信发送失敗，请重試。");
						}
					},
					error:function(json){
						alert("对不起，随机码短信发送失败！请稍后重试。");
					}
				});
			});
			
			$("#btn_getBills").click(function(){
				console.log("正在驗證隨機機短信......");
				$.ajax({
					type:"POST",
					url:"<%=request.getContextPath()%>/cdma/validateRandomcode",
					cache:false,
					dataType:"json",
					data:{
						randomCode:$("#txt_random_code").val()
					},
					success:function(json){
						console.log("隨機機短信驗證成功，正在獲取賬單......");
						if(json == true){
							getBills();
						}else{
							console.log("随机码短信验证失敗，请重試。");
						}
					},
					error:function(json){
						console.log("对不起，随机码短信验证失败！请稍后重试。");
					}
				});
			});
		});
		
		function getBills(){
			$.post("<%=request.getContextPath()%>/cdma/bills", {}, function(data){
				if(data == true){
					console.log("获取账单成功");
				}else{
					console.log("获取账单失败");
				}
			}, "json");
		}
	</script>
</body>
</html>