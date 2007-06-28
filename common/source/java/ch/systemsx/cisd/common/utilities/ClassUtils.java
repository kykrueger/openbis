/*
 * Copyright 2007 ETH Zuerich, CISD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ch.systemsx.cisd.common.utilities;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ch.systemsx.cisd.common.annotation.Mandatory;
import ch.systemsx.cisd.common.exceptions.CheckedExceptionTunnel;

/**
 * Operations on classes using reflection.
 * 
 * @author Christian Ribeaud
 */
public final class ClassUtils
{
    private ClassUtils()
    {
        // Can not be instantiated.
    }

    /**
     * For given <code>Class</code> returns a list of fields that are annotated with {@link Mandatory}.
     */
    public final static List<Field> getMandatoryFields(Class clazz)
    {
        return getMandatoryFields(clazz, null);
    }
    
    /**
     * For given <code>Class</code> returns a list of fields that are annotated with {@link Mandatory}.
     * 
     * @param fields if <code>null</code>, then a new <code>List</code> is created.
     */
    private final static List<Field> getMandatoryFields(Class clazz, List<Field> fields)
    {
        List<Field> list = fields;
        if (list == null)
        {
            list = new ArrayList<Field>();
        }
        for (Field field : clazz.getDeclaredFields())
        {
            if (field.getAnnotation(Mandatory.class) != null)
            {
                list.add(field);
            }
        }
        Class superclass = clazz.getSuperclass();
        if (superclass != null)
        {
            return getMandatoryFields(superclass, list);
        }
        return list;
    }

    /**
     * Returns the currently called <code>Method</code>.
     * <p>
     * Returns <code>null</code> if none could be found.
     * </p>
     */
    public final static Method getCurrentMethod()
    {
        return getMethodOnStack(2);
    }

    /**
     * Returns the <code>Method</code> on the stack of <var>level</var>.
     * <p>
     * <code>level=0</code> is this method itself, <code>level=1</code> is the method that called it and so forth.
     * </p>
     * IMPORTANT NOTE: You should carefully use this method in a class having more than one method with the same name.
     * The internal idea used here (<code>new Throwable().getStackTrace()</code>) only returns a method name and
     * does not make any other consideration.
     * 
     * @see StackTraceElement#getMethodName()
     * @return <code>null</code> if none could be found.
     */
    public final static Method getMethodOnStack(int level)
    {
        StackTraceElement[] elements = new Throwable().getStackTrace();
        if (elements.length <= level)
        {
            return null;
        }
        StackTraceElement element = elements[level];
        String methodName = element.getMethodName();
        try
        {
            Method[] methods = Class.forName(element.getClassName()).getMethods();
            for (Method method : methods)
            {
                if (method.getName().equals(methodName))
                {
                    return method;
                }
            }
            // SecurityException, ClassNotFoundException
        } catch (Exception ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
        return null;
    }

    /**
     * The {@link InvocationHandler} for the {@link ClassUtils#createProxy(Object, Class, Class[])} method.
     * 
     * @author Bernd Rinn
     */
    private static final class ProxyingInvocationHandler implements InvocationHandler
    {
        private final Object proxiedObject;

        ProxyingInvocationHandler(Object proxiedObject)
        {
            this.proxiedObject = proxiedObject;
        }

        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
        {
            final Method proxyThroughMethod =
                    proxiedObject.getClass().getMethod(method.getName(), method.getParameterTypes());
            return proxyThroughMethod.invoke(proxiedObject, args);
        }
    }

    private static boolean fitsInterfaces(Class classToCheck, List<Class> interfaces)
    {
        for (Class ifs : interfaces)
        {
            if (ifs.isInterface() == false)
            {
                System.err.println("'" + ifs.getCanonicalName() + "' is not an interface.");
                return false;
            }
            for (Method method : ifs.getMethods())
            {
                try
                {
                    classToCheck.getMethod(method.getName(), method.getParameterTypes());
                } catch (NoSuchMethodException ex)
                {
                    System.err.println("'" + classToCheck.getCanonicalName() + "' does not have a method '" + method
                            + "'");
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Returns a {@link Proxy} for <var>objectToBeProxied</var>. This allows to retroactively fit a class into a couple
     * of interfaces.
     * 
     * @param objectToProxy The object to proxy through to.
     * @param mainInterfaceToProvideProxyFor The main interface to provide a proxy for and to cast to.
     * @param otherInterfacesToProvideProxyFor Other interfaces to provide a proxy for.
     */
    // Proxy guarantees that the type cast is OK.
    @SuppressWarnings("unchecked")
    public final static <T> T createProxy(Object objectToProxy, Class<T> mainInterfaceToProvideProxyFor,
            Class... otherInterfacesToProvideProxyFor)
    {
        final List<Class> interfaces = new ArrayList<Class>();
        interfaces.add(mainInterfaceToProvideProxyFor);
        interfaces.addAll(Arrays.asList(otherInterfacesToProvideProxyFor));
        assert fitsInterfaces(objectToProxy.getClass(), interfaces);
        return (T) Proxy.newProxyInstance(ClassUtils.class.getClassLoader(), interfaces.toArray(new Class[interfaces
                .size()]), new ProxyingInvocationHandler(objectToProxy));
    }

}
