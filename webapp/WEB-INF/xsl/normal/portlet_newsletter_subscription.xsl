<?xml version="1.0"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:param name="site-path" select="site-path" />
	<xsl:variable name="portlet-id" select="portlet/portlet-id" />
	<xsl:variable name="e-mail-error" select="portlet/newsletter-email-error" />
	<xsl:variable name="nochoice-error" select="portlet/subscription-nochoice-error" />
	<xsl:template match="portlet">
		<div class="portlet -lutece-border-radius append-bottom">
			<xsl:if test="not(string(display-portlet-title)='1')">
				<div class="portlet-header">
					<xsl:value-of disable-output-escaping="yes" select="portlet-name" />
				</div>
			</xsl:if>
			<div class="portlet-content">
				<form class="default-form" id="newsletter" action="{$site-path}"
					method="post">
					<xsl:apply-templates select="newsletter-subscription-list" />
					<div>
						<input name="page" value="newsletter" type="hidden" />
						<input name="action" value="register" type="hidden" />
						<input name="plugin_name" value="newsletter" type="hidden" />
						<p>
							<label for="email">
								<xsl:value-of select="newsletter-subscription-email" />
							</label>
							<input name="email" id="email" maxlength="100" type="text" class="-lutece-input -lutece-border-radius-mini" />
							<xsl:if test="not(string(newsletter-subscription-tos)='')">								
							<xsl:text disable-output-escaping="yes">
								<![CDATA[<div>
										<label for="comment">
											<input type="checkbox" name="tos" id="tos" value="1" />
											<a href="jsp/site/RunStandaloneApp.jsp?page=newsletter&view_requirement=view_requirement" onclick="javascript:openFrontRequirement(this.href); return false;" target="_blank">#i18n{newsletter.page_newsletter.requirement}</a>
										</label>
									</div>  ]]>
							</xsl:text>
							</xsl:if>							
							<xsl:if test="not(string(newsletter-subscription-captcha)='')">
								<xsl:value-of disable-output-escaping="yes" select="newsletter-subscription-captcha" />
							</xsl:if>						
							<input value="{newsletter-subscription-button}" type="submit" class="-lutece-input -lutece-border-radius-mini" />
							<input name="portlet_id" value="{portlet-id}" type="hidden" />
						</p>
					</div>
				</form>
			</div>
		</div>
	</xsl:template>


	<xsl:template match="newsletter-subscription-list">
		<xsl:if test="newsletter-subscription">
			<p>
				<ul>
					<xsl:apply-templates select="newsletter-subscription" />
				</ul>
			</p>
		</xsl:if>
	</xsl:template>


	<xsl:template match="newsletter-subscription">
		<li>
			<input type="checkbox" class="checkbox-field" name="newsletter_id"
				id="newsletter_id_{newsletter-subscription-id}" value="{newsletter-subscription-id}"
				checked="checked" />
			<label for="newsletter_id_{newsletter-subscription-id}">
				<xsl:value-of select="newsletter-subscription-subject" />
			</label>
		</li>
	</xsl:template>

</xsl:stylesheet>

