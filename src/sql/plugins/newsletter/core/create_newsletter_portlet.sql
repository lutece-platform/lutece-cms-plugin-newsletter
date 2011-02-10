--
-- Creation of table newsletter_portlet_archive
--
CREATE TABLE newsletter_portlet_archive (  
	id_portlet INT DEFAULT '0' NOT NULL,  
	id_sending INT DEFAULT '0' NOT NULL,  
	PRIMARY KEY (id_portlet,id_sending)
);


--
-- Creation of table newsletter_portlet_subscribe
--
CREATE TABLE newsletter_portlet_subscribe (  
	id_portlet INT DEFAULT '0' NOT NULL,  
	id_newsletter INT DEFAULT '0' NOT NULL,  
	PRIMARY KEY (id_portlet,id_newsletter)
);