<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>手机账单</title>
<script type="text/javascript" src="<%=request.getContextPath()%>/static/js/jquery-1.8.0.js"></script>
</head>
<body>
	<form id="form">
		<input type="hidden" value="" id="txt_captchaNeeded" />
		<input type="hidden" value="" id="txt_randomSmsCodeNeeded"/>
		<table>
			<tr>
				<td align="right">手机号：</td>
				<td><input type="text" name="phone" value="" id="txt_phone" maxlength="11"/></td>
			</tr>
			<tr>
				<td align="right">密码：</td>
				<td><input type="text" name="password" value="" id="txt_password" /></td>
			</tr>
			<tr style="display: none;" id="div_captcha">
				<td align="right">验证码：</td>
				<td><input type="text" name="rnum" value="" id="txt_rnum"/><img src="" id="img_captcha" alt="验证码" onclick="changeCodeImage()"></img></td>
			</tr>
			<tr>
				<td align="right"> </td>
				<td><input type="button" id="btn_submit" value="登录"></td>
			</tr>
		</table>
	</form>
	
	<div><br></div>
	<form id="form1" style="display: none;">
		<table>
			<tr>
				<td align="right">輸入機驗證碼：</td>
				<td><input type="text" name="phone" value="" id="txt_random_code"/>&nbsp;<input type="button" value="获取" id="btn_send_random_code"/><span id="info_send_random_code"></span></td>
			</tr>
			<tr>
				<td align="right"> </td>
				<td><input type="button" id="btn_getBills" value="獲取賬單"></td>
			</tr>
		</table>
	</form>
	
	<script type="text/javascript">
		$(document).ready(function(){
			$("#txt_phone").blur(function(){
				checkPhoneNumber($("#txt_phone").val());
			});
			
			$("#btn_send_random_code").click(function(){
				sendRandomcode();
			});
		});
		function changeCodeImage(){
			$("#img_captcha").attr("src", "<%=request.getContextPath()%>/mobile/captcha?" + new Date().valueOf());
		}
		function checkPhoneNumber(phone){
			if(phone && phone.length == 11){
				mobileReset();
				$.ajax({
					type:"POST",
					url:"<%=request.getContextPath()%>/mobile/checkPhoneNumber?phone=" + phone,
					cache:false,
					dataType:"json",
					success:function(mobileInfo){
						if(mobileInfo.validate == true){
							console.log("是合法的手机号码......");
							console.log("是否需要验证码:" + mobileInfo.captchaNeeded);
							console.log("是否需要随机短信码:" + mobileInfo.randomSmsCodeNeeded);
							$("#txt_captchaNeeded").val(mobileInfo.captchaNeeded);
							$("#txt_randomSmsCodeNeeded").val(mobileInfo.randomSmsCodeNeeded);
							$("#btn_submit").unbind("click").click(function(){
								login();
							});
							$("#btn_getBills").unbind("click").click(function(){
								validateRandomcode();
							});
							if(mobileInfo.captchaNeeded == true){//需要验证码
								$("#div_captcha").show();
								changeCodeImage();
							}else{
								$("#div_captcha").hide();
							}
						}else{
							console.log("不是合法的手机号码!!!");
							mobileReset();
						}
					},
					error:function(json){
						alert("对不起，手机号码检查失败！请稍后重试。");
						mobileReset();
					}
				});
			}
		}
		function mobileReset(){
			$("#div_captcha").hide();
			$("#form1").hide();
			$("#btn_submit").unbind("click");
			$("#btn_getBills").unbind("click");
		}
		function login(){
			var params = new Object();
			var phone = $("#txt_phone").val();
			if(phone == ""){
				alert("请输入手机号");
				return;
			}
			var password = $("#txt_password").val();
			if(password == ""){
				alert("请输服务密码");
				return;
			}
			var rnum = $("#txt_rnum").val();
			if($("#txt_captchaNeeded").val() == "true"){
				if(rnum == ""){
					alert("请输服务密码");
					return;
				}	
			}
			params.phone = phone;
			params.password = password;
			params.rnum = rnum;
			console.log("正在登录......");
			$.post("<%=request.getContextPath()%>/mobile/login", params, function(data){
				if(data == true){
					console.log("登录成功");
					var randomSmsCodeNeeded = $("#txt_randomSmsCodeNeeded").val();
					if(randomSmsCodeNeeded == "true"){//需要随机验证码
						$("#form1").show();
						//sendRandomcode();
					}else{//不需要，直接获取账单
						getBills();
					}
				}else{
					console.log("登录失败");
				}
			}, "json");
		}
		function sendRandomcode(){
			$.ajax({
				type:"POST",
				url:"<%=request.getContextPath()%>/mobile/sendRandomcode",
				cache:false,
				dataType:"json",
				success:function(json){
					if(json){
						console.log("随机码短信发送成功，请查收。");
						$("#info_send_random_code").text("随机短信码已发送到您的手机上，请注意查收");
					}else{
						console.log("随机码短信发送失敗，请重試。");
					}
				},
				error:function(json){
					alert("对不起，随机码短信发送失败！请稍后重试。");
				}
			});
		}
		function validateRandomcode(){
			console.log("正在驗證隨機機短信......");
			$.ajax({
				type:"POST",
				url:"<%=request.getContextPath()%>/mobile/validateRandomcode",
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
		}
		function getBills(){
			$.post("<%=request.getContextPath()%>/mobile/bills", {}, function(data){
				if(data == true){
					console.log("获取账单成功");
				}else{
					console.log("获取账单失败");
				}
				mobileReset();
			}, "json");
		}
	</script>
</body>
</html>