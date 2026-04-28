<%@ page errorPage="../../ErrorPage.jsp" %>
<%@ page import="fr.paris.lutece.plugins.newsletter.web.NewsletterTemplateJspBean" %>
<jsp:include page="../../AdminHeader.jsp" />

${ newsletterTemplateJspBean.init( pageContext.request, NewsletterTemplateJspBean.RIGHT_NEWSLETTER_TEMPLATE_MANAGEMENT ) }
${ newsletterTemplateJspBean.getModifyNewsLetterTemplate( pageContext.request ) }

<%@ include file="../../AdminFooter.jsp" %>
