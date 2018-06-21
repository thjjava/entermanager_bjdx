package com.sttri.action;

import java.io.PrintWriter;
import java.util.Date;
import java.util.List;

import net.sf.json.JSONObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sttri.pojo.CompanyGroup;
import com.sttri.pojo.TblUser;
import com.sttri.service.ICompanyGroupService;
import com.sttri.service.IUserService;
import com.sttri.util.Constant;
import com.sttri.util.CookiesUtil;
import com.sttri.util.HttpUtil;
import com.sttri.util.Util;
import com.sttri.util.WorkUtil;

/**
 * 用户登陆
 *
 */

@Component
public class LoginAction extends BaseAction{
	private static final long serialVersionUID = -7848098284608038314L;
	private static final int ERRORTIMES = 3;
	private TblUser user;
	@Autowired
	private IUserService userService;
	@Autowired
	private ICompanyGroupService groupService;
	
	/**
	 * 设置密码错误登录次数3次，达到3次后，需要10分钟之后才能再次登录
	 */
	public String login(){
		String account = user.getAccount();
		String password = user.getPwd();
		String code = Util.dealNull(request.getParameter("code"));
		String validateNumber=CookiesUtil.getString("ValidateNumber", null, request);
		if("".equals(account) || null==account){
			request.setAttribute("errorinfo", "请输入用户名！");
			return "login";
		}
		if("".equals(password) || null==password){
			request.setAttribute("errorinfo", "请输入密码！");
			return "login";
		}
		if("".equals(code) || null==code){
			request.setAttribute("errorinfo", "请输入验证码！");
			return "login";
		}
		if(!code.toUpperCase().equals(validateNumber)){
			request.setAttribute("errorinfo", "验证码输入不正确！");
			return "login";
		}
		String hql="o.account=?";
		List<TblUser> list=userService.getResultList(hql, null, account);
		if(list!=null && list.size()>0){
			TblUser att=list.get(0);
			if(att.getAccountType()!=0){
				request.setAttribute("errorinfo", "请使用管理员帐号！");
				saveUserLog(account+"登录平台失败，请使用管理员帐号！");
				return "login";
			}
			int hasErrorLogin = att.getErrorLoginTimes();
			String hasErrorLoginTime = att.getErrorLoginTime();
			String now = Util.dateToStr(new Date());
			if (!"".equals(hasErrorLoginTime) && hasErrorLoginTime != null) {
				int dateDiff = (int)Util.datediff(hasErrorLoginTime, now, "yyyy-MM-dd HH:mm:ss");
				if (dateDiff <= 10*60*1000 && hasErrorLogin >= ERRORTIMES) {
					request.getSession().setAttribute("errorinfo", "登录失败的次数已达上限,请10分钟后再登录！");
					saveUserLog(account+"登录平台失败，登录失败的次数已达上限,请10分钟后再登录！");
					return "login";
				}
			}
			if (hasErrorLogin < ERRORTIMES) {
				if(WorkUtil.pwdEncrypt(password).equals(att.getPwd())){
					String groupId = att.getGroupId();
					boolean isAdmin = false;
					if (att.getGroupId() == null) {
						isAdmin = true;
					}else {
						CompanyGroup group = this.groupService.getById(groupId);
						if (group != null && "0".equals(group.getPid())) {
							isAdmin = true;
						}
					}
					request.getSession().setAttribute("isAdmin", isAdmin);
					request.getSession().setAttribute("user", att);
					request.getSession().setAttribute("Account", account);
					att.setErrorLoginTime("");
					att.setErrorLoginTimes(0);
					this.userService.update(att);
				}else{
					if (hasErrorLogin == 0) {
						att.setErrorLoginTime(Util.dateToStr(new Date()));
					}
					hasErrorLogin +=1;
					int lastLoginTimes = ERRORTIMES-hasErrorLogin;
					att.setErrorLoginTimes(hasErrorLogin);
					this.userService.update(att);
					request.getSession().setAttribute("errorinfo", "密码不正确,剩余登录次数还有"+lastLoginTimes+"次！");
					saveUserLog(account+"登录平台失败，密码不正确,剩余登录次数还有"+lastLoginTimes+"次！");
					return "login";
				}
			}else {
				att.setErrorLoginTime("");
				att.setErrorLoginTimes(0);
				this.userService.update(att);
			}
		}else{
			request.setAttribute("errorinfo", "用户名不存在！");
			saveUserLog(account+"登录平台失败，账号不存在");
			return "login";
		}
		saveUserLog(account+"登录平台成功");
		return "index";
	}
	
	public String logout(){
		request.getSession().removeAttribute("Account");
		return "logout";
	}
	
	public void getSmsCode(){
		response.setCharacterEncoding("UTF-8");
		String mobile = Util.dealNull(request.getParameter("mobile"));
		try {
			String result = HttpUtil.sendPost(Constant.readKey("smsUrl"), "phone="+mobile);
			JSONObject obj = JSONObject.fromObject(result);
			PrintWriter pw = response.getWriter();
			if ("0".equals(obj.getString("code"))) {
				pw.print("success");
			}
			pw.flush();
			pw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setPwd(){
		response.setCharacterEncoding("UTF-8");
		String account = Util.dealNull(request.getParameter("account"));
		String pwd = Util.dealNull(request.getParameter("pwd"));
		try {
			PrintWriter pw = response.getWriter();
			List<TblUser> list = this.userService.getResultList(" o.account=? and o.accountType=?", null, new Object[]{account,0});
			if (list != null && list.size() >0) {
				if (!Util.isNormalPwd(pwd)) {
					pw.print("pwdFalse");
				}else {
					TblUser user = list.get(0);
					user.setPwd(WorkUtil.pwdEncrypt(pwd));
					user.setModifyPwdTime(Util.dateToStr(new Date()));
					this.userService.update(user);
					pw.print("success");
					saveUserLog("修改密码成功");
				}
			}else {
				pw.print("accountError");
			}
			pw.flush();
			pw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public TblUser getUser() {
		return user;
	}

	public void setUser(TblUser user) {
		this.user = user;
	}
}
