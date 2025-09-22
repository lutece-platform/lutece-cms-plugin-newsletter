-- liquibase formatted sql
-- changeset newsletter:update_db_newsletter-3.0.0-4.0.2.sql
-- preconditions onFail:MARK_RAN onError:WARN
UPDATE core_admin_right SET icon_url='ti ti-news' WHERE  id_right='NEWSLETTER_MANAGEMENT';
UPDATE core_admin_right SET icon_url='ti ti-template' WHERE  id_right='NEWSLETTER_TEMPLATE_MANAGEMENT';
UPDATE core_portlet_type SET icon_name='archive' WHERE  id_portlet_type='NEWSLETTER_ARCHIVE_PORTLET';
UPDATE core_portlet_type SET icon_name='news' WHERE  id_portlet_type='NEWSLETTER_SUBSCRIPTION_PORTLET';