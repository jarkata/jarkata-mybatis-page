package cn.jarkata.mybatis.page;

import java.lang.reflect.Field;
import java.util.Objects;

public class ReflectionUtils {

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
        Field declaredField;
        try {
            declaredField = clazz.getDeclaredField(fieldName);
        } catch (Exception ex) {
            return getField(clazz.getSuperclass(), fieldName);
        }
        return declaredField;
    }
}
