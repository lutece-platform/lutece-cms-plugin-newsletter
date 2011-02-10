--
-- Alter table structure for table newsletter_subscriber
--
ALTER TABLE newsletter_subscriber ADD COLUMN confirmed INT DEFAULT '1' NOT NULL;

--
-- Table structure for table newsletter_awaiting_confirmation
-- date_subscription can be used for deleting old entries
--
CREATE TABLE newsletter_awaiting_confirmation (
  id_user INT DEFAULT '0' NOT NULL,
  generated_key INT DEFAULT '0' NOT NULL,
  date_subscription TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
  PRIMARY KEY (id_user, generated_key)
);