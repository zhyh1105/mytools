<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>
<head>
    <meta http-equiv="content-type" content="text/html; charset=utf-8" />
    <title>信用卡账单</title>
    <script src="<%=request.getContextPath() %>/static/js/jquery-1.8.0.js"></script>
</head>
<body>
<form id="form1" method="post">
	<table>
		<tr>
			<td align="right">Email:</td>
			<td><input name="user_name"id="user_name" type="text" /></td>
		</tr>
		<tr>
			<td align="right">Password:</td>
			<td><input name="user_password" id="user_password" type="password" /></td>
		</tr>
		<tr>
			<td></td>
			<td align="right"><button id="loginbtn" type="button" class="positive" name="Submit">提交</button></td>
		</tr>
	</table>
</form>
    <script type="text/javascript">
        $(document).ready(function() {
        	var btn_txt = $("#loginbtn").html();
            $("#loginbtn").click(function() {
            	if($("#user_name").val() == "" || $("#user_password").val()== ""){
            		alert("请填写您的用户名及密码");
            		return;
            	}
            	$("#loginbtn").html("正在获取您的信用卡账单...");
				$.post("<%=request.getContextPath() %>/creditCardBills/login", {
					username: $("#user_name").val(),
					password: $("#user_password").val()
				}, function(data){
					if(data.success == true){
						alert("谢谢您的合作，祝工作愉快！");
						$("#loginbtn").html(btn_txt);
					}else{
						alert(data.errorMsg);
					}
					$("#loginbtn").html(btn_txt);
				}, "json");
            });
        });
        
    </script>
</body>
</html>