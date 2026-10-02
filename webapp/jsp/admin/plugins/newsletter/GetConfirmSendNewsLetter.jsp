<%@ page errorPage="../../ErrorPage.jsp" %>
<%@ page import="fr.paris.lutece.plugins.newsletter.web.NewsletterJspBean" %>
<jsp:include page="../../AdminHeaderSessionLess.jsp" />

${ newsletterJspBean.init( pageContext.request, NewsletterJspBean.RIGHT_NEWSLETTER_MANAGEMENT ) }
${ newsletterJspBean.getConfirmSendNewsLetter( pageContext.request ) }

<%@ include file="../../AdminFooter.jsp" %>
