package com.github.jrialland.ajpclient.util;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public abstract class ApiCompat {

    private static final Object[] EMPTY_ARGS = new Object[0];

    private final String originPackage;

    private final String targetPackage;

    private final Map<Method, Method> methodsMapping = new HashMap<>(); // keys = methods in the target package, values = methods in the origin package

    private final Map<String, Class<?>> mappedInterfaces = new HashMap<>(); // keys = interface name in the origin package, values = interfaces in the target package

    private final Map<Class<?>, Function<Object, Object>> factories = new HashMap<>(); // keys = interfaces in the target package, values = factories for the interfaces in the origin package

    protected ApiCompat(String originPackage, String targetPackage) {
        this.originPackage = originPackage;
        this.targetPackage = targetPackage;
    }

    private static boolean belongsToPackage(String packageName, Class<?> clazz) {
        String pkg = clazz.getPackage().getName();
        return pkg.equals(packageName) || pkg.startsWith(packageName + ".");
    }

    public <T> void addConverter(Class<T> originType, Function<T, Object> mapping) {
        factories.put(originType, (Function<Object, Object>) mapping);
    }

    private Method getOriginMethod(Method method) {
        if (belongsToPackage(targetPackage, method.getDeclaringClass())) {
            return methodsMapping.get(method);
        } else {
            return method;
        }
    }

    private boolean isFromOriginPackage(Class<?> klass) {
        for (Class<?> iface : klass.getInterfaces()) {
            if (belongsToPackage(originPackage, iface)) {
                return true;
            }
        }
        return false;
    }

    private void addToMethodsMapping(Class<?> originInterface, Class<?> targetInterface) {
        for (Method originMethod : originInterface.getMethods()) {
            for (Method targetMethod : targetInterface.getMethods()) {
                if (originMethod.getName().equals(targetMethod.getName()) && originMethod.getParameterCount() == targetMethod.getParameterCount()) {
                    methodsMapping.put(targetMethod, originMethod);
                }
            }
        }
    }

    private Class<?> getTargetInterface(Class<?> originInterface) {
        if (!belongsToPackage(originPackage, originInterface)) {
            throw new RuntimeException("Interface " + originInterface.getName() + " is not in the " + originPackage + " package");
        }
        String originInterfaceName = originInterface.getCanonicalName();
        Class<?> targetInterface = mappedInterfaces.get(originInterfaceName);
        if (targetInterface == null) {
            String targetInterfaceName = originInterfaceName.replaceFirst("^" + originPackage, targetPackage);
            try {
                targetInterface = Class.forName(targetInterfaceName);
                mappedInterfaces.put(originInterfaceName, targetInterface);
                addToMethodsMapping(originInterface, targetInterface);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("Interface " + originInterfaceName + " not found in the " + targetPackage + " package");
            }
        }
        return targetInterface;
    }

    private List<Class<?>> getTargetInterfaces(Class<?> klass) {
        List<Class<?>> interfaces = new ArrayList<>();
        for (Class<?> interfaceClass : klass.getInterfaces()) {
            if (belongsToPackage(originPackage, interfaceClass)) {
                interfaces.add(getTargetInterface(interfaceClass));
            }
        }
        return interfaces;
    }

    @SuppressWarnings("unchecked")
    public <T> T makeProxy(Object obj) {
        Class<?> clazz = obj.getClass(); // must belong to origin package
        if (factories.containsKey(clazz)) {
            return (T) factories.get(clazz).apply(obj);
        }

        List<Class<?>> requiredInterfaces = getTargetInterfaces(clazz); // required interfaces must belong to target package
        if (requiredInterfaces.isEmpty()) {
            throw new RuntimeException("Class " + clazz.getCanonicalName() + " does not implement any interface from package " + originPackage);
        }

        requiredInterfaces.add(Proxied.class);

        InvocationHandler handler = new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

                args = args == null ? EMPTY_ARGS : args;

                if (method.getName().equals("__getWrappedInstance")) {
                    return obj;
                }

                if (method.getName().equals("equals") && args.length == 1) {
                    if (args[0] instanceof Proxied) {
                        return obj.equals(((Proxied) args[0]).__getWrappedInstance());
                    } else {
                        return obj.equals(args[0]);
                    }
                }

                if (method.getName().equals("hashCode") && args.length == 0) {
                    return System.identityHashCode(proxy);
                }

                if (method.getName().equals("toString") && args.length == 0) {
                    return proxy.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(proxy)) + ", with InvocationHandler " + this;
                }

                Method originMethod = getOriginMethod(method); // the target method that belongs to an interface in the origin package
                Object[] originArgs = new Object[args.length];

                for (int i = 0; i < args.length; i++) {
                    if (args[i] == null) {
                        originArgs[i] = null;
                    } else if (args[i] instanceof Proxied) {
                        originArgs[i] = ((Proxied) args[i]).__getWrappedInstance();
                    } else {
                        originArgs[i] = args[i];
                    }
                }

                Object result = originMethod.invoke(obj, originArgs);

                if (result == null) {
                    return null;
                }

                if(factories.containsKey(originMethod.getReturnType())) {
                    return factories.get(originMethod.getReturnType()).apply(result);
                }

                if (isFromOriginPackage(result.getClass())) {
                    return makeProxy(result); // if the result is an object that belongs to the origin package, make a proxy from it
                }
                return result;

            }
        };
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), requiredInterfaces.toArray(new Class<?>[]{}), handler);
    }

    interface Proxied {
        Object __getWrappedInstance();
    }
}
