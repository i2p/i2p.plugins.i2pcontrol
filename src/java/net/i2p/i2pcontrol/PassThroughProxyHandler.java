package net.i2p.i2pcontrol;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;


/**
 * An invocation handler that passes on any calls made to it directly to its delegate.
 * This is useful to handle identical classes loaded in different classloaders - the
 * VM treats them as different classes, but they have identical signatures.
 * 
 * Note this is using class.getMethod, which will only work on public methods.
 */
class PassThroughProxyHandler implements InvocationHandler {
    private final Object delegate;
    public PassThroughProxyHandler(Object delegate) {
        this.delegate = delegate;
    }
    public Object invoke(Object proxy, Method method, Object[] args)
            throws Throwable {
        Method delegateMethod = delegate.getClass().getMethod(method.getName(), method.getParameterTypes());
        return delegateMethod.invoke(delegate, args);
    }
}
