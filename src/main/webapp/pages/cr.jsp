<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>征信报告</title>
<script type="text/javascript" src="<%=request.getContextPath()%>/static/js/jquery-1.8.0.js"></script>
</head>
<body>
	<form id="form">
		<input type="hidden" value="" id="txt_captchaNeeded" />
		<input type="hidden" value="" id="txt_randomSmsCodeNeeded"/>
		<div>登录名：<input type="text" name="phone" value="" id="txt_phone" maxlength="11"/></div>
		<div>密码：<input type="text" name="password" value="" id="txt_password" /></div>
		<div style="display: none;" id="div_captcha">验证码：<input type="text" name="rnum" value="" id="txt_rnum"/><img src="" id="img_captcha" alt="验证码" onclick="changeCodeImage()"></img></div>
		<div>查询码：<input type="text" name="tradeCode" value="" id="txt_tradeCode" /></div>
		<div><input type="button" id="btn_submit" value="登录"></div>
	</form>
	
	<script type="text/javascript">
		$(document).ready(function(){
			checkPhoneNumber();
			$("#btn_submit").click(function(){
				login();
			});
		});
		function changeCodeImage(){
			$("#img_captcha").attr("src", "<%=request.getContextPath()%>/creditReport/captcha?" + new Date().valueOf());
		}
		function checkPhoneNumber(phone){
			$.ajax({
				type:"POST",
				url:"<%=request.getContextPath()%>/creditReport/isCaptchaNeeded",
				cache:false,
				dataType:"json",
				success:function(data){
					if(data == true){
						$("#div_captcha").show();
						changeCodeImage();
					}
				},
				error:function(json){
					console.log("error");
				}
			});
		}
	 
		function login(){
			var params = new Object();
			var phone = $("#txt_phone").val();
			if(phone == ""){
				alert("请输登录名");
				return;
			}
			var password = $("#txt_password").val();
			if(password == ""){
				alert("请输密码");
				return;
			}
			var tradeCode = $("#txt_tradeCode").val();
			if(tradeCode == ""){
				alert("请输查询码");
				return;
			}
			var captchaCode = $("#txt_rnum").val();
			params.loginname = phone;
			params.password = password;
			params.tradeCode = tradeCode;
			params.captchaCode = captchaCode;
			console.log("正在登录......");
			$.post("<%=request.getContextPath()%>/creditReport/login", params, function(data){
				if(data == true){
					console.log("征信报告获取成功");
				}else{
					console.log("征信报告获取失败，请重试！！！");
					//changeCodeImage();
				}
			}, "json");
		}
	</script>
</body>
</html>