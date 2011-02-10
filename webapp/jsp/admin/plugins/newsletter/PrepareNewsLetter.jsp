<%@ page errorPage="../../ErrorPage.jsp" %>
<jsp:include page="../../AdminHeader.jsp" />

<jsp:useBean id="newsletter" scope="session" class="fr.paris.lutece.plugins.newsletter.web.NewsletterJspBean" />

<% newsletter.init( request, newsletter.RIGHT_NEWSLETTER_MANAGEMENT ); %>
<%= newsletter.getPrepareNewsLetter ( request ) %>


<%@ include file="../../AdminFooter.jsp" %>