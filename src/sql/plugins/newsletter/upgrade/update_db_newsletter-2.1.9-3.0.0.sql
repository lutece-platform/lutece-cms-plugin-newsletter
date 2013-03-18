DROP TABLE IF EXISTS newsletter_section;
CREATE TABLE newsletter_section (
  id_section INT,
  id_newsletter INT NOT NULL,
  section_type VARCHAR(100) NOT NULL,
  title VARCHAR(255) DEFAULT '',
  section_order INT NOT NULL,
  category INT NOT NULL,
  PRIMARY KEY (id_section)
);

DROP TABLE IF EXISTS newsletter_section_free_html;
CREATE TABLE newsletter_section_free_html (
  id_section INT,
  html_content LONG VARCHAR,
  PRIMARY KEY (id_section)
);

ALTER TABLE newsletter_description ADD COLUMN nb_categories INT DEFAULT '1';

ALTER TABLE newsletter_template ADD COLUMN section_type VARCHAR(100) NOT NULL;
ALTER TABLE newsletter_template ADD COLUMN categories INT DEFAULT '1' NOT NULL;
UPDATE newsletter_template SET section_type = 'NEWSLETTER_TEMPLATE' WHERE template_type = 0;
UPDATE newsletter_template SET section_type = 'NEWSLETTER_DOCUMENT' WHERE template_type = 1;
ALTER TABLE newsletter_template DROP COLUMN template_type;
RENAME TABLE newsletter_category_list TO newsletter_document_category;
ALTER TABLE newsletter_document_category DROP PRIMARY KEY;
ALTER TABLE newsletter_document_category CHANGE COLUMN id_category_list id_category INT NOT NULL;
ALTER TABLE newsletter_document_category CHANGE COLUMN id_newsletter id_section INT NOT NULL;
ALTER TABLE newsletter_document_category ADD PRIMARY KEY (id_category, id_section);
