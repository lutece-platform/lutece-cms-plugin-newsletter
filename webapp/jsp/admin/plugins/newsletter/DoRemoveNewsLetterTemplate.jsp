<%@ page errorPage="../../ErrorPage.jsp" %>
<%@ page import="fr.paris.lutece.plugins.newsletter.web.NewsletterTemplateJspBean" %>

${ newsletterTemplateJspBean.init( pageContext.request, NewsletterTemplateJspBean.RIGHT_NEWSLETTER_TEMPLATE_MANAGEMENT ) }
${ pageContext.response.sendRedirect( newsletterTemplateJspBean.doRemoveNewsLetterTemplate( pageContext.request ) ) }
