<?xml version="1.0"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:param name="site-path" select="site-path" />
	<xsl:variable name="portlet-id" select="portlet/portlet-id" />

	<xsl:template match="portlet">
		<div class="portlet -lutece-border-radius append-bottom">
			<xsl:if test="not(string(display-portlet-title)='1')">
				<h3 class="portlet-header">
					<xsl:value-of disable-output-escaping="yes" select="portlet-name" />
				</h3>
			</xsl:if>
			<div class="portlet-content">
				<xsl:apply-templates select="newsletter-sending-list" />
			</div>
		</div>
	</xsl:template>


	<xsl:template match="newsletter-sending-list">
		<ul>
			<xsl:apply-templates select="newsletter-sending" />
		</ul>
	</xsl:template>


	<xsl:template match="newsletter-sending">
		<li>
			<xsl:value-of select="newsletter-sending-date" />
			:
			<a
				href="jsp/site/plugins/newsletter/ViewNewsletterArchive.jsp?sending_id={newsletter-sending-id}"
				target="_top">
				<b>
					<xsl:value-of select="newsletter-sending-subject" />
				</b>
			</a>
			<br />
		</li>
	</xsl:template>

</xsl:stylesheet>

