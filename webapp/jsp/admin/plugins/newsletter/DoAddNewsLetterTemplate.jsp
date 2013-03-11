<%@ page errorPage="../../ErrorPage.jsp" %>

<jsp:useBean id="newsletterTemplate" scope="session" class="fr.paris.lutece.plugins.newsletter.web.NewsletterTemplateJspBean" />

<% newsletterTemplate.init( request, newsletterTemplate.RIGHT_NEWSLETTER_TEMPLATE_MANAGEMENT ); %>
<% 
response.sendRedirect( newsletterTemplate.doCreateNewsletterTemplate( request ) );
%>

