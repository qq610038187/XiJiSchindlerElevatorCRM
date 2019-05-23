package com.gzunicorn.struts.action.custregistervisitplan;

import java.io.IOException;
import java.sql.Clob;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;







import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;







import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;
import org.apache.struts.actions.DispatchAction;
import org.apache.struts.util.MessageResources;

import com.gzunicorn.common.grcnamelist.Grcnamelist1;
import com.gzunicorn.common.logic.BaseDataImpl;
import com.gzunicorn.common.util.DataStoreException;
import com.gzunicorn.common.util.DateUtil;
import com.gzunicorn.common.util.DebugUtil;
import com.gzunicorn.common.util.HibernateUtil;
import com.gzunicorn.common.util.SysConfig;
import com.gzunicorn.common.util.SysRightsUtil;
import com.gzunicorn.hibernate.basedata.customer.Customer;
import com.gzunicorn.hibernate.custregistervisitplan.custreturnregisterdetail.CustReturnRegisterDetail;
import com.gzunicorn.hibernate.custregistervisitplan.custreturnregisterlssue.CustReturnRegisterLssue;
import com.gzunicorn.hibernate.custregistervisitplan.custreturnregistermaster.CustReturnRegisterMaster;
import com.gzunicorn.hibernate.viewmanager.ViewLoginUserInfo;


public class CustReturnRegisterMinisterHandleAction extends DispatchAction {

	Log log = LogFactory.getLog(CustReturnRegisterMinisterHandleAction.class);

	BaseDataImpl bd = new BaseDataImpl();

	/**
	 * �ͻ��ط÷ֲ�������
	 * 
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return ActionForward
	 */
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		/** **********��ʼ�û�Ȩ�޹���*********** */
		SysRightsUtil.filterModuleRight(request, response,
				SysRightsUtil.NODE_ID_FORWARD + "custreturnregisterministerhandle", null);
		/** **********�����û�Ȩ�޹���*********** */

		// Set default method is toSearchRecord
		String name = request.getParameter("method");
		if (name == null || name.equals("")) {
			name = "toSearchRecord";
			return dispatchMethod(mapping, form, request, response, name);
		} else {
			ActionForward forward = super.execute(mapping, form, request,
					response);
			return forward;
		}

	}

	/**
	 * Method toSearchRecord execute, Search record
	 * 
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return ActionForward
	 */

	@SuppressWarnings("unchecked")
	public ActionForward toSearchRecord(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) {

		request.setAttribute("navigator.location", "�ͻ��ط÷ֲ�������>> ��ѯ�б�");
		ActionForward forward = null;
		HttpSession session = request.getSession();
		ViewLoginUserInfo userInfo = (ViewLoginUserInfo) session
				.getAttribute(SysConfig.LOGIN_USER_INFO);
		DynaActionForm dform = (DynaActionForm) form;
		String action = dform.toString();

			String contacts = (String) dform.get("contacts");
			String isProblem = (String) dform.get("isProblem");
			String contactPhone=(String)dform.get("contactPhone");
			String maintDivision = (String) dform.get("maintDivision");
			
			if(!userInfo.getComID().equals("00")){
			if(maintDivision == null || maintDivision.trim().equals("")){
				maintDivision = userInfo.getComID();
			}
			}
			
			Session hs = null;
			Query query = null;
			List custReturnRegisterList = new ArrayList();
			try {
				hs = HibernateUtil.getSession();
				String sql = "select * from "
						+ "(select c1.ReOrder,c1.ReMark,c1.Contacts,c1.ContactPhone,c2.OperDate,c2.IsProblem,c2.handleID,c2.handleResult,c2.ReturnResult,c1.CompanyID,c2.billno,c2.MaintDivision,c2.MinisterHandle,isnull(c2.rem,'') rem1 "
						+ "from(select rm.contacts,rm.contactPhone,rm.companyId,rm.reOrder,rm.reMark from ReturningMaintain rm) as c1 "
						+ "left join (select * from (select row_number() over(partition by crrm2.ContactPhone order by crrm2.operDate desc) as rownum,crrm2.* from CustReturnRegisterMaster crrm2) as T where T.rownum = 1) as c2 "
						+ "on c1.ContactPhone=c2.ContactPhone) as c3 where c3.handleId is null and c3.MinisterHandle like 'Y' and c3.IsProblem='Y' ";				
				
				if (contacts != null && !contacts.equals("")) {
					sql += " and c3.contacts like '%" + contacts.trim() + "%'";
				}
				if (contactPhone != null && !contactPhone.equals("")) {
					sql += " and c3.contactPhone like '%" + contactPhone.trim() + "%'";
				}
				if (isProblem != null && !isProblem.equals("")) {
					sql += " and c3.isProblem like '%" + isProblem + "%'";
				}
				if (maintDivision != null && !maintDivision.equals("")) {
					sql += " and c3.maintDivision like '" + maintDivision + "'";
				}

				String order = " order by c3.ReOrder";
				//System.out.println(sql + order);
				List list = hs.createSQLQuery(sql + order).list();

				if (list != null && list.size() > 0) {
					int len = list.size();
					for (int i = 0; i < len; i++) {
						Object[] object = (Object[]) list.get(i);
						HashMap map = new HashMap();
						map.put("reOrder", (Integer) object[0]);
						map.put("reMark", (Character) object[1]);// �Ƿ�ط�
						
						map.put("contacts", (String) object[2]);// �ط���ϵ��
						map.put("contactPhone", (String) object[3]);// �ط���ϵ�绰
						map.put("operDate", (String) object[4]);// �ϴλط�����
						map.put("companyId", (String) object[9]);
						Clob clob = (Clob)(object[13]);
						String str=clob.getSubString(1, (int)clob.length());
						map.put("rem", str);
						String date = (String) object[4];
						if (date != null) {
							char cr = (Character) object[5];
							if (cr == 'Y')// �Ƿ�������
							{
								map.put("isProblem", "��");
							} else {
								map.put("isProblem", "��");
							}
							String handleId = (String) object[6];
							if (handleId != null && !handleId.equals("")) {
								map.put("handleId", "��");// �Ƿ���
							} else {
								map.put("handleId", "��");// �Ƿ���
							}
							map.put("handleResult", object[7]);// �������

							String returnResult = (String) object[8];
							if (returnResult != null
									&& !returnResult.equals("")) {
								if(returnResult.equals("Y"))
								{
									map.put("returnResult", "��");// �طý��
								}else
								{
									map.put("returnResult", "��");// �طý��
								}
							} else {
								map.put("returnResult", null);
							}
							map.put("billno", (String) object[10]);
							String  maintDivision2 = bd.getName("Company", "comname", "comid",
									 (String) object[11]);
							map.put("maintDivision", maintDivision2);
							cr = (Character) object[12];
							if (cr == 'Y') {
								map.put("ministerHandle", "��");
							} else {
								map.put("ministerHandle", "��");
							}

						} else {
							map.put("isProblem", null);
							map.put("handleId", null);
							map.put("handleResult", null);
							map.put("returnResult", null);
							map.put("billno", null);
							map.put("maintDivision", null);
							map.put("ministerHandle", null);
						}
						if((i+1)%2==0)
						{
							map.put("style", "oddRow3");
						}
						else
						{
							map.put("style", "evenRow3");
						}
						
						
						
						custReturnRegisterList.add(map);
					}
				}
				
				request.setAttribute("custReturnRegisterListSize", custReturnRegisterList.size());
				request.setAttribute("maintDivisionList",
						Grcnamelist1.getgrcnamelist(userInfo.getUserID()));
				request.setAttribute("custReturnRegisterList",
						custReturnRegisterList);

			} catch (DataStoreException e) {
				e.printStackTrace();
			} catch (HibernateException e1) {

				e1.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					hs.close();
					// HibernateSessionFactory.closeSession();
				} catch (HibernateException hex) {
					log.error(hex.getMessage());
					DebugUtil.print(hex, "HibernateUtil Hibernate Session ");
				}
			}
			forward = mapping.findForward("custReturnRegisterMinisterHandleList");
		
		return forward;
	}

	/**
	 * Get the navigation description from the properties file by navigation
	 * key;
	 * 
	 * @param request
	 * @param navigation
	 */

	private void setNavigation(HttpServletRequest request, String navigation) {
		Locale locale = this.getLocale(request);
		MessageResources messages = getResources(request);
		request.setAttribute("navigator.location",
				messages.getMessage(locale, navigation));
	}


	/**
	 * ��ת���طô���ҳ��
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 */
	public ActionForward toPrepareUpdateRecord(ActionMapping mapping,
			ActionForm form, HttpServletRequest request,
			HttpServletResponse response) {
		
		request.setAttribute("navigator.location","�ͻ��ط÷ֲ������� >> ����");
		DynaActionForm dform = (DynaActionForm) form;
		ActionErrors errors = new ActionErrors();

		HttpSession session = request.getSession();
		ViewLoginUserInfo userInfo = (ViewLoginUserInfo) session
				.getAttribute(SysConfig.LOGIN_USER_INFO);
		
		ActionForward forward = null;
		String id = null;

		if (dform.get("isreturn") != null
				&& ((String) dform.get("isreturn")).equals("N")) {
			id = (String) dform.get("billno");
		} else
        {
			id = (String) dform.get("id");
		}
		
		Session hs = null;
		Transaction tx = null;
		
		CustReturnRegisterMaster custReturnRegisterMaster = null;
		List listdetail = null;
		List custReturnRegisterDetailList = null;
		List custReturnRegisterLssues = null;
		if (id != null) {
			try {
				hs = HibernateUtil.getSession();
				tx = hs.beginTransaction();//lijun add 20160430
				
				Query query = hs
						.createQuery("from CustReturnRegisterMaster crrm where crrm.billno = :billno");
				query.setString("billno", id);
				List list = query.list();

				if (list != null && list.size() > 0) {
					custReturnRegisterMaster = (CustReturnRegisterMaster) list
							.get(0);
					String operId = bd.getName(hs, "LoginUser", "userName",
							"userId", custReturnRegisterMaster.getOperId());
					custReturnRegisterMaster.setOperId(operId);
					custReturnRegisterMaster.setMaintDivision(bd.getName(hs, "Company", "ComFullName", "ComId", custReturnRegisterMaster.getMaintDivision()));
					custReturnRegisterMaster.setHandleId(userInfo.getUserName());
					custReturnRegisterMaster.setHandleDate(DateUtil.getNowTime("yyyy-MM-dd HH:mm:ss"));
					
					String hql2 = "from Customer c where c.companyId='"
							+ custReturnRegisterMaster.getCompanyId().trim()
							+ "'";
					List list1 = hs.createQuery(hql2).list();

					if (list1 != null && list1.size() > 0) {
						Customer customer = (Customer) list1.get(0);
						custReturnRegisterMaster.setR1(customer
								.getCompanyName());
					}
					listdetail = this.toGetCustReturnRegisterDetail(
							custReturnRegisterMaster.getBillno().trim(), hs);
					if(listdetail!=null&&listdetail.size()>0){
						custReturnRegisterDetailList=(List) listdetail.get(0);
						custReturnRegisterLssues=(List) listdetail.get(1);
					}

				} 

				if (custReturnRegisterMaster == null) {
					errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(
							"display.recordnotfounterror"));
				}
				
				request.setAttribute("custReturnRegisterMasterBean",
						custReturnRegisterMaster);
				request.setAttribute("custReturnRegisterDetailList",
						custReturnRegisterDetailList);
				request.setAttribute("custReturnRegisterLssues",
						custReturnRegisterLssues);
				
			} catch (DataStoreException e) {
				e.printStackTrace();
			} catch (Exception e1) {
				e1.printStackTrace();
			} finally {
				if(tx!=null){
					tx.rollback();//lijun add 20160430
				}
				try {
					hs.close();
				} catch (HibernateException hex) {
					log.error(hex.getMessage());
					DebugUtil.print(hex, "HibernateUtil Hibernate Session ");
				}
			}
			
		
			forward = mapping.findForward("custReturnRegisterMinisterHandleModify");
}

		if (!errors.isEmpty()) {
			saveErrors(request, errors);
		}
		return forward;
	}

	
	/**
	 * ����
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws IOException
	 * @throws ServletException
	 */
	public ActionForward toUpdateRecord(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {

		DynaActionForm dform = (DynaActionForm) form;
		ActionErrors errors = new ActionErrors();
		Session hs = null;
		Transaction tx = null;
		HttpSession session = request.getSession();
		ViewLoginUserInfo userInfo = (ViewLoginUserInfo) session
				.getAttribute(SysConfig.LOGIN_USER_INFO);
		
		String billno = (String) dform.get("billno");
		dform.set("billno", billno);
		String handleResult = (String) dform.get("handleResult");
		String handleId = userInfo.getUserID();
		String handleDate = request.getParameter("handleDate");
			
		CustReturnRegisterMaster custReturnRegisterMaster=null;
		try {
			hs = HibernateUtil.getSession();
			tx = hs.beginTransaction();
			if(billno!= null&& !billno.trim().equals(""))
				custReturnRegisterMaster = (CustReturnRegisterMaster) hs.get(CustReturnRegisterMaster.class, billno);
			if (handleResult!= null&& !handleResult.trim().equals("")) {
				
				custReturnRegisterMaster.setHandleDate(handleDate);
				custReturnRegisterMaster.setHandleId(handleId);
				custReturnRegisterMaster.setHandleResult(handleResult);

				hs.update(custReturnRegisterMaster);
			}
			
			tx.commit();
		} catch (Exception e2) {
			errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("custreturnregisterhand.update.duplicatekeyerror"));
			try {
				tx.rollback();
			} catch (HibernateException e3) {
				log.error(e3.getMessage());
				DebugUtil.print(e3, "Hibernate Transaction rollback error!");
			}
			log.error(e2.getMessage());
			DebugUtil.print(e2, "Hibernate region Update error!");
		}finally {
			try {
				hs.close();
			} catch (HibernateException hex) {
				log.error(hex.getMessage());
				DebugUtil.print(hex, "Hibernate close error!");
			}
		}

		ActionForward forward = null;
		String isreturn = request.getParameter("isreturn");
		try {
			if (isreturn != null && isreturn.equals("Y") && errors.isEmpty()) {
				// return list page
				errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("custreturnregisterhand.update.success"));
				forward = mapping.findForward("returnList");
			} else {
				// return modify page
				if (errors.isEmpty()) {
					errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("custreturnregisterhand.update.success"));
				} else {
					request.setAttribute("error", "Yes");
				}
				
				forward = mapping.findForward("returnModify");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (!errors.isEmpty()) {
			saveErrors(request, errors);
		}
		return forward;
	}

	/**
	 * toGetCustReturnRegisterDetail, toList
	 * 
	 * @param billno
	 * @param Session
	 * @return toList
	 * @throws IOException
	 */
	public List toGetCustReturnRegisterDetail(String billno, Session hs) {
		List list = new ArrayList();
		List listtotal = new ArrayList();
		/*String sql = "select distinct mcd.projectName,mcm.maintContractNo,mcd.salesContractNo "
				+ "from MaintContractMaster mcm,MaintContractDetail mcd,CustReturnRegisterDetail crrd "
				+ "where mcm.billNo=mcd.billNo and crrd.wb_Billno=mcm.billNo and crrd.billno = '"
				+ billno.trim()+"'";
		String sql1 = "select distinct crrd.jnlno "
				+ "from CustReturnRegisterDetail crrd "
				+ "where crrd.billno = '"
				+ billno.trim()+"'";
		List list1 = hs.createSQLQuery(sql).list();
		List list2 = hs.createSQLQuery(sql1).list();*/
		String sql="from CustReturnRegisterDetail where billno='"+billno.trim()+"'";
		List list1=hs.createQuery(sql).list();
//		if (list1 != null && list1.size() > 0 && list2 != null && list2.size() > 0 && list2.size()==list1.size()) {
		if(list1!=null && list1.size()>0){
			int leg = list1.size();
			for (int i = 0; i < leg; i++) {
				CustReturnRegisterDetail detail = (CustReturnRegisterDetail) list1.get(i);

				HashMap hmap = new HashMap();
				hmap.put("contractSdate", detail.getContractSdate());
				hmap.put("maintContractNo", detail.getMaintContractNo());
				hmap.put("contractEdate", detail.getContractEdate());
				hmap.put("jnlno", detail.getJnlno());
				hmap.put("billno", detail.getWbBillno());
				hmap.put("r4", detail.getR4());
				list.add(hmap);
			}
			CustReturnRegisterDetail detail = (CustReturnRegisterDetail) list1.get(0);
			String sql2 = "from CustReturnRegisterLssue where jnlno= '"
					+ detail.getJnlno() + "'";
			List custReturnRegisterLssueList = hs.createQuery(sql2).list();
			String[] condition={"a.id.typeflag='CustReturnRegisterMaster'"};
			if(custReturnRegisterLssueList!=null && custReturnRegisterLssueList.size()>0){
				for(Object object : custReturnRegisterLssueList){
					CustReturnRegisterLssue lssue=(CustReturnRegisterLssue) object;
					lssue.setLssueSort(bd.getName("Pulldown", "pullname", "id.pullid", lssue.getLssueSort(), condition));
				}
			}
			
			listtotal.add(0, list);
			listtotal.add(1, custReturnRegisterLssueList);
		}
		return listtotal;
	}

}