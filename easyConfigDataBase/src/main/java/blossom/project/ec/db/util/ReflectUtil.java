//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package blossom.project.ec.db.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ReflectUtil {
    private ReflectUtil() {
    }

    public static Object invokeMethod(Object instance, Object[] args, Class<?> clazz, String methodName, Class<?>... parameterTypes) {
        try {
            Method method = clazz.getMethod(methodName, parameterTypes);
            return method.invoke(instance, args);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException var6) {
            var6.printStackTrace();
            return null;
        }
    }
}
