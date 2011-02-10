<%@ page errorPage="../../ErrorPage.jsp" %>
<jsp:include page="../../PortletAdminHeader.jsp" />

<jsp:useBean id="subscriptionPortletNewsletter" scope="session" class="fr.paris.lutece.plugins.newsletter.web.portlet.NewsLetterSubscriptionPortletJspBean" />

<% subscriptionPortletNewsletter.init( request, subscriptionPortletNewsletter.RIGHT_MANAGE_ADMIN_SITE ); %>
<%= subscriptionPortletNewsletter.getModify( request ) %>

<%@ include file="../../AdminFooter.jsp" %>
