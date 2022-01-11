package thito.fancywaystones.reflection;

import java.lang.reflect.Field;

public class FieldFinder {

    public static FieldFinder find(FieldFinder...fieldFinders) {
        for (FieldFinder finder : fieldFinders) {
            if (finder.available()) {
                return finder;
            }
        }
        throw new UnsupportedOperationException("unsupported version of minecraft");
    }

    private Object className;
    private String fieldName;
    private Field field;

    public FieldFinder(Object className, String fieldName) {
        this.className = className;
        this.fieldName = fieldName;
    }

    public boolean available() {
        Class<?> declaring = ClassFinder.get(className);
        if (declaring != null) {
            try {
                field = declaring.getDeclaredField(fieldName);
                field.setAccessible(true);
                return true;
            } catch (Throwable ignored) {
            }
        }
        return false;
    }

    public Object get(Object instance) {
        if (field == null) return null;
        try {
            return field.get(instance);
        } catch (Throwable ignored) {
        }
        return null;
    }

    public void set(Object instance, Object value) {
        if (field == null) return;
        try {
            field.set(instance, value);
        } catch (Throwable ignored) {
        }
    }
}
