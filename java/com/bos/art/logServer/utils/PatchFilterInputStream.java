/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bos.art.logServer.utils;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class PatchFilterInputStream extends FilterInputStream {

    public PatchFilterInputStream(InputStream in) {
        super(in);
    }

    @Override
    public int read() throws IOException {

        int b = in.read();

        if (b == (byte) '\u0000') {
            return (byte) ' ';
        } else   if (b == (byte) '\u001f')   {
            return (byte) ' ';
        }

        return b;
    }

    @Override
    public int read(byte[] data, int offset, int length) throws IOException {

        int result = in.read(data, offset, length);
        for (int i = offset; i < offset + result; i++) {
            // do nothing with the printing characters
            // carriage return, linefeed, tab, and end of file      
            if (data[i] == (byte) '\u0000') {
                data[i] = (byte) ' ';
            } else if (data[i] == (byte) '\u001f') {
                data[i] = (byte) ' ';
            }
        }
        return result;

    }
}
