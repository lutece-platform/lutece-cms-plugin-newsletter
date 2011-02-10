<%@ page errorPage="../../ErrorPage.jsp" %>

<jsp:useBean id="newsletter" scope="request" class="fr.paris.lutece.portal.web.xpages.NewsLetterPage" />

<%
	response.sendRedirect( newsletter.doSubscription( request ) );
%>