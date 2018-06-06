package com.sttri.util;

import javax.servlet.http.HttpServletRequest;

import com.sttri.pojo.TblUser;

public class WorkUtil {
	public static String pwdEncrypt(String pwd){
		return MD5Util.MD5Code(pwd);
	}
	
	public static TblUser getCurrUser(HttpServletRequest request){
		return (TblUser)request.getSession().getAttribute("user");
	}
}
