package com.bos.helper;

/**
 * Created by i0360b6 on 2/19/14.
 */
public class MutableSingletonInstanceHelper<X> extends SingletonInstanceHelper {
    public MutableSingletonInstanceHelper(Class clasz) {
        super(clasz);
    }

    public void setInstance(X instance) {
        this.instance = instance;
    }
}
