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

    public static void main(String[] args) {
        //String str = "#P#&amp;.time=1367587660446&isZipCode=verify&ZipCode=&header.host=localhost:8080&header.user-agent=Mozilla/5.0 (Windows NT 6.1; WOW64; rv:21.0) Gecko/20100101 Firefox/21.0&header.accept=text/javascript, text/html, application/xml, text/xml, */*&header.accept-language=en-US,en;q=0.5&header.accept-encoding=gzip, deflate&header.x-requested-with=XMLHttpRequest&header.x-prototype-version=1.6.0.3&header.referer=http://localhost:8080/technology/audio-accessories/tapes-accessories/product-ARS29961?R=21234451&amp;ssp=true&amp;useSessionResults=false&amp;searchTerm=&header.cookie.ClrSSID=1367528097606-9830&header.cookie.ClrOSSID=1367528097606-9830&header.cookie.ClrSCD=1367528097606&header.cookie.i=&header.cookie.e=&header.cookie.ZipCode=60555&header.cookie.customerStorePostalCode=60555&header.cookie.cartId=628423300&header.cookie.JSESSIONID=633DB18EB8454B17305F858143C41D9E&header.cookie.ATG_SESSION_ID=633DB18EB8454B17305F858143C41D9E&header.cookie.cmTPSet=Y&header.cookie.mt.isBol=true&header.cookie.rid=&header.cookie.to=&header.cookie.c=&header.cookie.pv=&header.cookie.lc=&header.cookie.s=&header.cookie.f=&header.cookie.ClrCSTO=T&header.connection=keep-alive";
        String str = "#P#&amp;header.x-prototype-version=1.6.0.3&header.referer=http://localhost:8080/technology/audio-accessories/tapes-accessories/product-ARS29961?R=21234451&amp;ssp=true&amp;useSessionResults=false&amp;searchTerm=&header.cookie.ClrSSID=1367528097606-9830&header.cookie.ClrOSSID=1367528097606-9830&header.cookie.ClrSCD=1367528097606&header.cookie.i=&header.cookie.e=&header.cookie.ZipCode=60555&header.cookie.customerStorePostalCode=60555&header.cookie.cartId=628423300&header.cookie.JSESSIONID=633DB18EB8454B17305F858143C41D9E&header.cookie.ATG_SESSION_ID=633DB18EB8454B17305F858143C41D9E&header.cookie.cmTPSet=Y&header.cookie.mt.isBol=true&header.cookie.rid=&header.cookie.to=&header.cookie.c=&header.cookie.pv=&header.cookie.lc=&header.cookie.s=&header.cookie.f=&header.cookie.ClrCSTO=T&header.connection=keep-alive";
        QueryParameters qp = new QueryParameters(str, 2);
        qp.processQueryParameters();
    }

    /**
     * process query parameters, tokenize
     */
    public void processQueryParameters() {
        HashSet<String> set = new HashSet();
        StringBuilder stringSet = new StringBuilder();
        if (queryParameters != null) {
            if (queryParameters.indexOf(PARAM_MARKER) > -1) {
                int sep = queryParameters.indexOf(PARAM_MARKER);
                queryParameters = new StringBuilder(queryParameters.substring(0, sep)).append("&").append(queryParameters.substring(sep + 3));
            }
            int seplength = AMP_SEP.length();
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
                    seplength = AMP_SEP.length();
                    String currentParameter = queryParameters.substring(0, sep);
                    queryParameters = new StringBuilder(queryParameters.substring(sep + seplength));
                    if (!"".equals(currentParameter)) {
                        stringSet.append(currentParameter).append("||||");
                        set.add(currentParameter);
                    }
                    start = 0;
                } else {
                    String currentParameter = queryParameters.toString();
                    queryParameters = null;
                    stringSet.append(currentParameter).append("||||");
                    set.add(currentParameter);
                }
            }
        }
        // this does the lookup and insert
        Integer queryParameterID = null;

        queryParameterID = getQueryParameterId( stringSet.toString() );
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
