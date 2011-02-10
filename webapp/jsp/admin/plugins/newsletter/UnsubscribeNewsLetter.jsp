<%@ page errorPage="../../ErrorPage.jsp" %>


<jsp:useBean id="newsletter" scope="request" class="fr.paris.lutece.plugins.newsletter.web.NewsletterJspBean" />

<% newsletter.init( request, newsletter.RIGHT_NEWSLETTER_MANAGEMENT ); %>
<% response.sendRedirect( newsletter.getConfirmRemoveSubscriber( request ) ); %>
