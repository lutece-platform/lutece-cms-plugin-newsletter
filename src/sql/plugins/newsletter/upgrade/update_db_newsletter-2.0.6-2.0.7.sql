--
-- Alter table structure for table newsletter_description
--

ALTER TABLE newsletter_description ADD COLUMN description LONG VARCHAR DEFAULT NULL AFTER name;