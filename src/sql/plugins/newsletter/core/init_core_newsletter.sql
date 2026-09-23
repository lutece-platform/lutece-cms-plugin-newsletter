-- liquibase formatted sql
-- changeset newsletter:init_core_newsletter.sql
-- preconditions onFail:MARK_RAN onError:WARN
--
-- Dumping data for table core_admin_right
--

INSERT INTO core_admin_right (id_right,name,level_right,admin_url,description,is_updatable,plugin_name,id_feature_group,icon_url,documentation_url) VALUES ('NEWSLETTER_MANAGEMENT','newsletter.adminFeature.newsletter_management.name',2,'jsp/admin/plugins/newsletter/ManageNewsLetter.jsp','newsletter.adminFeature.newsletter_management.description',0,'newsletter','CONTENT','ti ti-news', NULL);
INSERT INTO core_admin_right (id_right,name,level_right,admin_url,description,is_updatable,plugin_name,id_feature_group,icon_url,documentation_url) VALUES ('NEWSLETTER_TEMPLATE_MANAGEMENT','newsletter.adminFeature.newsletter_template_management.name',2,'jsp/admin/plugins/newsletter/ManageTemplates.jsp','newsletter.adminFeature.newsletter_template_management.description',0,'newsletter','STYLE','ti ti-template', NULL);

--
-- Dumping data for table core_admin_role
--

INSERT INTO core_admin_role VALUES ('newsletter_manager','The role needed to access a newsletter template');


--
-- Dumping data for table core_admin_role_resource
--

INSERT INTO core_admin_role_resource (role_key,resource_type,resource_id,permission) VALUES ('newsletter_manager','NEWSLETTER','*','*');
INSERT INTO core_admin_role_resource (role_key,resource_type,resource_id,permission) VALUES ('newsletter_manager','NEWSLETTER_TEMPLATE','*','*');

 
--
-- Dumping data for table core_portlet_type
--

INSERT INTO core_portlet_type VALUES ('NEWSLETTER_ARCHIVE_PORTLET','newsletter.portlet.name','plugins/newsletter/CreatePortletNewsletter.jsp','plugins/newsletter/ModifyPortletNewsletter.jsp','fr.paris.lutece.plugins.newsletter.business.portlet.NewsLetterArchivePortletHome','newsletter','plugins/newsletter/DoCreatePortletNewsletter.jsp','/admin/portlet/script_create_portlet.html','','','plugins/newsletter/DoModifyPortletNewsletter.jsp','/admin/portlet/script_modify_portlet.html','/admin/plugins/newsletter/newsletter_sending_list.html','','archive');
INSERT INTO core_portlet_type VALUES ('NEWSLETTER_SUBSCRIPTION_PORTLET','newsletter.portlet.subscription.name','plugins/newsletter/CreateSubscriptionPortletNewsletter.jsp','plugins/newsletter/ModifySubscriptionPortletNewsletter.jsp','fr.paris.lutece.plugins.newsletter.business.portlet.NewsLetterSubscriptionPortletHome','newsletter','plugins/newsletter/DoCreateSubscriptionPortletNewsletter.jsp','/admin/portlet/script_create_portlet.html','','','plugins/newsletter/DoModifySubscriptionPortletNewsletter.jsp','/admin/portlet/script_modify_portlet.html','/admin/plugins/newsletter/newsletter_subscription_list.html','','news');

--
-- Dumping data for table core_user_right
--

INSERT INTO core_user_right (id_right,id_user) VALUES ('NEWSLETTER_TEMPLATE_MANAGEMENT',1);
INSERT INTO core_user_right (id_right,id_user) VALUES ('NEWSLETTER_MANAGEMENT',1);
INSERT INTO core_user_right (id_right,id_user) VALUES ('NEWSLETTER_MANAGEMENT',2);

--
-- Dumping data for table core_user_role
--

INSERT INTO core_user_role VALUES ('newsletter_manager',1);
INSERT INTO core_user_role VALUES ('newsletter_manager',2);

--
-- FreeMarker templates available for the newsletter portlets, registered in the core (Section Template Management feature).
-- They replace the XSL styles 1100 (Newsletter-Archives) and 1101 (Newsletter-Subscription) of the former versions.
--
-- changeset newsletter:init_core_newsletter.sql-rev1.sql
-- preconditions onFail:MARK_RAN onError:WARN
-- precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM core_portlet_template WHERE id_portlet_type IN ('NEWSLETTER_ARCHIVE_PORTLET', 'NEWSLETTER_SUBSCRIPTION_PORTLET')
INSERT INTO core_portlet_template (id_portlet_type, description, template_path) VALUES ('NEWSLETTER_ARCHIVE_PORTLET', 'Newsletter - Archives', 'skin/plugins/newsletter/portlet/newsletter_archive_portlet.html');
INSERT INTO core_portlet_template (id_portlet_type, description, template_path) VALUES ('NEWSLETTER_SUBSCRIPTION_PORTLET', 'Newsletter - Souscription', 'skin/plugins/newsletter/portlet/newsletter_subscription_portlet.html');

--
-- The sendings / newsletters selection is now the specific part of the core creation form (no more override of admin/portlet/create_portlet.html)
--
-- changeset newsletter:init_core_newsletter.sql-rev2.sql
-- preconditions onFail:MARK_RAN onError:WARN
UPDATE core_portlet_type SET create_specific = '/admin/plugins/newsletter/newsletter_sending_list.html' WHERE id_portlet_type = 'NEWSLETTER_ARCHIVE_PORTLET';
UPDATE core_portlet_type SET create_specific = '/admin/plugins/newsletter/newsletter_subscription_list.html' WHERE id_portlet_type = 'NEWSLETTER_SUBSCRIPTION_PORTLET';
