<?xml version="1.0"?>

<xsl:stylesheet version="1.0"
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<!-- Diversity Settings -->
<!-- General Diversity Text/Icon -->
<xsl:variable name="MWBE_Text" select="'MWBE (general diversity): This product is manufactured by a Minority and Women -owned Business Enterprise, part of our Supplier Diversity program.'"/>
<xsl:variable name="MWBE_Icon" select="'images/general/i_mwbe.gif'"/>

<!-- Minority Owned Business -->
<xsl:variable name="MBE_Text" select="'MBE: This product is manufactured by a Minority-owned Business Enterprise, part of our Supplier Diversity program.'"/>
<xsl:variable name="MBE_Icon" select="'images/general/i_mbe.gif'"/>

<!-- Women Owned Business -->
<xsl:variable name="WBE_Text" select="'WBE: This product is manufactured by a Women-owned Business Enterprise, part of our Supplier Diversity program.'"/>
<xsl:variable name="WBE_Icon" select="'images/general/i_wbe.gif'"/>

<!-- Phisically Challenged Business -->
<xsl:variable name="PC_Text" select="'PHYSICALLY CHALLENGED: This product is manufactured by a non-profit organization providing employment and training for Persons with Physical and Developmental Challenges. It is part of our Supplier Diversity program.'"/>
<xsl:variable name="PC_Icon" select="'images/general/i_pc.gif'"/>

<!-- Javits-Wagner-O Day Act Business -->
<xsl:variable name="JWOD_Text" select="'JWOD: This product is manufactured by a non-profit organization providing employment and training for Persons who are Blind or have other Severe Disabilities, under the Javits-Wagner-O`Day Act. It is part of our Supplier Diversity program.'"/>
<xsl:variable name="JWOD_Icon" select="'images/general/i_jwod.gif'"/>

<!-- Other filter attributes -->

<!-- Small Business -->
<xsl:variable name="SB_Text" select="'SMALL BUSINESS: This product is manufactured by a Small Business which qualifies for certain government set-aside programs.'"/>
<xsl:variable name="SB_Icon" select="'images/general/i_sb.gif'"/>

<!-- Recycled Items -->
<xsl:variable name="Recy_Text" select="'RECYCLED: This item is recycled.'"/>
<xsl:variable name="Recy_Icon" select="'images/general/i_recycle.gif'"/>

<!-- Promotional Items -->
<xsl:variable name="Promo_Text" select="'This is a promotional item.'"/>
<xsl:variable name="Promo_Icon" select="'images/general/i_save.gif'"/>

<!-- Contract Items -->
<xsl:variable name="Contract_Text" select="'CONTRACT: This item is on your company`s contract.'"/>
<xsl:variable name="Contract_Icon" select="'images/general/i_contract.gif'"/>

<!-- Restricted Items -->
<xsl:variable name="Restrict_Text" select="'RESTRICTED: Your company has restricted you from ordering this item.'"/>
<xsl:variable name="Restrict_Icon" select="'images/general/i_restricted.gif'"/>

<!-- SOS Items -->
<xsl:variable name="SOS_Text" select="'SOS: This item is in the SOS catalog.'" />
<xsl:variable name="SOS_Icon" select="'images/general/i_sos.gif'" />

<xsl:variable name="Imprint_Text" select="'IMPRINT: Custom Print Items must be personalized individually'" />
<xsl:variable name="Imprint_Icon" select="'images/shop/printicon.gif'" />

  <xsl:template match="Diversity">
	<xsl:if test="Text">
	<xsl:element name="A">
		<xsl:attribute name="href">
			<xsl:choose>
				<xsl:when test="Text='MWBE'">javascript:showLegend();</xsl:when>
				<xsl:when test="Text='MBE'">javascript:showLegend();</xsl:when>
				<xsl:when test="Text='WBE'">javascript:showLegend();</xsl:when>
				<xsl:when test="Text='PC'">javascript:showLegend();</xsl:when>
				<xsl:when test="Text='JWOD'">javascript:showLegend();</xsl:when>
			</xsl:choose>
		</xsl:attribute>
		<xsl:element name="IMG">
			<xsl:attribute name="SRC">
				<xsl:choose>
					<xsl:when test="Text='MWBE'"><xsl:value-of select="$MWBE_Icon"/></xsl:when>
					<xsl:when test="Text='MBE'"><xsl:value-of select="$MBE_Icon"/></xsl:when>
					<xsl:when test="Text='WBE'"><xsl:value-of select="$WBE_Icon"/></xsl:when>
					<xsl:when test="Text='PC'"><xsl:value-of select="$PC_Icon"/></xsl:when>
					<xsl:when test="Text='JWOD'"><xsl:value-of select="$JWOD_Icon"/></xsl:when>
				</xsl:choose>
			</xsl:attribute>
			<xsl:attribute name="alt"></xsl:attribute>
			<xsl:attribute name="HSPACE">6</xsl:attribute>
			<xsl:attribute name="VSPACE">2</xsl:attribute>
			<xsl:attribute name="BORDER">0</xsl:attribute>
		</xsl:element>
	</xsl:element>
    </xsl:if>
    <xsl:if test="position() mod 3=0"><BR/></xsl:if>
  </xsl:template>

  <xsl:template match="Promo[@isPromo='true']">
	<xsl:element name="A">
		<xsl:attribute name="href">javascript:showLegend();</xsl:attribute>
		<xsl:element name="IMG">
			<xsl:attribute name="SRC"><xsl:value-of select="$Promo_Icon"/></xsl:attribute>
			<xsl:attribute name="alt"></xsl:attribute>
			<xsl:attribute name="HSPACE">6</xsl:attribute>
			<xsl:attribute name="VSPACE">2</xsl:attribute>
			<xsl:attribute name="BORDER">0</xsl:attribute>
		</xsl:element>
	</xsl:element>
    <xsl:if test="position() mod 3=0"><BR/></xsl:if>
  </xsl:template>

  <xsl:template match="Contract[@isOnContract='true']">
	<xsl:element name="A">
		<xsl:attribute name="href">javascript:showLegend();</xsl:attribute>
		<xsl:element name="IMG">
			<xsl:attribute name="SRC"><xsl:value-of select="$Contract_Icon"/></xsl:attribute>
			<xsl:attribute name="alt"></xsl:attribute>
			<xsl:attribute name="HSPACE">6</xsl:attribute>
			<xsl:attribute name="VSPACE">2</xsl:attribute>
			<xsl:attribute name="BORDER">0</xsl:attribute>
		</xsl:element>
	</xsl:element>
    <xsl:if test="position() mod 3=0"><BR/></xsl:if>
  </xsl:template>

  <xsl:template match="Recycle[@isRecycleable='true']">

	<xsl:element name="A">
		<xsl:attribute name="href">javascript:showLegend();</xsl:attribute>
		<xsl:element name="IMG">
			<xsl:attribute name="SRC"><xsl:value-of select="$Recy_Icon"/></xsl:attribute>
			<xsl:attribute name="alt"></xsl:attribute>
			<xsl:attribute name="HSPACE">6</xsl:attribute>
			<xsl:attribute name="VSPACE">2</xsl:attribute>
			<xsl:attribute name="BORDER">0</xsl:attribute>
		</xsl:element>
	</xsl:element>
    <xsl:if test="position() mod 3=0"><BR/></xsl:if>
  </xsl:template>

  <xsl:template match="Restricted[@isRestricted='true']">
	<xsl:element name="A">
		<xsl:attribute name="href">javascript:showLegend();</xsl:attribute>
		<xsl:element name="IMG">
			<xsl:attribute name="SRC"><xsl:value-of select="$Restrict_Icon"/></xsl:attribute>
			<xsl:attribute name="alt"></xsl:attribute>
			<xsl:attribute name="HSPACE">6</xsl:attribute>
			<xsl:attribute name="VSPACE">2</xsl:attribute>
			<xsl:attribute name="BORDER">0</xsl:attribute>
		</xsl:element>
	</xsl:element>
    <xsl:if test="position() mod 3=0"><BR/></xsl:if>
  </xsl:template>

  <xsl:template match="SmallBusiness[@isSmallBusiness='true']">
	<xsl:element name="A">
		<xsl:attribute name="href">javascript:showLegend();</xsl:attribute>
		<xsl:element name="IMG">
			<xsl:attribute name="SRC"><xsl:value-of select="$SB_Icon"/></xsl:attribute>
			<xsl:attribute name="alt"></xsl:attribute>
			<xsl:attribute name="HSPACE">6</xsl:attribute>
			<xsl:attribute name="VSPACE">2</xsl:attribute>
			<xsl:attribute name="BORDER">0</xsl:attribute>
		</xsl:element>
	</xsl:element>
    <xsl:if test="position() mod 3=0"><BR/></xsl:if>
  </xsl:template>

  <xsl:template match="SOS[@isSOS='true']">
	<xsl:element name="A">
		<xsl:attribute name="href">javascript:showLegend();</xsl:attribute>
		<xsl:element name="IMG">
			<xsl:attribute name="SRC"><xsl:value-of select="$SOS_Icon"/></xsl:attribute>
			<xsl:attribute name="alt"></xsl:attribute>
			<xsl:attribute name="HSPACE">6</xsl:attribute>
			<xsl:attribute name="VSPACE">2</xsl:attribute>
			<xsl:attribute name="BORDER">0</xsl:attribute>
		</xsl:element>
	</xsl:element>
    <xsl:if test="position() mod 3=0"><BR/></xsl:if>
  </xsl:template>

  <xsl:template match="ImprintItem[@isImprintItem='true']">
	<xsl:element name="A">
		<xsl:attribute name="href">javascript:showLegend();</xsl:attribute>
		<xsl:element name="IMG">
			<xsl:attribute name="SRC"><xsl:value-of select="$Imprint_Icon"/></xsl:attribute>
			<xsl:attribute name="alt"></xsl:attribute>
			<xsl:attribute name="HSPACE">6</xsl:attribute>
			<xsl:attribute name="VSPACE">2</xsl:attribute>
			<xsl:attribute name="BORDER">0</xsl:attribute>
		</xsl:element>
	</xsl:element>
    <xsl:if test="position() mod 3=0"><BR/></xsl:if>
  </xsl:template>

  <xsl:template match="itemComments">
      <xsl:element name="A">
          <xsl:attribute name="href">javascript:alert('<xsl:value-of select="Text"/>');</xsl:attribute>
          <xsl:element name="IMG">
              <xsl:attribute name="SRC">images/shop/comments_on.gif</xsl:attribute>
              <xsl:attribute name="alt"></xsl:attribute>
              <xsl:attribute name="HSPACE">6</xsl:attribute>
              <xsl:attribute name="VSPACE">2</xsl:attribute>
              <xsl:attribute name="BORDER">0</xsl:attribute>
          </xsl:element>
      </xsl:element>
      <xsl:if test="position() mod 3=0"><BR/></xsl:if>
  </xsl:template>
</xsl:stylesheet>
