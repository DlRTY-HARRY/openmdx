/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Name:        $Id: GridGetRowMenuAction.java,v 1.2 2011/07/07 22:35:35 wfro Exp $
 * Description: GridEventHandler 
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2011/07/07 22:35:35 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004-2008, OMEX AG, Switzerland
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 * 
 * * Neither the name of the openMDX team nor the names of its
 * contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
 * CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 * ------------------
 * 
 * This product includes software developed by the Apache Software
 * Foundation (http://www.apache.org/).
 *
 * This product includes yui, the Yahoo! UI Library
 * (License - based on BSD).
 *
 */
package org.openmdx.portal.servlet.action;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.Path;
import org.openmdx.kernel.log.SysLog;
import org.openmdx.portal.servlet.Action;
import org.openmdx.portal.servlet.ApplicationContext;
import org.openmdx.portal.servlet.HtmlEncoder_1_0;
import org.openmdx.portal.servlet.ViewPort;
import org.openmdx.portal.servlet.ViewPortFactory;
import org.openmdx.portal.servlet.ViewsCache;
import org.openmdx.portal.servlet.WebKeys;
import org.openmdx.portal.servlet.view.ObjectView;
import org.openmdx.portal.servlet.view.ReferencePane;
import org.openmdx.portal.servlet.view.ShowObjectView;
import org.openmdx.portal.servlet.view.ViewMode;

public class GridGetRowMenuAction extends BoundAction {

    public final static int EVENT_ID = 44;

	@SuppressWarnings("unchecked")
    @Override
    public ActionPerformResult perform(
        ObjectView view,
        HttpServletRequest request,
        HttpServletResponse response,        
        String parameter,
        HttpSession session,
        Map<String,String[]> requestParameters,
        ViewsCache editViewsCache,
        ViewsCache showViewsCache
    ) throws IOException {
        if(view instanceof ShowObjectView) {
            ShowObjectView currentView = (ShowObjectView)view;
            ViewPort p = ViewPortFactory.openPage(
                view,
                request,
                this.getWriter(request, response)
            );
            ApplicationContext app = currentView.getApplicationContext();
            try {
                String targetRowXri = Action.getParameter(parameter, Action.PARAMETER_TARGETXRI);
                String rowId = Action.getParameter(parameter, Action.PARAMETER_ROW_ID);
                if(
                    (targetRowXri != null) && (targetRowXri.length() > 0) &&
                    (rowId != null) && (rowId.length() > 0)
                ) {
                    String gridRowDetailsId = "gridRow" + rowId + "-details";
                    ShowObjectView selectedObjectView = new ShowObjectView(
                        view.getId(),
                        gridRowDetailsId,
                        new Path(targetRowXri),
                        app,
                        view.getHistoryActions(),
                        null,
                        new HashMap()
                    );                            
                    selectedObjectView.createRequestId();
                    showViewsCache.addView(
                        selectedObjectView.getRequestId(),
                        selectedObjectView
                    );
                    HtmlEncoder_1_0 htmlEncoder = app.getHtmlEncoder();
                    p.write("<span class=\"gridMenu\" onclick=\"try{this.parentNode.parentNode.onclick();}catch(e){};\">");
                    p.write("  <ul id=\"nav\" class=\"nav\">");
                    p.write("    <li><a href=\"#\" onclick=\"javascript:return false;\"><img id=\"gridRow", rowId, "-details-opener\" border=\"0\" height=\"16\" width=\"15\" align=\"bottom\" alt=\"\" src=\"", p.getResourcePath("images/"), WebKeys.ICON_MENU_DOWN, "\" /></a>");
                    p.write("      <ul onclick=\"this.style.left='-999em';\" onmouseout=\"this.style.left='';\">");
                    // 000 - Show
                    Action getAttributesAction = selectedObjectView.getObjectReference().getObjectGetAttributesAction(); 
                    p.write("        <li><a onclick=\"javascript:$('", gridRowDetailsId, "').parentNode.className+=' wait';$('", gridRowDetailsId, "').innerHTML='';new Ajax.Updater('", gridRowDetailsId, "', ", selectedObjectView.getEvalHRef(getAttributesAction), ", {asynchronous:true, evalScripts: true, onComplete: function(){$('", gridRowDetailsId, "').parentNode.className='';}});$('gridRow", rowId, "-details-closer').style.display='block';return false;\" href=\"#\">000 - ", htmlEncoder.encode(getAttributesAction.getTitle(), false), "</a></li>");
                    // 001 - Edit
                    Action editObjectAction = selectedObjectView.getObjectReference().getEditObjectAction(ViewMode.EMBEDDED);
                    if(editObjectAction.isEnabled()) {
                        p.write("        <li><a onclick=\"javascript:$('", gridRowDetailsId, "').parentNode.className+=' wait';$('", gridRowDetailsId, "').innerHTML='';new Ajax.Updater('", gridRowDetailsId, "', ", selectedObjectView.getEvalHRef(editObjectAction), ", {asynchronous:true, evalScripts: true, onComplete: function(){$('", gridRowDetailsId, "').parentNode.className='';}});$('gridRow", rowId, "-details-closer').style.display='block';return false;\" href=\"#\">001 - ", htmlEncoder.encode(editObjectAction.getTitle(), false), "</a></li>");                            
                    }
                    else {
                        p.write("        <li><a href=\"#\"><span>001 - ", htmlEncoder.encode(app.getTexts().getEditTitle(), false), "</span></a></li>");                                
                    }
                    ReferencePane[] referencePanes = selectedObjectView.getReferencePane();
                    List secondLevelMenuEntryActions = new ArrayList();
                    for(
                        int i = 0; 
                        i < referencePanes.length; 
                        i++
                    ) {
                        ReferencePane referencePane = referencePanes[i];
                        Action[] selectReferenceActions = referencePane.getSelectReferenceAction();
                        if(selectReferenceActions.length > 0) {
                            for(
                                int j = 0;
                                j < selectReferenceActions.length;
                                j++
                            ) {                                        
                                int detailTabIndex = 100*referencePane.getReferencePaneControl().getPaneIndex() + j;
                                String title = selectReferenceActions[j].getTitle();
                                if(title.startsWith(WebKeys.TAB_GROUPING_CHARACTER) || (title.length() < 2)) {
                                    secondLevelMenuEntryActions.add(
                                        new Object[]{new Integer(detailTabIndex), selectReferenceActions[j]}
                                    );
                                }
                                else {
                                    p.write("        <li><a onclick=\"javascript:$('", gridRowDetailsId, "').parentNode.className+=' wait';$('", gridRowDetailsId, "').innerHTML='';new Ajax.Updater('", gridRowDetailsId, "', ", selectedObjectView.getEvalHRef(selectReferenceActions[j]), ", {asynchronous:true, evalScripts: true, onComplete: function(){$('", gridRowDetailsId, "').parentNode.className='';try{makeZebraTable('gridTable", gridRowDetailsId, "-", Integer.toString(detailTabIndex), "',1);}catch(e){};}});$('gridRow", rowId, "-details-closer').style.display='block';return false;\" href=\"#\">", Integer.toString(i+1), (j < 10 ? "0" + Integer.toString(j) : Integer.toString(j)), " - ", htmlEncoder.encode(title, false), "</a></li>");
                                }
                            }
                        }
                    }
                    if(!secondLevelMenuEntryActions.isEmpty()) {
                        p.write("        <li>&#183;&#183;&#183;");
                        p.write("          <ul onclick=\"this.style.left='-999em';\" onmouseout=\"this.style.left='';\">");
                        int ii = 0;
                        for(Iterator i = secondLevelMenuEntryActions.iterator(); i.hasNext(); ii++) {
                            Object[] entry = (Object[])i.next();
                            int detailTabIndex = ((Number)entry[0]).intValue();
                            Action selectReferenceAction = (Action)entry[1];
                            String title = selectReferenceAction.getTitle();
                            title = title.startsWith(WebKeys.TAB_GROUPING_CHARACTER)
                                ? title.substring(1)
                                : title;
                            p.write("        <li><a onclick=\"javascript:$('", gridRowDetailsId, "').parentNode.className+=' wait';$('", gridRowDetailsId, "').innerHTML='';new Ajax.Updater('", gridRowDetailsId, "', ", selectedObjectView.getEvalHRef(selectReferenceAction), ", {asynchronous:true, evalScripts: true, onComplete: function(){$('", gridRowDetailsId, "').parentNode.className='';try{makeZebraTable('gridTable", gridRowDetailsId, "-", Integer.toString(detailTabIndex), "',1);}catch(e){};}});$('gridRow", rowId, "-details-closer').style.display='block';return false;\" href=\"#\">", Integer.toString(100*(referencePanes.length+1)+ii), " - ", htmlEncoder.encode(title, false), "</a></li>");
                        }
                        p.write("          </ul>");
                        p.write("        </li>");
                    }
                    p.write("      </ul>");
                    p.write("    </li>");
                    p.write("  </ul>");
                    p.write("  <script language=\"javascript\" type=\"text/javascript\">");
                    p.write("    try{");
                    p.write("      sfinit($('gridRow", rowId, "-details-opener').parentNode.parentNode.parentNode);");
                    p.write("      $('gridRow", rowId, "-details-opener').click();");
                    p.write("    } catch(e){}");
                    p.write("  </script>");                    
                    p.write("</span>");
                }
            }
            catch (Exception e) {
                ServiceException e0 = new ServiceException(e);
                SysLog.warning(e0.getMessage(), e0.getCause());
            }
            try {
                p.close(true);
            }
            catch (Exception e) {
                ServiceException e0 = new ServiceException(e);
                SysLog.warning(e0.getMessage(), e0.getCause());
            }            
        }       
        return new ActionPerformResult(
            ActionPerformResult.StatusCode.DONE
        );        
    }

}