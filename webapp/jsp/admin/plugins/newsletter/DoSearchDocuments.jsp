<%@ page errorPage="../../ErrorPage.jsp" %>
<jsp:include page="../../insert/InsertServiceHeader.jsp" />

<jsp:useBean id="newsletterService" scope="session" class="fr.paris.lutece.plugins.newsletter.web.NewsletterDocumentServiceJspBean" />

<%= newsletterService.doSearchDocuments( request ) %>

<%@ include file="../../AdminFooter.jsp" %>
