/*
 * Created on Nov 19, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.bos.art.logParser.records;


import com.bos.art.logParser.db.HtmlPageRecordPersistanceStrategy;
import com.bos.art.logParser.db.PersistanceStrategy;
import com.bos.art.logServer.utils.Base64;
import org.apache.commons.lang3.StringUtils;

/**
 * @author I0360D3
 *         <p/>
 *         TODO To change the template for this generated type comment go to
 *         Window - Preferences - Java - Code Style - Code Templates
 */
public class PageRecordEvent extends UserRequestEventDesc implements ILiveLogParserRecord {

    private String pageName;
    private String sessionId;
    private int requestToken;
    private int requestTokenCount;
    private String encodedPage;
    transient private AccessRecordsForeignKeys foreignKeys;
    transient private PersistanceStrategy pStrat;
                             
    public String getBrowser() {
        return null;
    }

    public int getLoadTime() {
        return 0;
    }

    public String getRemoteHost() {
        return null;
    }

    public boolean isAccessRecord() {
        return false;
    }

    public boolean isAccumulatorEvent() {
        return false;
    }

    public boolean isErrorPage() {
        return false;
    }

    public boolean isExternalAccessEvent() {
        return false;
    }

    public boolean isFirstTimeUser() {
        return false;
    }

    public boolean writeToDatabase() {
        return getPersistanceStrategy().writeToDatabase(this);
    }

    public String getEncodedPage() {
        return encodedPage;
    }

    public void setEncodedPage(String encodedPage) {
        if (StringUtils.isEmpty(pageName)) {
            this.encodedPage = encodedPage;
        } else {
            this.encodedPage = new String(Base64.decodeFast(encodedPage));
//            int hidden = this.encodedPage.indexOf("hidden");
//            if ( hidden>=0) {
//                Document doc = Jsoup.parse(this.encodedPage);
//                Elements inputElements = doc.select("input[type=hidden]");
//                boolean changed = false;
//                for(Element input:inputElements) {
//                    if (!input.attr("value").equals("")) {
//                        input.attr("value", "wiped");
//                        changed = true;
//                    }
//                }
//                if ( changed)
//                    this.encodedPage = doc.toString();
//            } else {
//                return;
//            }
        }
    }

    public String getPageName() {
        return pageName;
    }

    public void setPageName(String string) {
        pageName = string;

        if (StringUtils.isEmpty(pageName)) {
            // ignore this type of record, probably a TimingStopWatch event
            return;
        }

        try {
            int startPos = 0;
            if (pageName.charAt(0) == '/')
                startPos = 1;
            int endPos = pageName.indexOf("/", startPos);

            String mycontext = "null";
            if (pageName.equals("shop.OrderXML:shop/editOrder.xsl") || pageName.equals("ProductGroupXML:shop/productGroup.xsl")) {
                // if we see these pages we know the context is NASApp/boiseop
                mycontext = "NASApp/boiseop";
            } else {
                mycontext = pageName.substring(startPos, endPos);
                if (mycontext.equals("NASApp")) {
                    mycontext += "/" + pageName.substring(endPos + 1, pageName.indexOf("/", endPos + 1));
                    //System.out.println("context is: " + mycontext);
                }
                // should be left with ,shop or ,NASApp/boiseop, or integration
                //System.out.println("page is : " + page);
            }
            pageName = pageName.substring(pageName.indexOf(mycontext) + mycontext.length() + 1);
            setContext(mycontext);
        } catch (Exception e) {
            setContext("Exception_Getting Context");
            e.printStackTrace();
        }
    }

    public int getRequestToken() {
        return requestToken;
    }

    public void setRequestToken(int requestToken) {
        this.requestToken = requestToken;
    }

    public int getRequestTokenCount() {
        return requestTokenCount;
    }

    public void setRequestTokenCount(int requestTokenCount) {
        this.requestTokenCount = requestTokenCount;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }




    public AccessRecordsForeignKeys obtainForeignKeys() {
        if (foreignKeys == null) {
            foreignKeys = new AccessRecordsForeignKeys(getEventTime().getTime());
        }
        return foreignKeys;
    }

    public PersistanceStrategy getPersistanceStrategy() {
        if (pStrat == null) {
            pStrat = HtmlPageRecordPersistanceStrategy.getInstance();
        }
        return pStrat;
    }

}
