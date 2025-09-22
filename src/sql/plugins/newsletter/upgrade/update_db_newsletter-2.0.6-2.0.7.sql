-- liquibase formatted sql
-- changeset newsletter:update_db_newsletter-2.0.6-2.0.7.sql
-- preconditions onFail:MARK_RAN onError:WARN
--
-- Alter table structure for table newsletter_description
--

ALTER TABLE newsletter_description ADD COLUMN description LONG VARCHAR DEFAULT NULL AFTER name;