package com.bos.applets;

public class GraphingFrame2 extends GraphingFrame {
    private static GraphingFrame2 instance = new GraphingFrame2("Live Graphs:2");

    public static GraphingFrame2 getInstance()
    {
        return instance;
    }

    public GraphingFrame2(String title) {
        super(title);
    }
}

