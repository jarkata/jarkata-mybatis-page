package cn.jarkata.mybatis.page;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class ReflectionUtils {

    private static final Map<Class<?>, Field> fieldCache = new ConcurrentHashMap<>();

    public static Object getFieldValue(Object obj, String fieldName) {
        if (Objects.isNull(obj)) {
            return null;
        }
        Class<?> objClass = obj.getClass();
        Field field = getField(objClass, fieldName);
        if (Objects.isNull(field)) {
            return null;
        }
        try {
            field.setAccessible(true);
            return field.get(obj);
        } catch (Exception ex) {
            return null;
        }
    }


    public static Field getField(Class<?> clazz, String fieldName) {
        if (Objects.isNull(clazz)) {
            return null;
        }
        if (Object.class.getSimpleName().equals(clazz.getSimpleName())) {
            return null;
        }
        Field declaredField = fieldCache.get(clazz);
        if (Objects.nonNull(declaredField)) {
            return declaredField;
        }
        try {
            declaredField = clazz.getDeclaredField(fieldName);
        } catch (Exception ex) {
            declaredField = getField(clazz.getSuperclass(), fieldName);
        }
        if (Objects.nonNull(declaredField)) {
            fieldCache.put(clazz, declaredField);
        }
        return declaredField;
    }
}
