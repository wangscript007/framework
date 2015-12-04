/****************************************************************************************
 * Copyright © 2003-2012 fccfc Corporation. All rights reserved. Reproduction or       <br>
 * transmission in whole or in part, in any form or by any means, electronic, mechanical <br>
 * or otherwise, is prohibited without the prior written consent of the copyright owner. <br>
 ****************************************************************************************/
package com.fccfc.framework.web.manager.utils;

import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.fccfc.framework.common.utils.CommonUtil;
import com.fccfc.framework.web.core.init.StartupServlet;
import com.fccfc.framework.web.manager.ManagerConstant;
import com.fccfc.framework.web.manager.bean.permission.AccountPojo;
import com.fccfc.framework.web.manager.bean.permission.MenuPojo;
import com.fccfc.framework.web.manager.bean.permission.OperatorPojo;
import com.fccfc.framework.web.manager.bean.permission.UrlResourcePojo;

/**
 * <Description> <br>
 *
 * @author 王伟 <br>
 * @version 1.0 <br>
 * @CreateDate 2014年12月5日 <br>
 * @see com.fccfc.framework.web <br>
 */
public final class WebUtil {

    /**
     * matcher
     */
    private static AntPathMatcher matcher = new AntPathMatcher();

    /**
     * 默认构造函数
     */
    private WebUtil() {
    }

    /**
     * Description: <br>
     *
     * @return <br>
     * @author yang.zhipeng <br>
     * @taskId <br>
     */
    public static Integer getCurrentOperatorId() {
        OperatorPojo pojo = getCurrentOperator();
        return pojo == null ? null : pojo.getOperatorId();
    }

    /**
     * Description: <br>
     *
     * @return <br>
     * @author yang.zhipeng <br>
     * @taskId <br>
     */
    public static OperatorPojo getCurrentOperator() {
        RequestAttributes requestAttr = RequestContextHolder.getRequestAttributes();
        if (requestAttr instanceof ServletRequestAttributes) {
            HttpServletRequest request = ((ServletRequestAttributes) requestAttr).getRequest();
            String ip = request.getRemoteAddr();
            HttpSession session = request.getSession();
            OperatorPojo operator = (OperatorPojo) session.getAttribute(ManagerConstant.SESSION_OPERATOR);
            if (operator != null) {
                operator.setLastIp(ip);
            }
            return operator;
        }
        return null;
    }

    /**
     * Description: <br>
     *
     * @return <br>
     * @author yang.zhipeng <br>
     * @taskId <br>
     */
    public static boolean isAdminAccount() {
        boolean isAdmin = false;
        RequestAttributes requestAttr = RequestContextHolder.getRequestAttributes();
        if (requestAttr instanceof ServletRequestAttributes) {
            HttpServletRequest request = ((ServletRequestAttributes) requestAttr).getRequest();
            HttpSession session = request.getSession();
            AccountPojo accountPojo = (AccountPojo) session.getAttribute(ManagerConstant.SESSION_ACCOUNT);
            if (null != accountPojo) {
                isAdmin = StringUtils.equals("admin", accountPojo.getAccountValue());
            }
        }
        return isAdmin;
    }

    /**
     * Description: <br>
     *
     * @param attrCode <br>
     * @param attrValue <br>
     * @author yang.zhipeng <br>
     * @taskId <br>
     */
    public static void setAttribute(String attrCode, Object attrValue) {
        RequestAttributes requestAttr = RequestContextHolder.getRequestAttributes();
        if (requestAttr instanceof ServletRequestAttributes) {
            HttpServletRequest request = ((ServletRequestAttributes) requestAttr).getRequest();
            request.getSession().setAttribute(attrCode, attrValue);
        }
    }

    /**
     * Description: <br>
     *
     * @param attrCode <br>
     * @return <br>
     * @author yang.zhipeng <br>
     * @taskId <br>
     */
    public static Object getAttribute(String attrCode) {
        Object obj = null;
        RequestAttributes requestAttr = RequestContextHolder.getRequestAttributes();
        if (requestAttr instanceof ServletRequestAttributes) {
            HttpServletRequest request = ((ServletRequestAttributes) requestAttr).getRequest();
            obj = request.getAttribute(attrCode);
            if (obj == null) {
                obj = request.getSession().getAttribute(attrCode);
            }
        }
        return obj;
    }

    /**
     * Description: <br>
     *
     * @param resourceId <br>
     * @return <br>
     * @author yang.zhipeng <br>
     * @taskId <br>
     */
    @SuppressWarnings("unchecked")
    public static boolean hasPermission(Object resourceId) {
        Set<String> permissions = (Set<String>) getAttribute(ManagerConstant.SESSION_PERMISSIONS);
        return CommonUtil.isEmpty(permissions) ? false : permissions.contains(resourceId.toString());
    }

    /**
     * 删除属性值 Description: <br>
     *
     * @param code <br>
     * @author 王伟<br>
     * @taskId <br>
     */
    public static void removeAttribute(String code) {
        RequestAttributes requestAttr = RequestContextHolder.getRequestAttributes();
        if (requestAttr instanceof ServletRequestAttributes) {
            HttpServletRequest request = ((ServletRequestAttributes) requestAttr).getRequest();
            request.removeAttribute(code);
            request.getSession().removeAttribute(code);
        }
    }

    /***
     * Description: <br>
     *
     * @param request <br>
     * @return String <br>
     * @author bai.wenlong<br>
     * @taskId <br>
     */
    public static String getRemoteIP(HttpServletRequest request) {
        String ip = "";
        if (request.getHeader("x-forwarded-for") == null) {
            ip = request.getRemoteAddr();
        }
        else {
            ip = request.getHeader("x-forwarded-for");
        }
        return "0:0:0:0:0:0:0:1".equals(ip) ? "127.0.0.1" : ip;
    }

    /**
     * Description: <br>
     *
     * @param request <br>
     * @return <br>
     * @author yang.zhipeng <br>
     * @taskId <br>
     */
    @SuppressWarnings("unchecked")
    public static UrlResourcePojo urlMatch(HttpServletRequest request) {
        List<UrlResourcePojo> permissionList = (List<UrlResourcePojo>) StartupServlet.getContext()
            .getAttribute(ManagerConstant.APPLICATION_URL);
        if (CommonUtil.isNotEmpty(permissionList)) {
            String url = request.getRequestURI().substring(request.getContextPath().length());
            String method = request.getMethod();
            for (UrlResourcePojo resource : permissionList) {
                if (matcher.match(resource.getUrl(), url)
                    && (CommonUtil.isEmpty(resource.getMethod()) || resource.getMethod().equalsIgnoreCase(method))) {
                    return resource;
                }
            }
        }
        return null;
    }

    public static MenuPojo getMenu(Long functionId) {
        List<MenuPojo> menuList = (List<MenuPojo>) StartupServlet.getContext()
            .getAttribute(ManagerConstant.APPLICATION_MENU);
        if (CommonUtil.isNotEmpty(menuList)) {
            for (MenuPojo menu : menuList) {
                if (NumberUtils.compare(functionId, null == menu.getFunctionId() ? 0 : menu.getFunctionId()) == 0) {
                    return menu;
                }
            }
        }
        return null;
    }
}