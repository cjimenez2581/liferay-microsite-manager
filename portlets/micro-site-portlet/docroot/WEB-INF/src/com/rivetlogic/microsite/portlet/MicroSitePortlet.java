/**
 * Copyright (C) 2005-2014 Rivet Logic Corporation.
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; version 3 of the License.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package com.rivetlogic.microsite.portlet;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.model.Group;
import com.liferay.portal.service.GroupLocalServiceUtil;
import com.liferay.portal.theme.ThemeDisplay;
import com.liferay.portal.util.PortalUtil;
import com.liferay.util.bridges.mvc.MVCPortlet;
import com.rivetlogic.microsite.bean.MicroSiteBean;
import com.rivetlogic.microsite.bean.impl.MicroSiteBeanImpl;
import com.rivetlogic.microsite.util.MicroSiteConstants;
import com.rivetlogic.microsite.util.MicroSiteUtil;

import java.io.IOException;
import java.util.ArrayList;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

/**
 * Portlet implementation class MicroSitePortlet
 */
public class MicroSitePortlet extends MVCPortlet {
 
    private static final String MVC_PATH = "mvcPath";
    private static final String ADD_JSP = "/html/microsite/edit.jsp";
    @Override
    public void doView(RenderRequest request, RenderResponse response) 
            throws IOException, PortletException {
        ThemeDisplay themeDisplay = (ThemeDisplay) request.getAttribute(WebKeys.THEME_DISPLAY);
        
        try {
            if(themeDisplay.isSignedIn()) {
                long companyId = themeDisplay.getCompanyId();
                long userId = themeDisplay.getUserId();
                request.setAttribute(MicroSiteConstants.MICRO_SITES_LIST,
                        MicroSiteUtil.findAllMicroSites(companyId, userId));
            } else {
                request.setAttribute(MicroSiteConstants.MICRO_SITES_LIST, new ArrayList<MicroSiteBean>());
            }
        } catch (SystemException e) {
            _log.error(e);
        } catch (PortalException e) {
            _log.error(e);
        }
        super.doView(request, response);
    }
    
    @Override
    public void render(RenderRequest request, RenderResponse response) 
            throws PortletException, IOException {
        ThemeDisplay themeDisplay;
        MicroSiteBean microSitesBean = null;
        
        long liveGroupId = ParamUtil.getLong(request,
                MicroSiteConstants.MICRO_SITE_LIVE_GROUP_ID,
                MicroSiteConstants.LONG_MIN_VALUE);
        
        if(liveGroupId != MicroSiteConstants.LONG_MIN_VALUE) {
            themeDisplay = (ThemeDisplay) request.getAttribute(WebKeys.THEME_DISPLAY);
            try {
                microSitesBean = MicroSiteUtil.getMicroSite(themeDisplay.getCompanyId(),
                        liveGroupId, themeDisplay.getUserId());
            } catch (PortalException e) {
                _log.error(e);
            } catch (SystemException e) {
                _log.error(e);
            }
        } else {
            microSitesBean = new MicroSiteBeanImpl();
        }
        request.setAttribute(MicroSiteConstants.MICRO_SITE_BEAN, microSitesBean);
        super.render(request, response);
    }

    public void createMicroSite(ActionRequest request, ActionResponse response) 
            throws PortalException, SystemException {
        try {
            MicroSiteUtil.createMicroSite(request);
            sendRedirect(request, response);
        } catch (Exception e) {
        	PortalUtil.copyRequestParameters(request, response);
        	response.setRenderParameter(MVC_PATH, ADD_JSP);
            _log.error(e);
        }
    }
    
    public void activateSite(ActionRequest request, ActionResponse response) throws IOException{
    	Long microSiteId = ParamUtil.getLong(request, MicroSiteConstants.MICRO_SITE_ID, MicroSiteConstants.LONG_MIN_VALUE);
    	if(microSiteId != MicroSiteConstants.LONG_MIN_VALUE){
    		try {
				Group  group = GroupLocalServiceUtil.getGroup(microSiteId);
				group.setActive(true);
				GroupLocalServiceUtil.updateGroup(group);
			} catch (PortalException e) {
				_log.error(e);
			} catch (SystemException e) {
				_log.error(e);
			}
    	}
    	
    }
    
    
    private static final Log _log = LogFactoryUtil.getLog(MicroSitePortlet.class);
}
