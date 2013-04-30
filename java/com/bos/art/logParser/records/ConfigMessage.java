package com.bos.art.logParser.records;

public class ConfigMessage extends UserRequestEventDesc {
        // these are specific for config messages
    private String fileName;
    private int maxSize;
    private int maxAge;

    public ConfigMessage()
    {
    }

    public void setFileName(String string)
    {
        fileName = string;
    }

    public String getFileName()
    {
        return fileName;
    }

    public void setMaxSize(int size)
    {
        maxSize = size;
    }
    public int getMaxSize()
    {
        return maxSize;
    }
    public void setMaxAge(int age)
    {
        maxAge = age;
    }
    public int getMaxAge()
    {
        return maxAge;
    }

    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append( super.toString());
        sb.append("\nConfigMessage: " ).append( fileName ).
        append(" ").append(maxSize).append(" ").append(maxAge);
        return sb.toString();
    }
}
