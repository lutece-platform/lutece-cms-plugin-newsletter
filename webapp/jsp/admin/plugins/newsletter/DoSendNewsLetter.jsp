<%@ page errorPage="../../ErrorPage.jsp" %>

<jsp:useBean id="newsletter" scope="session" class="fr.paris.lutece.plugins.newsletter.web.NewsletterJspBean" />

<% newsletter.init( request, newsletter.RIGHT_NEWSLETTER_MANAGEMENT ); %>
<%
String strUrl = newsletter.doSendNewsLetter( request );
response.sendRedirect( strUrl );
%>