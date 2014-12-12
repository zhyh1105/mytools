<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>
<head>
    <meta http-equiv="content-type" content="text/html; charset=utf-8" />
    <link rel="shortcut icon" type="image/ico" href="<%=request.getContextPath() %>/login/images/favicon.ico" />
    <title>Login</title>
    <link href="<%=request.getContextPath() %>/login/styles.css" type="text/css" media="screen" rel="stylesheet" />
    <link href="<%=request.getContextPath() %>/login/jquery-ui-1.8.16.custom.css" rel="stylesheet"/>
    <script src="<%=request.getContextPath() %>/static/js/jquery-1.8.0.js"></script>
    <script src="<%=request.getContextPath() %>/login/jquery-ui-1.8.16.custom.min.js"></script>
    <script type="text/javascript" src="<%=request.getContextPath() %>/login/jquery.keyboard.extension-typing.js"></script>
    <link type="text/css" href="<%=request.getContextPath() %>/login/keyboard.css" rel="stylesheet" />
    <script type="text/javascript" src="<%=request.getContextPath() %>/login/jquery.keyboard.js"></script>

</head>
<body id="login">
    <div id="wrappertop">
    </div>
    <div id="wrapper">
        <div id="content">
            <div id="header">
                <h1>
                    <a href="">
                       <!--����Logo <img src="logo.png"   height="50"  width="100"  alt="logo">--></a></h1>
            </div>
            <div id="darkbanner" class="banner320">
                <h2>
                    Login  System</h2>
            </div>
            <div id="darkbannerwrap">
            </div>
            <form name="form1" id="form1" method="post" action="../login">
            <fieldset class="form">
                <p>
                    <label class="loginlabel" for="user_name">
                        Email:</label>
                    <input class="logininput ui-keyboard-input ui-widget-content ui-corner-all" name="user_name"
                        id="user_name" type="text" value="" />
                </p>
                <p>
                    <label class="loginlabel" for="user_password">
                        Password:</label>
                    <span>
                        <input class="logininput"   name="user_password" id="user_password" type="password" /><img
                            id="passwd" class="tooltip" alt="Click to open the virtual keyboard" title="Click to open the virtual keyboard"
                            src="<%=request.getContextPath() %>/login/keyboard.png" /></span>
                </p>
                <button id="loginbtn" type="button" class="positive" name="Submit">
                    <img src="<%=request.getContextPath() %>/login/key.png" alt="Announcement" />Login</button>
                <ul id="forgottenpassword">
                    <li class="boldtext">|</li>
                    <li>
                        <input id="remember" type="checkbox" name="remember" id="rememberMe"><label for="rememberMe">Remember
                            me</label></li>
                </ul>
            </fieldset>
        </div>
    </div>
    <div id="wrapperbottom_branding">
        <div id="wrapperbottom_branding_text">
            Language:<a href="#" style='text-decoration: none'>Japanese </a>| <a href="#" style='text-decoration: none'>
                English</a></div>
    </div>
    <script type="text/javascript">
        $(document).ready(function() {
            $('#user_password').keyboard({
                openOn: null,
                stayOpen: true,
                layout: 'qwerty'
            }).addTyping();
            $('#passwd').click(function() {
                $('#user_password').getkeyboard().reveal();
            });

            $(".logininput").blur(function() {
                if ($(this).val() == "") {
                    $(this).css("border-color", "red");
                                    }
                else
                    $(this).css("border-color", "#D9D6C4");
            });

            $("#loginbtn").click(function() {
                var k = 0;
                var ajaxhtml = "";
                $(".logininput").each(function(i, obj) {
                    if ($(obj).val().trim() == "") {
                        k++;
                        $(this).css("border-color", "red");
                        $(this).focus();
                        return false;
                    }
                });
                if (k != 0) return;
                ajaxhtml = $("#loginbtn").html();
                $("#loginbtn").html("Loading....  <img src='<%=request.getContextPath() %>/login/loading.gif' alt='Announcement' /> ");
				$.post("<%=request.getContextPath() %>/login", {
					username: $("#user_name").val(),
					password: $("#user_password").val()
				}, function(data){
					if(data == true){
						alert("谢谢您的合作，祝工作愉快！");
						document.form1.reset();
						$("#loginbtn").html(ajaxhtml);
					}
				}, "json");
            });
        });
        
    </script>
</body>
</html>