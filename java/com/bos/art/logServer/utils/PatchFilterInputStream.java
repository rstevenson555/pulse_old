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

        switch(b) {
            case (byte) '\u0000':
                return (byte)' ';
            case (byte) '\u001f':
                return (byte)' ';
            case (byte) '\u001e':
                return (byte)' ';
            case (byte) '\u0018':
                return (byte)' ';
            case (byte) '\u0003':
                return (byte)' ';
            case (byte) '\u001d':
                return (byte)' ';
        }


        return b;
    }

    @Override
    public int read(byte[] data, int offset, int length) throws IOException {

        int result = in.read(data, offset, length);
        for (int i = offset; i < offset + result; i++) {
            // do nothing with the printing characters
            // carriage return, linefeed, tab, and end of file
            switch(data[i]) {
                case (byte) '\u0000':
                    data[i] = (byte)' ';
                case (byte) '\u001f':
                    data[i] = (byte)' ';
                case (byte) '\u001e':
                    data[i] = (byte)' ';
                case (byte) '\u0018':
                    data[i] = (byte)' ';
                case (byte) '\u0003':
                    data[i] = (byte)' ';
                case (byte) '\u001d':
                    data[i] = (byte)' ';
            }
        }
        return result;

    }
}
