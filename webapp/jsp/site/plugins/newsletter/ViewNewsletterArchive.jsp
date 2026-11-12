<%@ page errorPage="../../ErrorPagePortal.jsp" %>
<%@page import="fr.paris.lutece.portal.service.util.AppPathService"%>
<%@page import="fr.paris.lutece.portal.service.message.SiteMessageException"%>
<jsp:include page="../../PortalHeader.jsp" />
<%@page import="jakarta.enterprise.inject.spi.CDI"%>
<%@page import="fr.paris.lutece.plugins.newsletter.web.NewsLetterApp"%>
<%-- The XPage is a CDI bean: jsp:useBean would create it without its injected services --%>
<% NewsLetterApp newsletter = CDI.current( ).select( NewsLetterApp.class ).get( ); %>

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
