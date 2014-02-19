package com.bos.helper;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by i0360b6 on 2/19/14.
 */
public class SingletonInstanceHelper<X> {
    private AtomicBoolean instanceInitialized = new AtomicBoolean(false);
    private AtomicBoolean instanceComplete = new AtomicBoolean(false);
    private Object objectLock = new Object();
    private Class createClass = null;
    protected X instance;

    public SingletonInstanceHelper(Class create) {
        this.createClass = create;
    }

    public X getInstance() {
        if (instanceInitialized.compareAndSet(false, true)) {
            if (instance == null && !instanceComplete.get()) {
                try {
                    //instance = (X) createClass.newInstance();
//                    createClass.getDeclaredConstructor()
                    Constructor<X> createClass;// .getDeclaredConstructors(new Class[0]);
                    try {
                        Constructor<X> constructor = this.createClass.getDeclaredConstructor(new Class[0]);
                        constructor.setAccessible(true);
                        try {
                            instance = constructor.newInstance(new Object[0]);
                        } catch (InvocationTargetException e) {
                            e.printStackTrace();
                        }
                    } catch (NoSuchMethodException e) {
                        e.printStackTrace();
                    }
                    //constructor.setAccessible(true);
                    //X foo = constructor.newInstance(new Object[0]);
                    //System.out.println(foo);

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
