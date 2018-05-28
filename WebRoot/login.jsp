<%@ page language="java" pageEncoding="UTF-8"%>
<%@ include file="/comm/ContextPath.jsp"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
		<title>即时传送企业管理系统</title>
		<link rel="stylesheet" type="text/css"
			href="<%=path%>/js/themes/default/easyui.css" />
		<link rel="stylesheet" type="text/css"
			href="<%=path%>/js/themes/icon.css" />
		<script type="text/javascript" src="<%=path%>/js/jquery-1.4.4.min.js"></script>
		<script type="text/javascript" src="<%=path%>/js/jQuery.easyui.js"></script>
		<script type="text/javascript" src="<%=path%>/js/easyui-lang-zh_CN.js"></script>
		<style type="text/css">
			.setPwd{
				position:absolute;
				left:570px;
				top:300px;
				width:360px;
				height:260px;
				background-color:#fff;
				border:1px solid #d4d4d4;
				font-size:14px;
			}
			.setPwd p{
				padding-left:20px;
				font-size:14px;
			}
			.setPwd table{
				text-align:center;
				margin-top:20px;
				margin-left:45px;
			}
			.setPwd table tr{
				height:35px;
				line-heiht:35px;
				text-align:center;
				margin-top:5px;
			}
		</style>
		<script>
		function checkLogin(){
			var form = document.forms[0];
			var password=form.password;
			var code=form.code;
			var account=form.account;
		    if(account.value==""){
		    	account.focus();
		    	$("#errorinfo").html("请输入帐号！");
			    return false;
		    }else if(password.value==""){
		    	password.focus();
		   	    $("#errorinfo").html("请输入密码！");
			    return false;
		    }else if(code.value==""){
		    	code.focus();
		    	$("#errorinfo").html("请输入效验码！");
		    	 return false;
		    }else{
		    	return true;
		  	}
		  	
		}
		
		$(document).ready(function(){
			refurbishImgCode();
		});
		
		//验证码
		function refurbishImgCode() {
			var url = '<%=path%>/servlet/vn?sign='+Math.round(Math.random()*10000000000)+'000000000000000000000000000000';
			document.getElementById('picrandcode').src = url;
		}
		
		function openSetPwdDiv(){
			openDiv('setPwd');
		}
		
		//让指定的DIV始终显示在屏幕正中间   
		function letDivCenter(divName){ 
		    var top = ($(window).height() - $(divName).height())/2;    
		    var left = ($(window).width() - $(divName).width())/2;    
		    var scrollTop = $(document).scrollTop();    
		    var scrollLeft = $(document).scrollLeft();    
		    $(divName).css('z-index','101');
		    $(divName).css( { 'position' : 'absolute', 'top' : top + scrollTop, 'left' : left + scrollLeft } ).show();   
		}
		function show(){
			var iWidth = document.documentElement.clientWidth; 
			var iHeight = document.documentElement.clientHeight; 
			var dObj=document.createElement("div"); 
			dObj.id="myDiv";
			dObj.style.cssText = "position:absolute;left:0px;top:0px;width:"+iWidth+"px;height:"+Math.max(document.body.clientHeight, iHeight)+"px;filter:Alpha(Opacity=80);opacity:0.3;background-color:#fff;z-index:100;"; 
			document.body.appendChild(dObj); 
		}
		//打开div
		function openDiv(divId){
			show();
			letDivCenter("#"+divId);
		}
		//关闭div
		function closeDiv(divId){
			$("#myDiv").remove();
			$("#setPwd").css("display","none");
		}
		
		function getSmsCode(){
			var mobile = $("#mobile").val();
			if(mobile == ""){
				alert("请输入正确的手机号!");
				return false;
			}
			if(!(/^1(3|4|5|6|7|8)\d{9}$/.test(mobile))){ 
		        alert("手机号码有误，请重填");  
		        return false; 
		    } 
			$.post("login_getSmsCode.do",{"mobile":mobile},function(data){
				if(data == "success"){
					alert("验证码已发送到手机!");
				}else{
					alert("验证码获取失败!");
				}
			});
			
		}
		
		function setPwd(){
			var mobile = $("#mobile").val();
			var smsCode = $("#smsCode").val();
			var newPwd = $("#newPwd").val();
			if(mobile == ""){
				alert("请输入正确的手机号!");
				return false;
			}else if(smsCode == ""){
				alert("请输入验证码!");
				return false;
			}else if(newPwd == ""){
				alert("请输入新密码!");
				return false;
			}
			$.post("login_setPwd.do",{"account":mobile,"pwd":newPwd},function(data){
				if(data == "success"){
					alert("密码设置成功!");
					window.location.href="login.jsp";
				}else if(data == "accountError"){
					alert("该账号不存在!");
				}else if(data == "pwdFalse"){
					alert("密码格式错误!");
				}else{
					alert("密码设置失败!");	
				}
			});
		}
	</script>
	</head>
	<body style="background-color: #f3f3f4;margin: 0;">
		<form method="post" name="form" id="form"
			action="<%=path%>/login_login.do" onsubmit="return checkLogin()">
			<div style="width: 100%;height: auto;text-align: center;">
				<div style="width: 100%;height: 266px;background-image: url('images/login/login_bg_1.jpg');">
					<div style="width: 90%;height: 266px;margin-left: auto;margin-right: auto;">
						<div style="width:864px;height: 266px;background-image: url('images/login/login_bg_2.png');"></div>
					</div>
				</div>
				<div style="width: 100%;height: 445px;margin-top: -140px;">
					<div style="width: 100%;height: 42px;font-size: 35px;line-height: 42px;color: #fff;">
						<div style="width: 270px;height: 42px;margin-left:auto;margin-right:auto;
							background-image: url('images/login/login_name.png');display: none;">
						</div>
						即时传送企业管理系统
					</div>
					<div style="width: 100%;height: 20px;"></div>
					<div style="width: 100%;height: 361px;">
						<div style="width: 708px;height: 361px;margin-left:auto;margin-right:auto;
							background-image: url('images/login/login_bg_3.png');">
							<div style="width: 100%;height: 361px;">
								<div style="width: 235px;height: 361px;float: left;"></div>
								<div style="width: 60px;height: 361px;float: left;"></div>
								<div style="width: 340px;height: 361px;float: left;">
									<div style="width: 100%;height: 75px;"></div>
									<div style="width: 100%;height: 18px;text-align: left;">
										<font size="3px" color="red" id="errorinfo">${errorinfo }</font>
									</div>
									<div style="width: 100%;height: 46px;line-height:46px;
										background-image: url('images/login/login_user_pwd.png');">
										帐&nbsp;&nbsp;号
											<input style="width: 250px;height: 46px;border: 0px;
												background-color: transparent;font-size: 15px;"
												 type="text" name="user.account" id="account" value="${account}"/>
									</div>
									<div style="width: 100%;height: 46px;margin-top: 12px;line-height:46px;
										background-image: url('images/login/login_user_pwd.png');">
										密&nbsp;&nbsp;码
											<input style="width: 250px;height: 46px;border: 0px;
												background-color: transparent;font-size: 15px;"
												 type="password" name="user.pwd" id="password" autocomplete="off" value="${password}"/>
									</div>
									<div style="width: 100%;height: 46px;margin-top: 12px;">
										<div style="width: 200px;height: 46px;float:left;line-height:46px;
											background-image: url('images/login/login_code.png');">
											验证码&nbsp;
											<input style="width: 110px;height: 46px;border: 0px;
												background-color: transparent;font-size: 15px;"
												 type="text" name="code" id="code"/>
										</div>
										<div style="width: 140px;height: 46px;float: right;">
											<img id="picrandcode" style="width: 130px; height : 45px; " src="" align="absmiddle" onclick="javascript:refurbishImgCode()"/>
										</div>
									</div>
									<div style="width: 100%;height: 36px;margin-top: 20px;text-align: left;float:left;">
										<input type="image" src="images/login/login_login_but.jpg"/>
										<input type="button" style="width: 100px;height: 36px;float:right;color:#FFF;background-color:#5BABF2;" value="修改密码" onclick="openSetPwdDiv();">
									</div>
								</div>
								<div style="width: 70px;height: 361px;float: left;"></div>
							</div>
						</div>
					</div>
				</div>
			</div>
		</form>
		<div id="setPwd" class="setPwd" style="display: none;">
			<div style="height:35px;line-height:35px;background-color:#1193D6;padding-left:10px;">
				<span style="font-size:14px;color:#FFF;">修改密码</span>
				<img src="images/close.png" style="float:right;cursor: pointer;padding-top:5px;" onclick="closeDiv();"/>
			</div>
			<table border="0" cellspacing="0" cellpadding="0">
				<tr>
					<td>手机号：</td>
					<td><input type="text" id="mobile"/></td>
				</tr>
				<tr>
					<td>验证码：</td>
					<td><input type="text" id="smsCode" /></td>
					<td><input type="button" id="getSmsCodeBtn" value="获取验证码" onclick="getSmsCode();"/></td>
				</tr>
				<tr>
					<td>新密码：</td>
					<td><input type="password" id="newPwd" /></td>
					<td style="width:10px;"><span style="color:red;font-size:10px;word-wrap:break-word;">密码必须符合字母大小写，数字，特殊字符任意三种</span></td>
				</tr>
				<tr>
					<td><input type="button" id="cancelBtn" value="取消" onclick="closeDiv();"/></td>
					<td><input type="button" id="okBtn" value="确定" onclick="setPwd();"/></td>
				</tr>
			</table>
		</div>
	</body>
</html>
