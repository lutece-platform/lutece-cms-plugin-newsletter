-- liquibase formatted sql
-- changeset newsletter:update_db_core_newsletter-4.0.4-5.0.0.sql
-- preconditions onFail:MARK_RAN onError:WARN
-- precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = database() AND table_name = 'core_portlet' AND column_name = 'id_template'
--
-- The XSL based rendering has been removed : every newsletter portlet is now rendered with a FreeMarker template
-- chosen per portlet among the templates registered in the core for the portlet type (core_portlet_template,
-- core_portlet.id_template, Section Template Management feature). The old XSL styles are mapped to the matching templates.
--
-- The plugin upgrade scripts run BEFORE the core upgrade script in the same liquibase run (sql/plugins/* sorts before sql/upgrade/*) :
-- the core structures are created here when they do not exist yet, with the very same statements as the core script, which is then skipped.
--
ALTER TABLE core_portlet ADD COLUMN id_template int default 0 NOT NULL;

-- changeset newsletter:update_db_core_newsletter-4.0.4-5.0.0.sql-rev1.sql
-- preconditions onFail:MARK_RAN onError:WARN
-- precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = database() AND table_name = 'core_portlet_template'
CREATE TABLE IF NOT EXISTS core_portlet_template (
	id_template int AUTO_INCREMENT NOT NULL,
	id_portlet_type varchar(50) default NULL,
	description varchar(255) default NULL,
	template_path varchar(255) default NULL,
	PRIMARY KEY (id_template)
);

--
-- Templates available for the newsletter portlets
--
-- changeset newsletter:update_db_core_newsletter-4.0.4-5.0.0.sql-rev2.sql
-- preconditions onFail:MARK_RAN onError:WARN
-- precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM core_portlet_template WHERE id_portlet_type IN ('NEWSLETTER_ARCHIVE_PORTLET', 'NEWSLETTER_SUBSCRIPTION_PORTLET')
INSERT INTO core_portlet_template (id_portlet_type, description, template_path) VALUES ('NEWSLETTER_ARCHIVE_PORTLET', 'Newsletter - Archives', 'skin/plugins/newsletter/portlet/newsletter_archive_portlet.html');
INSERT INTO core_portlet_template (id_portlet_type, description, template_path) VALUES ('NEWSLETTER_SUBSCRIPTION_PORTLET', 'Newsletter - Souscription', 'skin/plugins/newsletter/portlet/newsletter_subscription_portlet.html');

--
-- Template chosen for each portlet : XSL style 1100 (Newsletter-Archives) -> archive template, XSL style 1101 (Newsletter-Subscription) -> subscription template.
-- Each type had a single style, so every portlet of the type takes the template of its type.
--
-- changeset newsletter:update_db_core_newsletter-4.0.4-5.0.0.sql-rev3.sql
-- preconditions onFail:MARK_RAN onError:WARN
UPDATE core_portlet SET id_template = (SELECT MIN(id_template) FROM core_portlet_template WHERE id_portlet_type = 'NEWSLETTER_ARCHIVE_PORTLET' AND template_path = 'skin/plugins/newsletter/portlet/newsletter_archive_portlet.html') WHERE id_portlet_type = 'NEWSLETTER_ARCHIVE_PORTLET';
UPDATE core_portlet SET id_template = (SELECT MIN(id_template) FROM core_portlet_template WHERE id_portlet_type = 'NEWSLETTER_SUBSCRIPTION_PORTLET' AND template_path = 'skin/plugins/newsletter/portlet/newsletter_subscription_portlet.html') WHERE id_portlet_type = 'NEWSLETTER_SUBSCRIPTION_PORTLET';
UPDATE core_portlet SET id_style = 0 WHERE id_portlet_type IN ('NEWSLETTER_ARCHIVE_PORTLET', 'NEWSLETTER_SUBSCRIPTION_PORTLET');

-- changeset newsletter:update_db_core_newsletter-4.0.4-5.0.0.sql-rev4.sql
-- preconditions onFail:MARK_RAN onError:WARN
-- comment Legacy XSL style tables left the core for plugin-xmltransformer and are absent from many databases: skip instead of failing the whole update
-- precondition-sql-check expectedResult:3 SELECT COUNT(1) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = database() AND TABLE_NAME IN ('core_style_mode_stylesheet', 'core_stylesheet', 'core_style')
DELETE FROM core_style_mode_stylesheet WHERE id_style IN (1100, 1101);
DELETE FROM core_style WHERE id_style IN (1100, 1101);
DELETE FROM core_stylesheet WHERE id_stylesheet IN (400, 401);

--
-- The sendings / newsletters selection is now the specific part of the core creation form (no more override of admin/portlet/create_portlet.html)
--
-- changeset newsletter:update_db_core_newsletter-4.0.4-5.0.0.sql-rev5.sql
-- preconditions onFail:MARK_RAN onError:WARN
UPDATE core_portlet_type SET create_specific = '/admin/plugins/newsletter/newsletter_sending_list.html' WHERE id_portlet_type = 'NEWSLETTER_ARCHIVE_PORTLET';
UPDATE core_portlet_type SET create_specific = '/admin/plugins/newsletter/newsletter_subscription_list.html' WHERE id_portlet_type = 'NEWSLETTER_SUBSCRIPTION_PORTLET';
