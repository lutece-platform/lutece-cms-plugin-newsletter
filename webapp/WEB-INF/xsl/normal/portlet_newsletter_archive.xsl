<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:param name="site-path" select="site-path" />
    <xsl:variable name="portlet-id" select="portlet/portlet-id" />
    
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
                <xsl:apply-templates select="newsletter-sending-list" />
            </div>
        </div>
    </xsl:template>


    <xsl:template match="newsletter-sending-list">
        <ul class="unstyled">
            <xsl:apply-templates select="newsletter-sending" />
        </ul>
    </xsl:template>


    <xsl:template match="newsletter-sending">
        <li>
            <span class="label label-info">
                <xsl:value-of select="newsletter-sending-date" />
            </span>
            <a href="jsp/site/plugins/newsletter/ViewNewsletterArchive.jsp?sending_id={newsletter-sending-id}" target="_blank">
               <strong>
                    <xsl:value-of select="newsletter-sending-subject" />
               </strong>
            </a> 
        </li>
    </xsl:template>

</xsl:stylesheet>

