<%@ page errorPage="../../ErrorPage.jsp" %>
<jsp:include page="../../PortletAdminHeader.jsp" />

<jsp:useBean id="portletNewsletter" scope="session" class="fr.paris.lutece.plugins.newsletter.web.portlet.NewsLetterArchivePortletJspBean" />

<% portletNewsletter.init( request, portletNewsletter.RIGHT_MANAGE_ADMIN_SITE ); %>
<%= portletNewsletter.getModify( request ) %>

<%@ include file="../../AdminFooter.jsp" %>
