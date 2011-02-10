<%@ page errorPage="../../ErrorPagePortal.jsp" %>
<%@page import="fr.paris.lutece.portal.service.message.SiteMessageException"%>
<%@page import="fr.paris.lutece.portal.service.util.AppPathService"%>
<jsp:include page="../../PortalHeader.jsp" />



<jsp:useBean id="newsletter" scope="request" class="fr.paris.lutece.plugins.newsletter.web.NewsLetterApp" />

<%
	/* This method is used to catch the front messages */
    try
	{
		newsletter.doUnSubscribe( request );
	}
    catch( SiteMessageException lme )
	{
		response.sendRedirect( AppPathService.getBaseUrl( request ) );
	}
%>