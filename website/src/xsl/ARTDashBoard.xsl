<?xml version="1.0"?>

<xsl:stylesheet version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
 
<!-- the following variables may not be used in this stylesheet, but will be used by importing ones -->
<xsl:variable name="dashboarddatanode" select="/Page/Payload"/>

<xsl:template name="DashBoardTemplate">
  <xsl:param name="panelnode"/>
  <xsl:apply-templates select="$panelnode" mode="ARTDashBoard"/>
</xsl:template>
  
<xsl:template match="*|/" mode="ARTDashBoard">
</xsl:template>

</xsl:stylesheet>

