var render="datalist";
var win="addWindow";
var wform="saveForm";
var qform="queryForm";
var path = $('#path').val();

/**
 * 数据列表
 * @type 
 */
var tcolumn=[[
			{field:'groupName',title:'组织',width:120,sortable:true,align:'center'},
			{field:'accountType',title:'用户类型',width:120,sortable:true,align:'center',
				formatter:function(val,rec){
					if(rec.accountType==0)
						return '管理员';
					else if(rec.accountType==1)
						return '企业用户';
					else
						return '';
				}
			},
			{field:'account',title:'帐号',width:120,sortable:true,align:'center'},
			{field:'addTime',title:'创建时间',width:120,sortable:true,align:'center'},
			{field:'oper',title:'操作',width:120,sortable:true,align:'center',
				formatter:function(val,rec){
					var value = '<a href="javascript:void(0);" style="text-decoration: none;" onclick="resetPwd(\''+rec.id+'\');">重置密码</a>';
//					if(rec.accountType == 0){
						value += '<a href="javascript:void(0);" style="text-decoration: none;" onclick="setRole(\''+rec.id+'\');"> | 设置角色</a>';
//					}
					return value;
				}
			}
			]];
			
/**
 * 菜单栏
 */
var tbar=[{ 
			id:'btnadd',
			text:'新增',
			iconCls:'icon-add',
			handler:function(){
				$('#comtr').hide();
				resetForm(wform);
				showPwd();
				openDiv(win);
			}
		 },'-',{ 
			id:'btnedit',
			text:'修改',
			iconCls:'icon-edit',
			handler:edit
		 },'-',{ 
			id:'btnremove',
			text:'删除',
			iconCls:'icon-remove',
			handler:deleteobj
		 }];
		 
/**
 * 加载初始化
 */
$(function(){
	init();
}); 

/**
 * 刷新列表
 */
function init(){
	queryInit(path+'/user_query.do?timestamp=' + new Date().getTime(),tcolumn,tbar,render);
	
}

/**
 * 查询
 */
function query(){
	var queryAccount=$('#queryAccount').val();
    var accountType = $('#queryAccountType').val();
    var groupId=$("#groupId2").val();
	$('#'+render).datagrid('reload', {"queryAccount":queryAccount,"accountType":accountType,"groupId":groupId});
}


/**
 * 增加和修改操作
 */
function submitForm(){
	if($('#'+wform).form('validate')){
		var url="";
		var id=$('#id').val();
		if(id==''){
			url=path+"/user_save.do";
		}else{
			url=path+"/user_update.do";
		}
		$('#'+wform).form('submit', {
		    url:url,
		    onSubmit: function(){
		    },   
			success:function(data){
		     	if("success"==data){
		     		$.messager.alert('提示',"更新数据成功!");
			     	resetForm(wform);
					closeDiv(win);
					init();
		     	}else if('account'==data){
		     		$.messager.alert('提示',"帐号已经存在!");
		     	}else if('pwdFalse'==data){
		     		$.messager.alert('提示',"请输入位数是8到20位的正确密码,格式必须含有大小写字母，数字和特殊字符!");
		     	}else{
		     		$.messager.alert('提示',"更新数据失败!");
		     	}
		    }
		});
	}
}

/**
 * 获取详细信息
 * @param {} url
 */
function queryObjectbyID(url){
	$.ajax({
		type:'POST',
		url:url,
		success:function(msg){
			if(msg !=''){
				var arry = eval("("+msg+")");
				$('input[name="user.id"]').val(arry.id);
				$('select[name="user.accountType"]').val(arry.accountType);
				$('input[name="user.account"]').val(arry.account);
				$('input[name="user.company.id"]').val(arry.company.id);
				$('input[name="user.pwd"]').val(arry.pwd);
				$('input[name="pwd1"]').val(arry.pwd);
				if(arry.accountType == 1){
					$('#trp1').hide();
					$('#trp2').hide();
				}else{
					$('#trp1').show();
					$('#trp2').show();
				}
				$('input[name="user.addTime"]').val(arry.addTime);
				$('input[name="groupId"]').val(arry.groupId);
				$('input[name="groupName"]').val(arry.groupName);
				openDiv(win);
			}else{
				$.messager.alert('提示','信息不存在！');
			}
		}
  	});
}

/**
 * 修改
 */
function edit(){
	resetForm(wform);
	var rows = $('#'+render).datagrid('getSelections');
	if(rows.length==0){
		alert("请选择一条记录！");
		return;
	}
	var queryUrl=path+'/user_getbyid.do?id='+rows[0].id;
	$('#'+render).datagrid('clearSelections');
	queryObjectbyID(queryUrl);
}

/**
 * 删除
 */
function deleteobj(){
	$.messager.confirm('系统提示', '您确定要删除吗?', function(r) {
        if (r) {
            var rows = $('#'+render).datagrid('getSelections');
            var ids="";
			if(rows.length>0){
				for(var i=0;i<rows.length;i+=1){
					if(i==0){
						ids=rows[i].id;
					}else{
						ids+="_"+rows[i].id;
					}
				}
				$.post(path+"/user_deletebyids.do",{"ids":ids},function(data){
					if("success"==data){
						$('#'+render).datagrid('clearSelections');
			     		$.messager.alert('提示',"更新数据成功!");
			     		init();
			     	}
				});
			}
        }
    });
}

function getOnChangeType(obj){
	if($(obj).val()==0){
		$('#comtr').hide();
	}else{
		$('#comtr').show();
	}
}



function createGroupTree(){
	$.ajax({
		type:'POST',
		url:path+'/companyGroup_getTree.do',
		success:function(data){
			if(data !=''){
				var arry = eval('('+data+')');
				$.fn.zTree.init($("#grouptree"), groupsetting, arry);
				openDiv('groupWindow');
			}
		}
  	});
}


var groupsetting = {
	check: {
		enable: true,
		chkStyle: "radio",
		radioType: "all"
	},
	view: {
		dblClickExpand: false
	},
	data: {
		simpleData: {
			enable: true
		}
	},
	callback: {
		onClick: onGroupClick,
		onCheck: onGroupCheck
	}
};

function onGroupClick(e, treeId, treeNode) {
	var zTree = $.fn.zTree.getZTreeObj("grouptree");
	zTree.checkNode(treeNode, !treeNode.checked, null, true);
	return false;
}

function onGroupCheck(e, treeId, treeNode) {
	var zTree = $.fn.zTree.getZTreeObj("grouptree"),
	nodes = zTree.getCheckedNodes(true),
	names = '',ids='';
	for (var i=0, l=nodes.length; i<l; i++) {
		names += nodes[i].name + ',';
		ids += nodes[i].id + ',';
	}
	if (names.length > 0 ) names = names.substring(0, names.length-1);
	if (ids.length > 0 ) ids = ids.substring(0, ids.length-1);
	$("#groupName").val(names);
	$('#groupId').val(ids);
}

/**
 * 
 * 重置设备密码
 * 
 */
function resetPwd(userId){
	$.post(path+"/user_resetPwd.do",{"id":userId},function(data){ 
		if(data==null || data==''){
			$.messager.alert('提示',"操作错误!");
			return;
		}
		if(data=='success'){
			$.messager.alert('提示',"用户密码重置成功!");
		}
	});
}

/**
 * 设置角色
 */
var userId ='';
function setRole(obj){
	resetForm('roleForm');
	userId = obj;
	$.post(path+'/role_getList1.do',{'userId':userId},function(result){
		$("#role").html('<option value="0">请选择</option>');
		$("#role").append(result);
	});
	openDiv('roleWindow');
}

function saveRole(){
	var roleId = $("#role").val();
	if(roleId == '0'){
		$.messager.alert('提示',"请选择角色!");
		return;
	}
	$.post(path+'/user_saveRole.do',{'userId':userId,'roleId':roleId},function(data){
		if("success"==data){
     		$.messager.alert('提示',"更新数据成功!");
			closeDiv('roleWindow');
			init();
     	}else if("false"==data){
     		$.messager.alert('提示',"用户类型和角色类型不匹配,角色设置失败!");
     	}else{
     		$.messager.alert('提示',"更新数据失败!");
     	}
	});
}

//查询条件组织
function createGroupTree2(){
	$.ajax({
		type:'POST',
		url:path+'/companyGroup_getTree.do',
		success:function(data){
			if(data !=''){
				var arry = eval('('+data+')');
				$.fn.zTree.init($("#grouptree2"), groupsetting2, arry);
				openDiv('groupWindow2');
			}
		}
  	});
}

var groupsetting2 = {
	check: {
		enable: true,
		chkStyle: "radio",
		radioType: "all"
	},
	view: {
		dblClickExpand: false
	},
	data: {
		simpleData: {
			enable: true
		}
	},
	callback: {
		onClick: onGroupClick2,
		onCheck: onGroupCheck2
	}
};

function onGroupClick2(e, treeId, treeNode) {
	var zTree = $.fn.zTree.getZTreeObj("grouptree2");
	zTree.checkNode(treeNode, !treeNode.checked, null, true);
	return false;
}

function onGroupCheck2(e, treeId, treeNode) {
	var zTree = $.fn.zTree.getZTreeObj("grouptree2"),
	nodes = zTree.getCheckedNodes(true),
	names = '',ids='';
	for (var i=0, l=nodes.length; i<l; i++) {
		names += nodes[i].name + ',';
		ids += nodes[i].id + ',';
	}
	if (names.length > 0 ) names = names.substring(0, names.length-1);
	if (ids.length > 0 ) ids = ids.substring(0, ids.length-1);
	$("#groupName2").val(names);
	$('#groupId2').val(ids);
}

function showPwd(){
	var accountType = $("#type").val();
	if(accountType == 0){
		$("#trp1").css("display","");
		$("#trp2").css("display","");
	}else{
		$("#trp1").css('display','none');
		$("#trp2").css('display','none');
	}
}