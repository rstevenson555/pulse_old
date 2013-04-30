/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bos.art.logServer.utils;

/*
 * Base64EncodedInputStream.java
 *
 * Created on November 18, 2004, 11:47 AM
 */


import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringEscapeUtils;

/**
 *
 * @author  I0360D3
 */
public class XMLEncodedInputStream extends FilterInputStream{
    
    private byte[] startBytes;
    private byte[] endBytes;
    
    private static final int LOOKING_FOR_START =0;
    private static final int LOOKING_FOR_END = 1;
    private static final int READING_FROM_ENCODED_REGISTER=3;
    
  
    //
    //  StateMachine Definitions;
    //
    int readingAction = LOOKING_FOR_START;
    int actionState;
    private byte[] encodedRegister;
    private int encodedRegisterPos=0;
    /** Creates a new instance of Base64EncodedInputStream */
    public XMLEncodedInputStream(InputStream in, String startString, String endString) {
        super(in);
        startBytes = startString.getBytes();
        endBytes = endString.getBytes();
    }


    /**
     * Reads the next byte of data from this input stream. The value 
     * byte is returned as an <code>int</code> in the range 
     * <code>0</code> to <code>255</code>. If no byte is available 
     * because the end of the stream has been reached, the value 
     * <code>-1</code> is returned. This method blocks until input data 
     * is available, the end of the stream is detected, or an exception 
     * is thrown. 
     * <p>
     * This method
     * simply performs <code>in.read()</code> and returns the result.
     *
     * @return     the next byte of data, or <code>-1</code> if the end of the
     *             stream is reached.
     * @exception  IOException  if an I/O error occurs.
     * @see        java.io.FilterInputStream#in
     */
    public int read() throws IOException {
	    if(readingAction == LOOKING_FOR_START){
            int nextByte = in.read();
            if(nextByte== -1){
            	int nb = nextByte;
            }
            if(nextByte !=-1 && ((byte)nextByte == startBytes[actionState]) ){
                if(++actionState==startBytes.length){
                    //
                    //  We now need to create the base64 encoded buffer.....
                    readingAction = LOOKING_FOR_END;
                    actionState =0;
                    int bytesRead = buildEncodedBuffer();
                }
            }else{
                actionState=0;
            }
            
            return nextByte;
            
        }else if(readingAction == READING_FROM_ENCODED_REGISTER){
            int nextByte = encodedRegister[encodedRegisterPos++];
            if(encodedRegisterPos>= encodedRegister.length){
                readingAction=LOOKING_FOR_START;
                encodedRegisterPos=0;
            }
            return nextByte;
        }else{
            throw new IOException("Incorrect readingAction State: " + readingAction);
        }
        
       // return -1;
    }
    
    private int buildEncodedBuffer(){
        return read1CharAtATimeUntilEndRegionIsFound();
    }


    /**
     * Reads up to <code>len</code> bytes of data from this input stream 
     * into an array of bytes. This method blocks until some input is 
     * available. 
     * <p>
     * This method simply performs <code>in.read(b, off, len)</code> 
     * and returns the result.
     *
     * @param      b     the buffer into which the data is read.
     * @param      off   the start offset of the data.
     * @param      len   the maximum number of bytes read.
     * @return     the total number of bytes read into the buffer, or
     *             <code>-1</code> if there is no more data because the end of
     *             the stream has been reached.
     * @exception  IOException  if an I/O error occurs.
     * @see        java.io.FilterInputStream#in
     */
    public int read(byte b[], int off, int len) throws IOException {
        byte[] lba = new byte[len];
        int i =0;
        for(i=0;i<len;){
            int nextval = read();
            if(nextval == -1){
            	if(i==0){
            		return -1;
            	}
                break;
            }
            lba[i]=(byte)nextval;
            ++i;
        }
        System.arraycopy(lba,0,b,off,i);
	    return i;
    }

    /**
     * Skips over and discards <code>n</code> bytes of data from the 
     * input stream. The <code>skip</code> method may, for a variety of 
     * reasons, end up skipping over some smaller number of bytes, 
     * possibly <code>0</code>. The actual number of bytes skipped is 
     * returned. 
     * <p>
     * This method
     * simply performs <code>in.skip(n)</code>.
     *
     * @param      n   the number of bytes to be skipped.
     * @return     the actual number of bytes skipped.
     * @exception  IOException  if an I/O error occurs.
     */
    public long skip(long n) throws IOException {
	return in.skip(n);
    }

    /**
     * Returns the number of bytes that can be read from this input 
     * stream without blocking. 
     * <p>
     * This method
     * simply performs <code>in.available(n)</code> and
     * returns the result.
     *
     * @return     the number of bytes that can be read from the input stream
     *             without blocking.
     * @exception  IOException  if an I/O error occurs.
     * @see        java.io.FilterInputStream#in
     */
    public int available() throws IOException {
	return in.available();
    }

    /**
     * Closes this input stream and releases any system resources 
     * associated with the stream. 
     * This
     * method simply performs <code>in.close()</code>.
     *
     * @exception  IOException  if an I/O error occurs.
     * @see        java.io.FilterInputStream#in
     */
    public void close() throws IOException {
	in.close();
    }

    /**
     * Marks the current position in this input stream. A subsequent 
     * call to the <code>reset</code> method repositions this stream at 
     * the last marked position so that subsequent reads re-read the same bytes.
     * <p>
     * The <code>readlimit</code> argument tells this input stream to 
     * allow that many bytes to be read before the mark position gets 
     * invalidated. 
     * <p>
     * This method simply performs <code>in.mark(readlimit)</code>.
     *
     * @param   readlimit   the maximum limit of bytes that can be read before
     *                      the mark position becomes invalid.
     * @see     java.io.FilterInputStream#in
     * @see     java.io.FilterInputStream#reset()
     */
    public synchronized void mark(int readlimit) {
	in.mark(readlimit);
    }

    /**
     * Repositions this stream to the position at the time the 
     * <code>mark</code> method was last called on this input stream. 
     * <p>
     * This method
     * simply performs <code>in.reset()</code>.
     * <p>
     * Stream marks are intended to be used in
     * situations where you need to read ahead a little to see what's in
     * the stream. Often this is most easily done by invoking some
     * general parser. If the stream is of the type handled by the
     * parse, it just chugs along happily. If the stream is not of
     * that type, the parser should toss an exception when it fails.
     * If this happens within readlimit bytes, it allows the outer
     * code to reset the stream and try another parser.
     *
     * @exception  IOException  if the stream has not been marked or if the
     *               mark has been invalidated.
     * @see        java.io.FilterInputStream#in
     * @see        java.io.FilterInputStream#mark(int)
     */
    public synchronized void reset() throws IOException {
	in.reset();
    }

    /**
     * Tests if this input stream supports the <code>mark</code> 
     * and <code>reset</code> methods. 
     * This method
     * simply performs <code>in.markSupported()</code>.
     *
     * @return  <code>true</code> if this stream type supports the
     *          <code>mark</code> and <code>reset</code> method;
     *          <code>false</code> otherwise.
     * @see     java.io.FilterInputStream#in
     * @see     java.io.InputStream#mark(int)
     * @see     java.io.InputStream#reset()
     */
    public boolean markSupported() {
	return in.markSupported();
    }   
    
      int read1CharAtATimeUntilEndRegionIsFound() {
        //
        //  We have to look deeper to get the data we want to encode.
        //  copy the last endRegion chars to a new buffer and start looking there...
        //
        byte[] ba = new byte[1024*1024];
        //		System.arraycopy(filterbuf,filterbuf.length - endRegion.length, ba,0,endRegion.length);

        int bytesToCompare = -1;
        
        while (indexOf(ba, 0, bytesToCompare, endBytes, 0, endBytes.length, 0) < 0) {
            byte b = -1;
            try {
                b = (byte) in.read();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
            if (b != -1) {
            	if(bytesToCompare == -1){
            		++bytesToCompare;
            	}
                ba[bytesToCompare++] = b;
            } else {
                break;
            }
            if (bytesToCompare >= ba.length) {
                break;
            }
        }
        int encodeSize = bytesToCompare - endBytes.length;
        //  filterbuf.length - (startBase64Region + startRegion.length + endRegion.length);
        //  encodeSize += bytesToCompare - endRegion.length;
        byte[] bufToEncode = new byte[encodeSize];

        //System.arraycopy(filterbuf,startBase64Region + startRegion.length,bufToEncode,0,filterbuf.length - (startBase64Region+startRegion.length+endRegion.length));
        System.arraycopy(ba, 0, bufToEncode, 0, encodeSize);
        //byte[] encodedbuf = Base64.encodeBase64(bufToEncode);
        byte[] encodedbuf = StringEscapeUtils.escapeXml(String.valueOf(bufToEncode)).getBytes();

        int totallength = encodedbuf.length + endBytes.length + cdataStart.length + cdataEnd.length;
        encodedRegister = new byte[totallength];
        System.arraycopy(cdataStart,0,encodedRegister,0,cdataStart.length);
        System.arraycopy(encodedbuf,0,encodedRegister,cdataStart.length,encodedbuf.length);
        System.arraycopy(cdataEnd,0,encodedRegister,encodedbuf.length+cdataStart.length,cdataEnd.length);
        System.arraycopy(endBytes,0,encodedRegister,encodedbuf.length+cdataStart.length+cdataEnd.length,endBytes.length);
        
        readingAction=READING_FROM_ENCODED_REGISTER;
        actionState =0;
        return bytesToCompare;
    }
     private static final byte[] cdataStart = "".getBytes();

     private static final byte[] cdataEnd = "".getBytes();
     //private static final byte[] cdataStart = "<![CDATA[".getBytes();

     //private static final byte[] cdataEnd = "]]>".getBytes();
    
        static int indexOf(
        byte[] source,
        int sourceOffset,
        int sourceCount,
        byte[] target,
        int targetOffset,
        int targetCount,
        int fromIndex) {
        if (fromIndex >= sourceCount) {
            return (targetCount == 0 ? sourceCount : -1);
        }
        if (fromIndex < 0) {
            fromIndex = 0;
        }
        if (targetCount == 0) {
            return fromIndex;
        }

        byte first = target[targetOffset];
        int i = sourceOffset + fromIndex;
        int max = sourceOffset + (sourceCount - targetCount);

        startSearchForFirstChar : while (true) {
            /* Look for first character. */
            while (i <= max && source[i] != first) {
                i++;
            }
            if (i > max) {
                return -1;
            }

            /* Found first character, now look at the rest of v2 */
            int j = i + 1;
            int end = j + targetCount - 1;
            int k = targetOffset + 1;
            while (j < end) {
                if (source[j++] != target[k++]) {
                    i++;
                    /* Look for str's first char again. */
                    continue startSearchForFirstChar;
                }
            }
            return i - sourceOffset; /* Found whole string. */
        }
    }

}
