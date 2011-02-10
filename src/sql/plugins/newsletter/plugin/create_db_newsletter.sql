--
-- Table structure for table newsletter
--

DROP TABLE IF EXISTS newsletter_description;
CREATE TABLE newsletter_description (
  id_newsletter INT DEFAULT '0' NOT NULL,
  date_last_send TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
  name VARCHAR(50) DEFAULT NULL,
  description LONG VARCHAR DEFAULT NULL,
  html LONG VARCHAR,
  id_newsletter_template INT DEFAULT '0',
  id_document_template INT DEFAULT '0',
  workgroup_key VARCHAR(50) DEFAULT NULL,
  unsubscribe VARCHAR(6) DEFAULT NULL,
  test_recipients VARCHAR(255) DEFAULT NULL,
  sender_mail VARCHAR(255) DEFAULT NULL,
  sender_name VARCHAR(255) DEFAULT NULL,
  test_subject VARCHAR(255) DEFAULT NULL,
  PRIMARY KEY (id_newsletter)
);



--
-- Table structure for table newsletter_template
--

DROP TABLE IF EXISTS newsletter_template;
CREATE TABLE newsletter_template (
  id_template INT DEFAULT '0' NOT NULL,
  template_type SMALLINT DEFAULT '0',
  description VARCHAR(50) DEFAULT NULL,
  file_name VARCHAR(100) DEFAULT NULL,
  picture VARCHAR(100) DEFAULT NULL,
  workgroup_key VARCHAR(50) DEFAULT NULL,
  PRIMARY KEY (id_template)
);



--
-- Table structure for table sending_newsletter
--

DROP TABLE IF EXISTS newsletter_sending;
CREATE TABLE newsletter_sending (
  id_sending INT DEFAULT '0' NOT NULL,
  id_newsletter INT DEFAULT '0' NOT NULL,
  date_sending TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
  subscriber_count INT DEFAULT '0' NOT NULL,
  html LONG VARCHAR,
  email_subject VARCHAR(200) DEFAULT NULL,
  PRIMARY KEY (id_sending)
);


--
-- Table structure for table newsletter_subscriber_details
--

DROP TABLE IF EXISTS newsletter_subscriber_details;
CREATE TABLE newsletter_subscriber_details (
  id_subscriber INT DEFAULT '0' NOT NULL,
  email VARCHAR(100) DEFAULT NULL,
  PRIMARY KEY (id_subscriber)
);
CREATE INDEX index_subscriber ON newsletter_subscriber_details (email);


--
-- Table structure for table newsletter_subscriber
--

DROP TABLE IF EXISTS newsletter_subscriber;
CREATE TABLE newsletter_subscriber (
  id_subscriber INT DEFAULT '0' NOT NULL,
  id_newsletter INT DEFAULT '0' NOT NULL,
  confirmed INT DEFAULT '1' NOT NULL,
  date_subscription TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
  PRIMARY KEY (id_newsletter,id_subscriber)
);
CREATE INDEX index_newsletter_subscriber ON newsletter_subscriber (id_subscriber);


--
-- Table structure for table newsletter_category_list
--

DROP TABLE IF EXISTS newsletter_category_list;
CREATE TABLE newsletter_category_list (
  id_category_list INT DEFAULT '0' NOT NULL,
  id_newsletter INT DEFAULT '0' NOT NULL,
  PRIMARY KEY (id_category_list,id_newsletter)
);

--
-- Table structure for table newsletter_awaiting_confirmation
--
DROP TABLE IF EXISTS newsletter_awaiting_confirmation;
CREATE TABLE newsletter_awaiting_confirmation (
  id_user INT DEFAULT '0' NOT NULL,
  generated_key INT DEFAULT '0' NOT NULL,
  date_subscription TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
  PRIMARY KEY (id_user, generated_key)
);


--
-- Table structure for table newsletter_properties
--

DROP TABLE IF EXISTS newsletter_properties;
CREATE TABLE newsletter_properties (
  validation_activated INT DEFAULT '1' NOT NULL,
  captcha_activated INT DEFAULT '1' NOT NULL,
  tos long VARCHAR 
 );

