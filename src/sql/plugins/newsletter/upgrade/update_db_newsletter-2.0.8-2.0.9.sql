-- liquibase formatted sql
-- changeset newsletter:update_db_newsletter-2.0.8-2.0.9.sql
-- preconditions onFail:MARK_RAN onError:WARN
--
-- Alter table structure for table newsletter_portlet_subscription
--
RENAME TABLE newsletter_portlet_subscription TO newsletter_portlet_subscribe;