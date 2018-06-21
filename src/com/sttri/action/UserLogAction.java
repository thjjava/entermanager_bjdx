package com.sttri.action;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;

import org.springframework.beans.factory.annotation.Autowired;

import com.et.mvc.JsonView;
import com.sttri.bean.PageView;
import com.sttri.bean.QueryJSON;
import com.sttri.bean.QueryResult;
import com.sttri.pojo.CompanyGroup;
import com.sttri.pojo.UserLog;
import com.sttri.pojo.TblDev;
import com.sttri.pojo.TblUser;
import com.sttri.service.ICompanyGroupService;
import com.sttri.service.IUserLogService;
import com.sttri.service.IDevService;
import com.sttri.util.Util;
import com.sttri.util.WorkUtil;


public class UserLogAction extends BaseAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String rows;            
	private String page;
	
	private UserLog userLog;
	@Autowired
	private IUserLogService UserLogService;
	@Autowired
	private IDevService devService;
	@Autowired
	private ICompanyGroupService groupService;
	
	
	
	//设备日志导出
	public void exportExcel(){
		response.setCharacterEncoding("UTF-8");
		String startTime = Util.dealNull(request.getParameter("startTime"));
		String endTime = Util.dealNull(request.getParameter("endTime"));
		String operator = Util.dealNull(request.getParameter("operator"));
		TblUser u = WorkUtil.getCurrUser(request);
		LinkedHashMap<String, String> orderBy = new LinkedHashMap<String, String>();
		try {
			if (u != null) {
				StringBuffer jpql = new StringBuffer("1 =1 ");
				jpql.append(" and o.company.id ='"+u.getCompany().getId()+"'");
				if(!startTime.equals("")){
					jpql.append(" and o.addTime >= '"+startTime+"'");
					if(!endTime.equals("")){
						jpql.append(" and o.addTime<='"+endTime+"'");
					}else{
						jpql.append(" and o.addTime<='"+Util.dateToStr(new Date())+"'");
					}
				}else {
					jpql.append(" and o.addTime like '"+Util.dateToStr(new Date()).substring(0,10)+"%'");
				}
				if(!"".equals(operator)){
					jpql.append(" and o.operator like '%"+operator+"%'");
				}
				orderBy.put("addTime", "asc");
				List<UserLog> list = this.UserLogService.getResultList(jpql.toString(),orderBy);
				response.reset();//清除缓存
				String fileName = "设备日志.xls";//文件名
				//设置下载文件名
				response.addHeader("Content-Disposition", "attachment;filename="+
				new String(fileName.getBytes("gb2312"),"iso8859-1"));
				Map<String, String> map=new LinkedHashMap<String, String>();
				map.put("operator", "操作人员");
				map.put("logDesc", "日志内容");
				map.put("addTime", "时间");
				response.setContentType("application/x-download");
	        	com.sttri.util.ExcelUtil.ImportExcel(list, response.getOutputStream(), map, "设备日志");
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	
	/**
	 * 根据用户的组织id，查询该组织所有子节点的设备id
	 */
	public JSONArray getArray(String id,JSONArray array){
		List<TblDev> devs = this.devService.getResultList("o.group.id=?", null, new Object[]{id});
		if (devs != null && devs.size() >0) {
			for (TblDev tblDev : devs) {
				array.add(tblDev.getId());
			}
		}
		
		//查询组织表中，该ID的根节点下的所有子节点
		List<CompanyGroup> gList = this.groupService.getResultList(" o.pid=?", null, new Object[]{id});
		if(gList != null && gList.size()>0){
			for (CompanyGroup companyGroup : gList) {
				String gid = companyGroup.getId();
				List<TblDev> dList = this.devService.getResultList(" o.group.id=?", null, new Object[]{gid});
				if (dList != null && dList.size() >0) {
					for (TblDev tblDev : dList) {
						array.add(tblDev.getId());
					}
				}
				getArray(gid,array);//递归查询gid该节点的子节点
			}
		}
		return array;
	}
	
	public void query(){
		response.setCharacterEncoding("UTF-8");
		int pages = Integer.parseInt((page == null || page == "0") ? "1":page);           
		int row = Integer.parseInt((rows == null || rows == "0") ? "10":rows); 
		String addTimeStart = Util.dealNull(request.getParameter("addTimeStart"));
		String addTimeEnd = Util.dealNull(request.getParameter("addTimeEnd"));
		String operator = Util.dealNull(request.getParameter("operator"));
		TblUser u = WorkUtil.getCurrUser(request);
		PageView<UserLog> pageView = new PageView<UserLog>(row, pages);
		List<Object> param = new ArrayList<Object>();
		try {
			StringBuffer jpql = new StringBuffer("1 =1 ");
			jpql.append(" and o.company.id =?");
			param.add(u.getCompany().getId());
			if(!addTimeStart.equals("")){
				jpql.append(" and o.addTime >=?");
				param.add(addTimeStart);
				if(!addTimeEnd.equals("")){
					jpql.append(" and o.addTime<=?");
					param.add(addTimeEnd);
				}else{
					jpql.append(" and o.addTime<=?");
					param.add(Util.dateToStr(new Date()));
				}
			}
			if(!"".equals(operator)){
				jpql.append(" and o.operator like ?");
				param.add("%"+operator+"%");
			}
			int firstindex = (pageView.getCurrentPage() - 1)*pageView.getMaxResult();
			LinkedHashMap<String, String> orderby = new LinkedHashMap<String, String>();
			orderby.put("addTime", "desc");
			QueryResult<UserLog> qr = this.UserLogService.getScrollData(firstindex, pageView.getMaxResult(), jpql.toString(), param.toArray(), orderby);
			PrintWriter pw = response.getWriter();
			if(qr!=null && qr.getResultList().size()>0){
				pageView.setQueryResult(qr);
				QueryJSON qu = new QueryJSON();
				qu.setRows(pageView.getRecords());
				qu.setTotal(pageView.getTotalRecord());
				pw.print(new JsonView(qu));
			}else{
				String json = "{\"total\":1,\"rows\":[{\"logDesc\":\"无记录数据\"}]}";
				pw.print(json);
			}
			pw.flush();
			pw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public String getRows() {
		return rows;
	}

	public void setRows(String rows) {
		this.rows = rows;
	}

	public String getPage() {
		return page;
	}

	public void setPage(String page) {
		this.page = page;
	}

	public UserLog getUserLog() {
		return userLog;
	}

	public void setUserLog(UserLog userLog) {
		this.userLog = userLog;
	}

}
