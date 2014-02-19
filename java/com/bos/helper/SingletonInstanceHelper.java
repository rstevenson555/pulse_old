package com.bos.helper;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by i0360b6 on 2/19/14.
 */
public class SingletonInstanceHelper<X> {
    private AtomicBoolean instanceInitialized = new AtomicBoolean(false);
    private AtomicBoolean instanceComplete = new AtomicBoolean(false);
    private Object objectLock = new Object();
    private Class createClass;
    protected X instance;

    public SingletonInstanceHelper(Class create) {
        this.createClass = create;
    }

    public X getInstance() {
        if (instanceInitialized.compareAndSet(false, true)) {
            if (instance == null && !instanceComplete.get()) {
                try {
                    instance = (X) createClass.newInstance();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
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
