<?xml version="1.0"?>
<xsl:stylesheet version="1.0" 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:variable name="bodydatanode" select="/Page/Payload"/>

<xsl:template name="BodyTemplate">
  <xsl:param name="panelnode"/>
  <xsl:apply-templates select="$panelnode" mode="ARTBody"/>
</xsl:template>

</xsl:stylesheet>
