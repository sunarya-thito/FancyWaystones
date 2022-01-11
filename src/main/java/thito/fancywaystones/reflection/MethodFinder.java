package thito.fancywaystones.reflection;

import java.lang.reflect.Method;

public class MethodFinder {

    public static MethodFinder find(MethodFinder... methods) {
        for (MethodFinder finder : methods) {
            if (finder.available()) {
                return finder;
            }
        }
        throw new UnsupportedOperationException("unsupported version of minecraft");
    }

    private Object className;
    private String methodName;
    private Object[] parameters;
    private Method method;
    private boolean simpleParameterMode;

    public MethodFinder(Object className, String methodName, Object... parameters) {
        this.className = className;
        this.methodName = methodName;
        this.parameters = parameters;
    }

    public MethodFinder simpleParameter() {
        simpleParameterMode = true;
        return this;
    }

    public boolean available() {
        Class<?> declaringClass = ClassFinder.get(className);
        className = declaringClass;
        if (declaringClass != null) {
            if (simpleParameterMode) {
                for (Method method : declaringClass.getDeclaredMethods()) {
                    Class<?>[] param = method.getParameterTypes();
                    if (param.length == parameters.length && method.getName().equals(methodName)) {
                        for (int i = 0; i < param.length; i++) {
                            Object p = parameters[i];
                            Class<?> par = param[i];
                            if (p instanceof Class) {
                                if (!par.equals(p)) return false;
                            } else {
                                if (!par.getSimpleName().equals(p)) return false;
                            }
                        }
                        this.method = method;
                        method.setAccessible(true);
                        break;
                    }
                }
            } else {
                Class<?>[] params = new Class[parameters.length];
                for (int i = 0; i < parameters.length; i++) {
                    Class<?> find = ClassFinder.get(parameters[i]);
                    parameters[i] = find;
                    if (find == null) return false;
                    params[i] = find;
                }
                try {
                    method = declaringClass.getDeclaredMethod(methodName, params);
                    method.setAccessible(true);
                    return true;
                } catch (Throwable ignored) {
                }
            }
        }
        return false;
    }

    public Object invoke(Object instance, Object... params) {
        if (method != null) {
            try {
                return method.invoke(instance, params);
            } catch (Throwable ignored) {
            }
        }
        return null;
    }
}
