<?xml version="1.0"?>

<xsl:stylesheet version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:output version="1.0" method="html" encoding="ISO-8859-1" indent="yes"/>
  
<xsl:variable name="rightpaneldatanode" select="/Page/Payload"/>

<xsl:template name="RightPanelTemplate">
<xsl:param name="panelnode"/>
  <xsl:apply-templates select="$panelnode" mode="ARTRightPanel"/>
</xsl:template>

<xsl:template match="*|/" mode="BoiseOfficeRightPanel"/>

</xsl:stylesheet>


