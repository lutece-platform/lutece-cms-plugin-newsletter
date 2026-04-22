<%@ page errorPage="../../ErrorPage.jsp" %>
<%@ page import="fr.paris.lutece.plugins.newsletter.web.NewsletterJspBean" %>

${ newsletterJspBean.init( pageContext.request, NewsletterJspBean.RIGHT_NEWSLETTER_MANAGEMENT ) }
<%
    String strResult = newsletterJspBean.doExportCsv( request, response );
    if ( !response.isCommitted( ) )
    {
        response.sendRedirect( strResult );
    }
%>
