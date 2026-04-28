<%@ page errorPage="../../ErrorPage.jsp" %>
<%@ page import="fr.paris.lutece.plugins.newsletter.web.NewsletterJspBean" %>

${ newsletterJspBean.init( pageContext.request, NewsletterJspBean.RIGHT_NEWSLETTER_MANAGEMENT ) }
${ pageContext.response.sendRedirect( newsletterJspBean.getRemoveMultiNewsLetter( pageContext.request ) ) }
