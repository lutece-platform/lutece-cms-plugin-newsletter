<?xml version="1.0" ?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="html" indent="yes"/>
    <xsl:param name="site-path" select="site-path" />
    <xsl:variable name="portlet-id" select="portlet/portlet-id" />
    <xsl:variable name="e-mail-error" select="portlet/newsletter-email-error" />
    <xsl:variable name="nochoice-error" select="portlet/subscription-nochoice-error" />
    <xsl:template match="portlet">
        <xsl:variable name="device_class">
            <xsl:choose>
                <xsl:when test="string(display-on-small-device)='0'">hide-for-small</xsl:when>
                <xsl:otherwise></xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <div class="portlet {$device_class}">
            <xsl:if test="not(string(display-portlet-title)='1')">
                <h3 class="portlet-header">
                    <xsl:value-of disable-output-escaping="yes" select="portlet-name" />
                </h3>
            </xsl:if>
            <div class="portlet-content">
                <form class="form" id="newsletter" action="{$site-path}" method="post">
                    <input name="page" value="newsletter" type="hidden" />
                    <input name="action" value="register" type="hidden" />
                    <input name="plugin_name" value="newsletter" type="hidden" />
                    <label for="email">
                        <xsl:value-of select="newsletter-subscription-email" />
                    </label>
                    <input name="email" id="email" maxlength="100" type="text" class="input-large" />
                    <xsl:apply-templates select="newsletter-subscription-list" />   
                    <xsl:if test="not(string(newsletter-subscription-tos)='')">
                        <label class="checkbox" for="top">
                            <input type="checkbox" name="tos" id="tos" value="1" />
                            #i18n{newsletter.siteMessage.tos.title}
							<button class="btn btn-link" type="button" data-toggle="modal" data-target="#requirementModal"> #i18n{newsletter.page_newsletter.tos.header}</button>
                        </label>
                        <div id="requirementModal" class="modal fade" tabindex="-1" aria-labelledby="requirementLabel" aria-hidden="true">
							<div class="modal-dialog">
								<div class="modal-content">
									<div class="modal-header">
										<h2 id="requirementLabel">#i18n{newsletter.page_newsletter.tos.header}</h2>
										<button type="button" class="close" data-dismiss="modal" aria-hidden="true">X</button>
									</div>
									<div class="modal-body p-3">
										<xsl:value-of disable-output-escaping="yes" select="newsletter-subscription-tos-content" />
									</div>
									<div class="modal-footer">
										<button class="btn" data-dismiss="modal" aria-hidden="true">#i18n{portal.util.labelBack}</button>
									</div>
								</div>
							</div>
                        </div>
                    </xsl:if>							
                    <xsl:if test="not(string(newsletter-subscription-captcha)='')">
                        <xsl:value-of disable-output-escaping="yes" select="newsletter-subscription-captcha" />
                    </xsl:if>						
                    <button type="submit" class="btn btn-primary">
                        <i class="icon-plus icon-white">&#160;</i>&#160;
                        <xsl:value-of select="newsletter-subscription-button" />
                    </button>
                    <input name="portlet_id" value="{portlet-id}" type="hidden" />
                </form>
            </div>
        </div>
    </xsl:template>

    <xsl:template match="newsletter-subscription-list">
        <xsl:if test="newsletter-subscription">
            <ul>
                <xsl:apply-templates select="newsletter-subscription" />
            </ul>
        </xsl:if>
    </xsl:template>

    <xsl:template match="newsletter-subscription">
        <li>
            <label class="checkbox" for="newsletter_id_{newsletter-subscription-id}">
                <input type="checkbox" class="checkbox-field" name="newsletter_id" id="newsletter_id_{newsletter-subscription-id}" value="{newsletter-subscription-id}" checked="checked" />
                <xsl:value-of select="newsletter-subscription-subject" />
            </label>
        </li>
    </xsl:template>
</xsl:stylesheet>