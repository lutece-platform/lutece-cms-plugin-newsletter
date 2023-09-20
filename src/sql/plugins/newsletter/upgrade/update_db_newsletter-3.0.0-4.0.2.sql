UPDATE core_admin_right SET icon_url='ti ti-news' WHERE  id_right='NEWSLETTER_MANAGEMENT';
UPDATE core_admin_right SET icon_url='ti ti-template' WHERE  id_right='NEWSLETTER_TEMPLATE_MANAGEMENT';
UPDATE core_portlet_type SET icon_name='archive' WHERE  id_portlet_type='NEWSLETTER_ARCHIVE_PORTLET';
UPDATE core_portlet_type SET icon_name='news' WHERE  id_portlet_type='NEWSLETTER_SUBSCRIPTION_PORTLET';