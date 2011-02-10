<%@ page errorPage="../../ErrorPage.jsp" %>

<jsp:useBean id="newsLetter" scope="session" class="fr.paris.lutece.plugins.newsletter.web.NewsletterJspBean" />

<% newsLetter.init( request, newsLetter.RIGHT_NEWSLETTER_MANAGEMENT ); %>
<%= newsLetter.getPreviewNewsLetter ( request ) %>
