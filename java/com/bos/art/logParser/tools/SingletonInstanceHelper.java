package com.bos.art.logParser.tools;


import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by i0360b6 on 2/19/14.
 */
public class SingletonInstanceHelper<X> {
    protected X instance;
    private AtomicBoolean instanceInitialized = new AtomicBoolean(false);
    private AtomicBoolean instanceComplete = new AtomicBoolean(false);
    private Object objectLock = new Object();
    private Class createClass = null;

    public SingletonInstanceHelper(Class create) {
        this.createClass = create;
    }

    public Object createInstance() {
        return null;
    }

    public X getInstance() {
        if (instanceInitialized.compareAndSet(false, true)) {
            if (instance == null && !instanceComplete.get()) {
                instance = (X) createInstance();
                instanceComplete.set(true);
                synchronized (objectLock) {
                    objectLock.notify();
                }
            }
        }
        if (!instanceComplete.get()) {
            synchronized (objectLock) {
                try {
                    objectLock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        return instance;
    }
}
