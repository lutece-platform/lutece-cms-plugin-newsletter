-- liquibase formatted sql
-- changeset newsletter:init_db_newsletter.sql
-- preconditions onFail:MARK_RAN onError:WARN
--
-- Dumping data for table newsletter
--
INSERT INTO newsletter_description values (1,'2007-05-16 13:03:30','Lettre d''information','Lettre d''information pour l''ensemble des utilisateurs','Content',1,'all','TRUE','lutece@lutece.fr','lutece@lutece.fr','Lutece','Pour validation',2);

--
-- Dumping data for table newsletter_template
--

INSERT INTO newsletter_template VALUES (1,'newsletter_model','model_newsletter.html','model-newsletter.svg','all','NEWSLETTER_TEMPLATE',2);

--
-- Dumping data for table newsletter_properties
--
INSERT INTO newsletter_properties VALUES (1,1,null);
 