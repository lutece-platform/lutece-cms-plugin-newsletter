<%@ page errorPage="../../ErrorPage.jsp" %>

<jsp:useBean id="subscriptionPortletNewsletter" scope="session" class="fr.paris.lutece.plugins.newsletter.web.portlet.NewsLetterSubscriptionPortletJspBean" />

<% subscriptionPortletNewsletter.init( request, subscriptionPortletNewsletter.RIGHT_MANAGE_ADMIN_SITE ); %>
<%
	response.sendRedirect( subscriptionPortletNewsletter.doModify( request ) );
%>


