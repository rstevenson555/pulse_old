<?xml version="1.0"?>

<xsl:stylesheet version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:output version="1.0" method="html" encoding="ISO-8859-1" indent="yes"/>

<xsl:variable name="leftpaneldatanode" select="/Page/Payload"/>

<xsl:template name="LeftPanelTemplate">
  <xsl:param name="panelnode"/>
  <xsl:apply-templates select="$panelnode" mode="ARTLeftPanel"/>
</xsl:template>

<xsl:template match="*|/" mode="ARTLeftPanel"/>

</xsl:stylesheet>


