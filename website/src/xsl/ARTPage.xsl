<?xml version="1.0"?>

<xsl:stylesheet version="1.0" 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:import href="ARTDashBoard.xsl"    />
<xsl:import href="ARTLeftPanel.xsl" />
<xsl:import href="ARTBody.xsl"      />
<xsl:import href="ARTRightPanel.xsl"/>
<xsl:import href="ARTFooter.xsl"    />

<xsl:output version="1.0" method="html" encoding="ISO-8859-1" indent="yes"/>

<xsl:template match="/">
  <xsl:apply-templates select="Page"/>
</xsl:template>

<xsl:template match="Page">
  <html>
    <xsl:call-template name="setHtmlHead"/>
  </html>
</xsl:template>

<xsl:template name="setHtmlHead">
<head>
 <xsl:call-template name="setHtmlHeadTitle"  />
 <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1"/>
 <meta name="author" content="Will Webb"/>
 <link rel="stylesheet" type="text/css" href="css/default.css" title="default" media="screen"/>
</head>
</xsl:template>

<!-- Sets the head/title element value -->
<xsl:template name="setHtmlHeadTitle">
  <title>ART Client - Online statistical reporting for boiseoffice.com</title>
</xsl:template>

<xsl:template match="DashBoard">
  <xsl:call-template name="DashBoardTemplate">
      <xsl:with-param name="panelnode" select="/Page/DashBoard" />
  </xsl:call-template>
</xsl:template>
    
<xsl:template match="LeftPanel">
  <xsl:call-template name="LeftPanelTemplate">
    <xsl:with-param name="panelnode" select="/Page/LeftPanel" />
  </xsl:call-template>
</xsl:template>

<xsl:template match="Body">
  <xsl:call-template name="BodyTemplate">
    <xsl:with-param name="panelnode" select="/Page/Body" />
  </xsl:call-template>
</xsl:template>

<xsl:template match="Footer">
  <xsl:call-template name="FooterTemplate">
    <xsl:with-param name="panelnode" select="/Page/Footer" />
  </xsl:call-template>
</xsl:template>

</xsl:stylesheet>
