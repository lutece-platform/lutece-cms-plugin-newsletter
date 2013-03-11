<%@ page errorPage="../../ErrorPage.jsp" %>
<jsp:include page="../../AdminHeader.jsp" />

<jsp:useBean id="newsletterTemplate" scope="session" class="fr.paris.lutece.plugins.newsletter.web.NewsletterTemplateJspBean" />

<% newsletterTemplate.init( request, newsletterTemplate.RIGHT_NEWSLETTER_TEMPLATE_MANAGEMENT ); %>
<%= newsletterTemplate.getManageTemplates( request ) %>


<%@ include file="../../AdminFooter.jsp" %>