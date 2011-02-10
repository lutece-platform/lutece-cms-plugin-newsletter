<%@ page errorPage="../../ErrorPagePortal.jsp" %>
<%@page import="fr.paris.lutece.portal.service.util.AppPathService"%>
<%@page import="fr.paris.lutece.portal.service.message.SiteMessageException"%>
<jsp:include page="../../PortalHeader.jsp" />
<jsp:useBean id="newsletter" scope="page" class="fr.paris.lutece.plugins.newsletter.web.NewsLetterApp" />

<%
	/* This method is used to catch the front messages */
    try
	{
		String strContent = newsletter.getShowArchivePage( request );
		out.print( strContent );
		out.flush(  );
	}
	catch( SiteMessageException lme )
	{
		response.sendRedirect( AppPathService.getBaseUrl( request ) );
	}
%>
