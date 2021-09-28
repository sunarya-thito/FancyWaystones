package org.mozilla.javascript;

import java.lang.reflect.*;

public class VMBridge_custom extends VMBridge {
    private final Object[] helper = new Object[1];
    @Override
    protected Object getThreadContextHelper() {
        return helper;
    }

    @Override
    protected Context getContext(Object o) {
        return (Context) ((Object[]) o)[0];
    }

    @Override
    protected void setContext(Object o, Context context) {
        ((Object[]) o)[0] = context;
    }

    @Override
    protected boolean tryToMakeAccessible(AccessibleObject accessibleObject) {
        try {
            accessibleObject.setAccessible(true);
            return true;
        } catch (Throwable t) {
        }
        return false;
    }

    protected Object getInterfaceProxyHelper(ContextFactory cf, Class<?>[] interfaces) {
        ClassLoader loader = interfaces[0].getClassLoader();
        Class cl = Proxy.getProxyClass(loader, interfaces);

        try {
            Constructor<?> c = cl.getConstructor(InvocationHandler.class);
            return c;
        } catch (NoSuchMethodException var7) {
            throw new IllegalStateException(var7);
        }
    }

    protected Object newInterfaceProxy(Object proxyHelper, final ContextFactory cf, final InterfaceAdapter adapter, final Object target, final Scriptable topScope) {
        Constructor<?> c = (Constructor)proxyHelper;
        InvocationHandler handler = new InvocationHandler() {
            public Object invoke(Object proxy, Method method, Object[] args) {
                if (method.getDeclaringClass() == Object.class) {
                    String methodName = method.getName();
                    if (methodName.equals("equals")) {
                        Object other = args[0];
                        return proxy == other;
                    }

                    if (methodName.equals("hashCode")) {
                        return target.hashCode();
                    }

                    if (methodName.equals("toString")) {
                        return "Proxy[" + target.toString() + "]";
                    }
                }

                return adapter.invoke(cf, target, topScope, proxy, method, args);
            }
        };

        try {
            Object proxy = c.newInstance(handler);
            return proxy;
        } catch (InvocationTargetException var10) {
            throw Context.throwAsScriptRuntimeEx(var10);
        } catch (IllegalAccessException var11) {
            throw new IllegalStateException(var11);
        } catch (InstantiationException var12) {
            throw new IllegalStateException(var12);
        }
    }
}
