package com.bos.helper;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by i0360b6 on 2/19/14.
 */
abstract public class SingletonInstanceHelper<X> {
    protected X instance;
    private AtomicBoolean instanceInitialized = new AtomicBoolean(false);
    private AtomicBoolean instanceComplete = new AtomicBoolean(false);
    private Object objectLock = new Object();
    private Class createClass = null;

    /**
     * constructor the singleton creator
     *
     * @param create
     */
    public SingletonInstanceHelper(Class create) {
        this.createClass = create;
    }

    /**
     * override the createInstance method
     *
     * @return
     */
    abstract public Object createInstance();

    /**
     * ensure that only 1 instance gets created.
     *
     * @return
     */
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

