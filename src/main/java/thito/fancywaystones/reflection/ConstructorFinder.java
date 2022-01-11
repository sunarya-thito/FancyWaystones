package thito.fancywaystones.reflection;

import java.lang.reflect.Constructor;

public class ConstructorFinder {

    public static ConstructorFinder find(ConstructorFinder... methods) {
        for (ConstructorFinder finder : methods) {
            if (finder.available()) {
                return finder;
            }
        }
        throw new UnsupportedOperationException("unsupported version of minecraft");
    }

    private Object className;
    private Object[] parameters;
    private Constructor method;
    private boolean simpleParameterMode;

    public ConstructorFinder(Object className, Object... parameters) {
        this.className = className;
        this.parameters = parameters;
    }

    public ConstructorFinder simpleParameter() {
        simpleParameterMode = true;
        return this;
    }

    public boolean available() {
        Class<?> declaringClass = ClassFinder.get(className);
        className = declaringClass;
        if (declaringClass != null) {
            if (simpleParameterMode) {
                for (Constructor method : declaringClass.getDeclaredConstructors()) {
                    Class<?>[] param = method.getParameterTypes();
                    if (param.length == parameters.length) {
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
                    method = declaringClass.getDeclaredConstructor(params);
                    method.setAccessible(true);
                    return true;
                } catch (Throwable ignored) {
                }
            }
        }
        return false;
    }

    public Object newInstance(Object... params) {
        if (method != null) {
            try {
                return method.newInstance(params);
            } catch (Throwable ignored) {
            }
        }
        return null;
    }
}
