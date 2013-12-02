/*
 * Created on Aug 23, 2004
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package com.bos.art.logParser.records;

import com.bos.art.logParser.collector.QueryParameterWriteQueue;
import com.bos.art.logParser.db.AccessRecordPersistanceStrategy;
import com.bos.art.logParser.db.ForeignKeyStore;
import com.bos.art.logParser.db.PersistanceStrategy;

import java.io.UnsupportedEncodingException;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

/**
 * @author I0360D3
 *
 * To change the template for this generated type comment go to Window>Preferences>Java>Code Generation>Code and Comments
 */
public class QueryParameters {

    private static final Logger logger = (Logger) Logger.getLogger(QueryParameters.class.getName());
    private StringBuilder queryParameters;
    private Integer recordPK;
    private static final PersistanceStrategy pStrat = AccessRecordPersistanceStrategy.getInstance();
    private static boolean base64Encoded = true; // default to true
    private static final String NEW_SEPARATOR = "||||";

    static {
        if (System.getProperty("base64Encoded")!=null)
            base64Encoded = Boolean.parseBoolean(System.getProperty("base64Encoded"));
        logger.warn("base64Encoded: " + base64Encoded);
    }
    
    private QueryParameters() {
    }

    public QueryParameters(String qp, int rPK) {
       
        recordPK = new Integer(rPK);
        if (qp != null && qp.indexOf("#P#") < 0) {
            queryParameters = new StringBuilder(decode(qp));
        } else {
            queryParameters = new StringBuilder(qp);
        }
    }

    private String decode(String s) {
        if (s != null) {
            try {
                byte[] decodeBA;
                if ( base64Encoded) {
                    decodeBA = com.bos.art.logParser.tools.Base64.decodeFast(s);
                    return new String(decodeBA);
                } else {
                    return s;
                }

            } catch (Exception e) {
                return s;
            }
        }
        return null;
    }
    private static String AMP_SEP = "&";
    private static String AMP_ESCAPED = "&amp;";
    private static String PARAM_MARKER = "#P#";

//    public static void main(String[] args) {
//        //String str = "#P#&amp;.time=1367587660446&isZipCode=verify&ZipCode=&header.host=localhost:8080&header.user-agent=Mozilla/5.0 (Windows NT 6.1; WOW64; rv:21.0) Gecko/20100101 Firefox/21.0&header.accept=text/javascript, text/html, application/xml, text/xml, */*&header.accept-language=en-US,en;q=0.5&header.accept-encoding=gzip, deflate&header.x-requested-with=XMLHttpRequest&header.x-prototype-version=1.6.0.3&header.referer=http://localhost:8080/technology/audio-accessories/tapes-accessories/product-ARS29961?R=21234451&amp;ssp=true&amp;useSessionResults=false&amp;searchTerm=&header.cookie.ClrSSID=1367528097606-9830&header.cookie.ClrOSSID=1367528097606-9830&header.cookie.ClrSCD=1367528097606&header.cookie.i=&header.cookie.e=&header.cookie.ZipCode=60555&header.cookie.customerStorePostalCode=60555&header.cookie.cartId=628423300&header.cookie.JSESSIONID=633DB18EB8454B17305F858143C41D9E&header.cookie.ATG_SESSION_ID=633DB18EB8454B17305F858143C41D9E&header.cookie.cmTPSet=Y&header.cookie.mt.isBol=true&header.cookie.rid=&header.cookie.to=&header.cookie.c=&header.cookie.pv=&header.cookie.lc=&header.cookie.s=&header.cookie.f=&header.cookie.ClrCSTO=T&header.connection=keep-alive";
//        String str = "#P#&amp;header.x-prototype-version=1.6.0.3&header.referer=http://localhost:8080/technology/audio-accessories/tapes-accessories/product-ARS29961?R=21234451&amp;ssp=true&amp;useSessionResults=false&amp;searchTerm=&header.cookie.ClrSSID=1367528097606-9830&header.cookie.ClrOSSID=1367528097606-9830&header.cookie.ClrSCD=1367528097606&header.cookie.i=&header.cookie.e=&header.cookie.ZipCode=60555&header.cookie.customerStorePostalCode=60555&header.cookie.cartId=628423300&header.cookie.JSESSIONID=633DB18EB8454B17305F858143C41D9E&header.cookie.ATG_SESSION_ID=633DB18EB8454B17305F858143C41D9E&header.cookie.cmTPSet=Y&header.cookie.mt.isBol=true&header.cookie.rid=&header.cookie.to=&header.cookie.c=&header.cookie.pv=&header.cookie.lc=&header.cookie.s=&header.cookie.f=&header.cookie.ClrCSTO=T&header.connection=keep-alive";
//        QueryParameters qp = new QueryParameters(str, 2);
//        qp.processQueryParameters();
//    }

    public static void main(String []args) {
        long start = System.currentTimeMillis();
        for(int i =0;i<100;i++) {
            String str = " #P#&/atg/commerce/order/purchase/CartModifierFormHandler.productIds=prod4220022&productIds=prod4220022&isFromCartWidget=true&_D:/atg/commerce/order/purchase/CartModifierFormHandler.addMultipleItemsToOrder= &defParentCatName10000349=Paper Mate Write Bros. Stick Medium Point Ballpoint Pens, 12 Blue Ink Pens&_dyncharset=ISO-8859-1&10000349csId=&_D:/atg/commerce/order/purchase/CartModifierFormHandler.addMultipleItemsToOrderErrorURL= &catalogRefIds=23411853&ratingAttribs=5.0-_-2&_D:/atg/commerce/order/purchase/CartModifierFormHandler.addMultipleItemsToOrderSuccessURL= &/atg/commerce/order/purchase/CartModifierFormHandler.continueShoppingURL=/catalog/search.jsp?freeText=blue+pen&searchText=Search+by+Keyword+or+Item+%23&/atg/commerce/order/purchase/CartModifierFormHandler.addMultipleItemsToOrderSuccessURL=/common/cartWidget.jsp&defParentCatId10000349=cat520002&defParentCatId=cat520002&isBeforeZipCodeExist=true&_D:/atg/commerce/order/purchase/CartModifierFormHandler.catalogRefIds= &23411853=1&/atg/commerce/order/purchase/CartModifierFormHandler.addMultipleItemsToOrder=submit&_DARGS=/catalog/common/addToCartButton.jsp&_D:/atg/commerce/order/purchase/CartModifierFormHandler.continueShoppingURL= &skuName10000349=Paper Mate Write Bros. Stick Medium Point Ballpoint Pens, 12 Blue Ink Pens&ratingAttribs10000349=5.0-_-2&/atg/commerce/order/purchase/CartModifierFormHandler.catalogRefIds=23411853&/atg/commerce/order/purchase/CartModifierFormHandler.addMultipleItemsToOrderErrorURL=/common/cartWidget.jsp&_D:/atg/commerce/order/purchase/CartModifierFormHandler.productIds= &header.connection=keep-alive&header.content-type=application/x-www-form-urlencoded; charset=UTF-8&header.Accept-Language=en-us&header.accept=text/javascript, text/html, application/xml, text/xml, */*&header.user-agent=Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1; WOW64; Trident/4.0; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; .NET4.0C; .NET4.0E; InfoPath.2)&header.pragma=no-cache&header.x-prototype-version=1.6.0.3&header.x-requested-with=XMLHttpRequest&header.Accept-Encoding=gzip, deflate&header.cookie.ZipCode=60510&header.cookie.customerStorePostalCode=60510&header.cookie.cartId= &header.cookie.mt.isBol=true&header.cookie.ZipCode5=60510&header.cookie.abc=b&header.content-length=1750&header.host=omx-01-stress.officemax.com  ";
            QueryParameters qp = new QueryParameters(str, 2);
            System.out.println(qp.processQueryParameters());
        }
        long end = System.currentTimeMillis();
        System.out.println("elapsed: " + (end - start));

    }
    /**
     * process query parameters, tokenize
     */
    public String processQueryParameters() {
        StringBuilder stringSet = new StringBuilder();

        if (queryParameters != null) {
            if (queryParameters.indexOf(PARAM_MARKER) > -1) {
                int sep = queryParameters.indexOf(PARAM_MARKER);
                queryParameters = new StringBuilder(queryParameters.substring(0, sep)).append("&").append(queryParameters.substring(sep + 3));
            }
            final int seplength = AMP_SEP.length();
            int sep = 0, asep = 0;
            int start = 0;
            
            while (queryParameters != null) {
                if ((sep = queryParameters.indexOf(AMP_SEP, start)) > -1) {
                    asep = queryParameters.indexOf(AMP_ESCAPED, start);
                    if (asep == sep) {
                        // this was a encoded amp and not just a &
                        start = asep + AMP_ESCAPED.length();
                        continue; // skip this & because it's an encoded amp;
                    }
                    String currentParameter = queryParameters.substring(0, sep);
                    queryParameters = new StringBuilder(queryParameters.substring(sep + seplength));
                    if (StringUtils.isNotEmpty(currentParameter)) {
                        stringSet.append(currentParameter).append(NEW_SEPARATOR);
                    }
                    start = 0;
                } else {
                    String currentParameter = queryParameters.toString();
                    queryParameters = null;
                    stringSet.append(currentParameter).append(NEW_SEPARATOR);
                }
            }
        }

        return stringSet.toString();

    }

    public void writeQueryParameter(String str) {
        // this does the lookup and insert
        Integer queryParameterID = null;
        queryParameterID = getQueryParameterId( str );
        QueryParameterWriteQueue.getInstance().addLast(new DBQueryParamRecord(queryParameterID, recordPK));
    }


    /**
     * this does the lookup and insert
     * @param s
     * @return 
     */
    private Integer getQueryParameterId(String s) {
        int result = ForeignKeyStore.getInstance().getForeignKey(null, s, ForeignKeyStore.FK_QUERY_PARAMETER_ID, pStrat);
        return new Integer(result);
    }

    public static class DBQueryParamRecord {

        public DBQueryParamRecord(Integer quid, Integer rpk) {
            QueryParameterID = quid;
            RecordPK = rpk;
        }
        private Integer RecordPK;
        private Integer QueryParameterID;

        public Integer getQueryParameterID() {
            return QueryParameterID;
        }

        public Integer getRecordPK() {
            return RecordPK;
        }
    }
}
