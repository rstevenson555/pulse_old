/* $Id$
 * 
 * $Author$
 * $Date$
 * $Revision$
 *
 * This class is responsible for mapping config elements from the struts-config.xml file at runtime.
 * 
 * Copyright 2003 Boise Cascade Office Products Inc. All rights reserved.
 *
 */
package com.bos.config.struts;

import com.bcop.arch.logger.Logger;
import org.apache.struts.action.ActionMapping;

public class BoiseActionMapping extends ActionMapping
{
    public BoiseActionMapping () {
        if (logger.isDebugEnabled()) {
            logger.debug("BoiseActionConfig constructed: ");
        }
    }
    
    private static final Logger logger = 
        (Logger)Logger.getLogger(BoiseActionMapping.class.getName());
    private String styleSheet = null;

    public void setStyleSheet(String s) {
        if (logger.isDebugEnabled()) {
            logger.debug("set styleSheet: "+s);
        }
        this.styleSheet = s;
    }

    public String getStyleSheet() {
        if (logger.isDebugEnabled()) {
            logger.debug("get styleSheet: "+this.styleSheet);
        }
        return this.styleSheet;
    }

}
