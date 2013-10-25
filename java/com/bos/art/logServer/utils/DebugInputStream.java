package com.bos.art.logServer.utils;

import java.io.*;

public class DebugInputStream extends FilterInputStream
{
    private OutputStream output;

    public DebugInputStream(InputStream in,String outputName)
    {
        super(in);
        try {
            output = new FileOutputStream( outputName );
        }
        catch(IOException io)
        {
            System.out.println("io error " +io);
        }
    }

    public int read(byte b[]) throws IOException {
        int num = super.read(b);
        try {
            if ( num > 0) 
                output.write( b,0,num );
        }
        catch(IOException io)
        {
            System.out.println("io error " +io);
        }
            
        return num;
    }

    @Override
    public int read() throws IOException {
        int c =  super.read();    //To change body of overridden methods use File | Settings | File Templates.
        output.write(c);
        return c;
    }

    public int read(byte b[],int off, int len) throws IOException {
        int num = super.read(b,off,len);
        try {
            if ( num >0 ) 
                output.write(b,off,len);
        }
        catch(IOException io)
        {
            System.out.println("io error " +io);
        }
        return num;
    }
}
