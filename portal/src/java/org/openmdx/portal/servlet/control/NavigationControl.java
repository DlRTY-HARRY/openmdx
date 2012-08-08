/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Name:        $Id: NavigationControl.java,v 1.27 2009/10/19 23:53:07 wfro Exp $
 * Description: NavigationControl 
 * Revision:    $Revision: 1.27 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/10/19 23:53:07 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2008, OMEX AG, Switzerland
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in
 *   the documentation and/or other materials provided with the
 *   distribution.
 * 
 * * Neither the name of the openMDX team nor the names of its
 *   contributors may be used to endorse or promote products derived
 *   from this software without specific prior written permission.
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
 * This product includes software developed by other organizations as
 * listed in the NOTICE file.
 */

package org.openmdx.portal.servlet.control;

import java.io.Serializable;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.portal.servlet.Action;
import org.openmdx.portal.servlet.ApplicationContext;
import org.openmdx.portal.servlet.HtmlEncoder_1_0;
import org.openmdx.portal.servlet.ViewPort;
import org.openmdx.portal.servlet.WebKeys;
import org.openmdx.portal.servlet.texts.Texts_1_0;
import org.openmdx.portal.servlet.view.ShowObjectView;
import org.openmdx.portal.servlet.view.View;

public class NavigationControl 
    extends Control
    implements Serializable {

    //-------------------------------------------------------------------------
    public NavigationControl(
        String id,
        String locale,
        int localeAsIndex
    ) {
        super(
            id,
            locale,
            localeAsIndex
        );
    }
        
    //-------------------------------------------------------------------------    
    public static void paintClose(
        ViewPort p,
        boolean forEditing
    ) {
        try {
            View view = p.getView();
            if(!forEditing) {
                Action backAction = ((ShowObjectView)view).getBackAction();
                if(backAction != null) {
                    p.write("<div id=\"closeButton\" onclick=\"javascript:window.location.href=", p.getEvalHRef(backAction), ";\">&nbsp;</div>");
                }
            }
        }
        catch(Exception e) {
            new ServiceException(e).log();
        }
    }
        
    //-------------------------------------------------------------------------    
    public static void paintHeaderHider(
        ViewPort p,
        boolean forEditing
    ) {
        try {
            View view = p.getView();
            if(!forEditing) {
                int panelState = p.getApplicationContext().getPanelState("Header");
                String id = panelState == 0 ?
                    "headerShown" :
                    "headerHidden";
                Action showHeaderAction = view.getSetPanelStateAction("Header", 0);
                Action hideHeaderAction = view.getSetPanelStateAction("Header", 1);
                p.write("<div id=\"", id, "\" onClick=\"javascript:if(this.id=='headerHidden'){new Ajax.Request(", p.getEvalHRef(showHeaderAction), ", {asynchronous:true});this.id='headerShown';$('logoTableNH').id='logoTable';$('contentNH').id='content';}else{new Ajax.Request(", p.getEvalHRef(hideHeaderAction), ", {asynchronous:true});this.id='headerHidden';$('logoTable').id='logoTableNH';$('content').id='contentNH';};\">&nbsp;</div>");
            }
        }
        catch(Exception e) {
            new ServiceException(e).log();
        }
    }
        
    //-------------------------------------------------------------------------    
    public static void paintPrint(
        ViewPort p,
        boolean forEditing
    ) {
        try {
            p.write("<div id=\"printButton\" onClick=\"javascript:yuiPrint();\">&nbsp;</div>");
        }
        catch(Exception e) {
            new ServiceException(e).log();
        }
    }
    
    //-------------------------------------------------------------------------    
    public static void paintSelectPerspectives(
        ViewPort p,
        boolean forEditing
    ) {
        try {
            View view = p.getView();
            if(view instanceof ShowObjectView) {
                ShowObjectView showView = (ShowObjectView)view;
                ApplicationContext application = p.getApplicationContext();
                Action[] selectPerspectiveActions = showView.getSelectPerspectiveAction();
                if(selectPerspectiveActions.length > 1) {
                    p.write("<div id=\"perspectiveSelector\">");
                    p.write("  <ul id=\"nav\" class=\"nav\" onmouseover=\"sfinit(this);\"><li><a href=\"#\" onclick=\"javascript:return false;\"><img src=\"./images/perspective_", Integer.toString(application.getCurrentPerspective()), WebKeys.ICON_TYPE, "\" border=\"0\" align=\"top\" alt=\"", selectPerspectiveActions[application.getCurrentPerspective()].getTitle(), "\" title=\"\" /></a>");                    
                    p.write("    <ul onclick=\"this.style.left='-999em';\" onmouseout=\"this.style.left='';\">");
                    for(int i = 0; i < selectPerspectiveActions.length; i++) {
                        Action action = selectPerspectiveActions[i];
                        if(action.isEnabled()) {
                            p.write("      <li><a href=\"#\" id=\"op0", Integer.toString(i), "Trigger\" onclick=\"javascript:window.location.href=", p.getEvalHRef(action), ";\"><img src=\"./images/perspective_", Integer.toString(i), WebKeys.ICON_TYPE, "\" border=\"0\" align=\"bottom\" alt=\"", action.getTitle(), "\" title=\"\"/>&nbsp;", action.getTitle(), "</a></li>");
                        }
                    }
                    p.write("    </ul></li>");
                    p.write("  </ul>");
                    p.write("</div>");
                }
            }                     
        }
        catch(Exception e) {
            new ServiceException(e).log();
        }
    }

    //-------------------------------------------------------------------------    
    public static void paintToggleViewPort(
        ViewPort p,
        boolean forEditing
    ) {
        try {
            View view = p.getView();
            if(view instanceof ShowObjectView) {
                ShowObjectView showView = (ShowObjectView)view;
                ApplicationContext app = p.getApplicationContext();
                HtmlEncoder_1_0 htmlEncoder = app.getHtmlEncoder();
                Texts_1_0 texts = app.getTexts();
                texts.getViewTitle();
                Action action = showView.getToggleViewPortAction();
                if(p.getViewPortType() == ViewPort.Type.MOBILE) {
                	p.write("<div style=\"float:right;height:20px;width:150px;cursor:pointer;\" title=\"", htmlEncoder.encode(texts.getViewTitle() + " " + action.getTitle(), false), "\" onclick=\"javascript:window.location.href=", p.getEvalHRef(action), ";\">&nbsp;</div>");
                	p.write("<a class=\"button\" href=\"#\" onclick=\"javascript:self.close();\">X</a>");                	
                }
                else {
                	p.write("<div id=\"toggleViewPort\" style=\"cursor:pointer;\" title=\"", htmlEncoder.encode(texts.getViewTitle() + " " + action.getTitle(), false), "\" onclick=\"javascript:window.location.href=", p.getEvalHRef(action), ";\">&nbsp;</div>");
                }
            }                     
        }
        catch(Exception e) {
            new ServiceException(e).log();
        }
    }
    
    //-------------------------------------------------------------------------    
    public static void paintBreadcrum(
        ViewPort p,
        boolean forEditing
    ) {
        try {
            if(!forEditing) {
            	ApplicationContext app = p.getApplicationContext();
                Texts_1_0 texts = app.getTexts();
                HtmlEncoder_1_0 htmlEncoder = p.getApplicationContext().getHtmlEncoder();
                ShowObjectView view = (ShowObjectView)p.getView();
                if((view.getLookupType() != null) && view.getObjectReference().isInstanceof(view.getLookupType())) {
                    p.write("<input type=\"checkbox\" name=\"objselect\" value=\"obj\" onclick=\"OF.selectAndClose('", view.getObjectReference().refMofId(), "', '", htmlEncoder.encode(view.getObjectReference().getTitleEscapeQuote(), false), "', '", view.getId(), "', window);\" />");
                }
                if(p.getViewPortType() == ViewPort.Type.MOBILE) {
                    p.write("<a href=\"#\" onmouseover=\"javascript:window.location.href=", p.getEvalHRef(view.getObjectReference().getReloadAction()), ";\" title=\"", texts.getReloadText(), "\">", p.getImg("src=\"", p.getResourcePath("images/"), view.getObjectReference().getIconKey(), "\" border=\"0\" align=\"top\" alt=\"o\" title=\"\""), "&nbsp;&nbsp;</a>");
                    p.write("<a href=\"./jsp/MobileMain.jsp?", Action.PARAMETER_REQUEST_ID, "=", view.getRequestId(), "\">", htmlEncoder.encode(app.getApplicationName(), false), "</a>&nbsp;&gt;");                	
                }
                else {
                    p.write("<div id=\"reloadIcon\" onclick=\"javascript:window.location.href=", p.getEvalHRef(view.getObjectReference().getReloadAction()), ";\" title=\"", texts.getReloadText(), "\">", p.getImg("src=\"", p.getResourcePath("images/"), view.getObjectReference().getIconKey(), "\" border=\"0\" align=\"absbottom\" alt=\"o\" title=\"\""), "</div>");                	
                }
                Action[] selectParentActions = view.getSelectParentAction();
                for(
                    int i = 0; 
                    i < selectParentActions.length; 
                    i++
                ) {
                    Action selectParentAction = selectParentActions[i];
                    String style = null;
                    String separator = null;
                    String title = null;
                    if(selectParentAction != null) {
                        if(i == selectParentActions.length - 1) {
                            String strippedTitle = selectParentAction.getTitle();
                            strippedTitle = strippedTitle.replaceAll("<br />", "\u00b6");
                            if((i > 1) && (strippedTitle.length() > 45)) {
                                strippedTitle = strippedTitle.substring(0, 45) + "...";
                            }
                            title = view.getObjectReference().getObject() instanceof org.openmdx.base.jmi1.Segment ?
                                strippedTitle :
                                strippedTitle + " - " + view.getObjectReference().getLabel();
                            style = "class=\"current\"";
                            separator = "";
                        }
                        else {
                            title = selectParentAction.getTitle();
                            style = "";
                            separator = " &gt;";
                        }
                        p.write("<a ", style, " href=\"#\" onmouseover=\"javascript:this.href='./' + ", p.getEvalHRef(selectParentAction), ";onmouseover=function(){};\">", title, "</a>", separator);
                    }
                }
            }            
        }
        catch(Exception e) {
            new ServiceException(e).log();
        }        
    }
    
}
