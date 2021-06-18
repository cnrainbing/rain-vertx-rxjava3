package org.rain.vertx.app.base.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

/**
 * Reflection-related utility class
 *
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since 0.5.0
 */
public class ReflectionUtils {
    /**
     * Get a class' constructor. Return {@code null} if no such constructor found.
     *
     * <p>
     * This method can get private and protected constructors.
     * </p>
     *
     * @param clazz
     * @param parameterTypes
     * @return
     * @since 0.9.1.4
     */
    public static <T> Constructor<T> getConstructor(Class<T> clazz, Class<?>... parameterTypes) {
        try {
            return clazz != null ? clazz.getDeclaredConstructor(parameterTypes) : null;
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    /**
     * Get a class' method by name. Return {@code null} if no such method found.
     *
     * @param methodName
     * @param clazz
     * @param parameterTypes
     * @return
     * @since 0.9.1.5
     */
    private static Method getDeclaredMethod(String methodName, Class<?> clazz,
                                            Class<?>... parameterTypes) {
        try {
            return clazz != null ? clazz.getDeclaredMethod(methodName, parameterTypes) : null;
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    /**
     * Get a class' method by name. Return {@code null} if no such method found.
     *
     * <p>
     * This method can get private and protected methods.
     * </p>
     *
     * <p>
     * This method can get methods from super classes/interfaces.
     * </p>
     *
     * @param methodName
     * @param clazz
     * @param parameterTypes
     * @return
     * @since 0.9.1.4
     */
    public static Method getMethod(String methodName, Class<?> clazz, Class<?>... parameterTypes) {
        if (clazz == null) {
            return null;
        }
        Method m = getDeclaredMethod(methodName, clazz, parameterTypes);
        if (m == null) {
            List<Class<?>> interfacesAndSuperClasses = new LinkedList<>();
            interfacesAndSuperClasses.add(clazz.getSuperclass());
            for (Class<?> cl : clazz.getInterfaces()) {
                interfacesAndSuperClasses.add(cl);
            }
            for (Class<?> cl : interfacesAndSuperClasses) {
                m = getMethod(methodName, cl, parameterTypes);
                if (m != null) {
                    return m;
                }
            }
            return null;
        } else {
            return m;
        }
    }

    /**
     * Tells if a class is a sub-class of a super-class.
     * <p>
     * Note:
     * <ul>
     * <li>Sub-class against super-class: this method returns {@code true}.</li>
     * <li>Sub-interface against super-interface: this method returns
     * {@code true}.</li>
     * <li>Class against interface: this method returns {@code false}.</li>
     * </ul>
     *
     * @param clazz      class to check
     * @param superClazz the super-class to check
     * @return {@code true} if {@code superClazz} is indeed the super-class of
     * {@code clazz}
     */
    public static boolean hasSuperClass(Class<?> clazz, Class<?> superClazz) {
        if (clazz == null || superClazz == null || clazz == superClazz) {
            return false;
        }

        if (clazz.isInterface()) {
            return superClazz.isAssignableFrom(clazz);
        }

        Class<?> parent = clazz.getSuperclass();
        while (parent != null) {
            if (parent == superClazz) {
                return true;
            }
            parent = parent.getSuperclass();
        }
        return false;
    }

    /**
     * Tells if a class (or one of its super-classes) implements an interface;
     * or an interface is a sub-interface of a super-interface.
     * <p>
     * Note:
     * <ul>
     * <li>Sub-interface against super-interface: this method returns
     * {@code true}.</li>
     * <li>Class against interface: this method returns {@code true}.</li>
     * <li>Class against super-interface: this method returns {@code true}.</li>
     * </ul>
     *
     * @param clazz
     * @param iface
     * @return
     */
    public static boolean hasInterface(Class<?> clazz, Class<?> iface) {
        if (clazz == null || iface == null || clazz == iface) {
            return false;
        }
        return iface.isInterface() && iface.isAssignableFrom(clazz);
    }
}