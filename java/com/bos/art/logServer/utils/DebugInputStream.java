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

    public int read(byte b[],int off, int len) throws IOException {
        int num = super.read(b,off,len);
        try {
            if ( num >0 ) 
                output.write(b,off,num);
        }
        catch(IOException io)
        {
            System.out.println("io error " +io);
        }
        return num;
    }
}
