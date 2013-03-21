DROP TABLE IF EXISTS newsletter_topic;
CREATE TABLE newsletter_topic (
  id_topic INT,
  id_newsletter INT NOT NULL,
  topic_type VARCHAR(100) NOT NULL,
  title VARCHAR(255) DEFAULT '',
  topic_order INT NOT NULL,
  section INT NOT NULL,
  PRIMARY KEY (id_topic)
);

DROP TABLE IF EXISTS newsletter_topic_free_html;
CREATE TABLE newsletter_topic_free_html (
  id_topic INT,
  html_content LONG VARCHAR,
  PRIMARY KEY (id_topic)
);

ALTER TABLE newsletter_description ADD COLUMN nb_sections INT DEFAULT '1';
ALTER TABLE newsletter_description DROP COLUMN id_document_template;

ALTER TABLE newsletter_template ADD COLUMN topic_type VARCHAR(100) NOT NULL;
ALTER TABLE newsletter_template ADD COLUMN sections INT DEFAULT '1' NOT NULL;
UPDATE newsletter_template SET topic_type = 'NEWSLETTER_TEMPLATE' WHERE template_type = 0;
UPDATE newsletter_template SET topic_type = 'NEWSLETTER_DOCUMENT' WHERE template_type = 1;
ALTER TABLE newsletter_template DROP COLUMN template_type;
RENAME TABLE newsletter_category_list TO newsletter_document_category;
ALTER TABLE newsletter_document_category DROP PRIMARY KEY;
ALTER TABLE newsletter_document_category CHANGE COLUMN id_category_list id_category INT NOT NULL;
ALTER TABLE newsletter_document_category CHANGE COLUMN id_newsletter id_topic INT NOT NULL;
ALTER TABLE newsletter_document_category ADD PRIMARY KEY (id_topic, id_category);

DELETE FROM core_admin_right WHERE id_right = 'NEWSLETTER_PROPERTIES_MANAGEMENT';
DELETE FROM core_user_right WHERE id_right = 'NEWSLETTER_PROPERTIES_MANAGEMENT';