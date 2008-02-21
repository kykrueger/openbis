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

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

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
     * For given <code>Class</code> returns a set of field names that are annotated with {@link Mandatory}.
     * <p>
     * Never returns <code>null</code> but could return an empty set.
     * </p>
     * 
     * @param clazz The class to return the mandatory fields for.
     * @param convertToLowerCase If <code>true</code>, all field names are converted to lower case in the set
     *            returned.
     */
    public final static Set<String> getMandatoryFields(final Class<?> clazz, final boolean convertToLowerCase)
    {
        final Set<String> set = new HashSet<String>();
        final List<Field> fields = ClassUtils.getMandatoryFieldsList(clazz);
        for (final Field field : fields)
        {
            if (convertToLowerCase)
            {
                set.add(field.getName().toLowerCase());
            } else
            {
                set.add(field.getName());
            }
        }
        return set;
    }

    /**
     * For given <code>Class</code> returns a list of fields that are annotated with {@link Mandatory}.
     */
    private final static List<Field> getMandatoryFieldsList(final Class<?> clazz)
    {
        return getMandatoryFields(clazz, null);
    }

    /**
     * For given <code>Class</code> returns a list of fields that are annotated with {@link Mandatory}.
     * 
     * @param fields if <code>null</code>, then a new <code>List</code> is created.
     */
    private final static List<Field> getMandatoryFields(final Class<?> clazz, final List<Field> fields)
    {
        List<Field> list = fields;
        if (list == null)
        {
            list = new ArrayList<Field>();
        }
        for (final Field field : clazz.getDeclaredFields())
        {
            if (field.getAnnotation(Mandatory.class) != null)
            {
                list.add(field);
            }
        }
        final Class<?> superclass = clazz.getSuperclass();
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
     * This method internally uses {@link Class#getMethods()} to retrieve the <code>Method</code> (meaning that
     * <code>private</code> methods will not be found).
     * </p>
     * <p>
     * IMPORTANT NOTE: You should carefully use this method in a class having more than one method with the same name.
     * The internal idea used here (<code>new Throwable().getStackTrace()</code>) only returns a method name and
     * does not make any other consideration.
     * </p>
     * 
     * @see StackTraceElement#getMethodName()
     * @return <code>null</code> if none could be found.
     */
    public final static Method getMethodOnStack(final int level)
    {
        final StackTraceElement[] elements = new Throwable().getStackTrace();
        if (elements.length <= level)
        {
            return null;
        }
        final StackTraceElement element = elements[level];
        final String methodName = element.getMethodName();
        try
        {
            final Method[] methods = Class.forName(element.getClassName()).getMethods();
            for (final Method method : methods)
            {
                if (method.getName().equals(methodName))
                {
                    return method;
                }
            }
            // SecurityException, ClassNotFoundException
        } catch (final Exception ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
        return null;
    }

    /**
     * Creates a new instance of a class specified by its fully-qualified name.
     * 
     * @param superClazz Super class <code>className</code> has to be implemented or extended.
     * @param className Fully-qualified class name.
     * @param argumentsOrNull Optional constructor arguments. If <code>(Object[]) null</code> then the empty
     *            constructor will be used. Note that <code>(Object) null</code> is not interpreted as
     *            <code>null</code> arguments but rather as <code>new Object[]{null}</code>.
     * @return an instance of type <code>interface</code>.
     */
    public static <T> T create(final Class<T> superClazz, final String className, final Object... argumentsOrNull)
    {
        assert superClazz != null : "Missing super class";
        assert className != null : "Missing class name";

        try
        {
            final Class<?> clazz = Class.forName(className);
            assert clazz.isInterface() == false : "Interface '" + clazz.getName()
                    + "' can not be instanciated as it is an interface.";
            assert superClazz.isAssignableFrom(clazz) : "Class '" + clazz.getName() + "' does not implements/extends '"
                    + superClazz.getName() + "'.";
            if (argumentsOrNull == null)
            {
                return createInstance(clazz);
            }
            final Class<?>[] classes = getClasses(argumentsOrNull);
            final Constructor<T> constructor = getConstructor(clazz, classes);
            if (constructor == null)
            {
                throw new IllegalArgumentException(String.format("No constructor could be found for classes '%s'.",
                        Arrays.asList(classes)));
            }
            return constructor.newInstance(argumentsOrNull);
        } catch (final ClassNotFoundException e)
        {
        } catch (final InstantiationException ex)
        {
        } catch (final IllegalAccessException ex)
        {
        } catch (final InvocationTargetException ex)
        {
        } catch (final NoSuchMethodException ex)
        {
        }
        throw new IllegalArgumentException(String.format("Cannot instantiate class '%s' with given arguments '%s'.",
                className, Arrays.asList(argumentsOrNull)));
    }

    private final static Class<?>[] getClasses(final Object... initargs)
    {
        final Class<?>[] classes = new Class<?>[initargs.length];
        int i = 0;
        for (final Object initarg : initargs)
        {
            classes[i++] = initarg.getClass();
        }
        return classes;
    }

    private final static <T> Constructor<T> getConstructor(final Class<?> clazz, final Class<?>[] classes)
            throws NoSuchMethodException
    {
        final Constructor<?>[] constructors = clazz.getConstructors();
        Constructor<?> returned = null;
        for (final Constructor<?> constructor : constructors)
        {
            final Class<?>[] parameterTypes = constructor.getParameterTypes();
            final int len = parameterTypes.length;
            if (len != classes.length)
            {
                continue;
            }
            boolean match = true;
            for (int i = 0; i < len; i++)
            {
                final Class<?> parameterType = parameterTypes[i];
                final Class<?> c = classes[i];
                match &= parameterType.equals(c) || parameterType.isAssignableFrom(c);
            }
            if (match)
            {
                returned = constructor;
            }
        }
        return toGenericType(returned);
    }

    @SuppressWarnings("unchecked")
    private static <T> Constructor<T> toGenericType(final Constructor<?> returned)
    {
        return (Constructor<T>) returned;
    }

    @SuppressWarnings("unchecked")
    private final static <T> T createInstance(final Class<?> clazz) throws InstantiationException,
            IllegalAccessException
    {
        return (T) clazz.newInstance();
    }

    /**
     * Sets declared field named <var>fieldName</var> of given <var>object</var> to given new value <var>newValue</var>.
     * <p>
     * This is useful when you want to set a <code>private</code> field on which you do not have access. Note that
     * this method should only be used in very special cases. You should consider it as a hack.
     * </p>
     */
    public final static void setFieldValue(final Object object, final String fieldName, final Object newValue)
            throws IllegalArgumentException
    {
        assert object != null : "Unspecified object.";
        final Class<?> clazz = object.getClass();
        try
        {
            final Field field = tryGetDeclaredField(clazz, fieldName);
            if (field != null)
            {
                field.setAccessible(true);
                field.set(object, newValue);
                return;
            }
        } catch (final SecurityException ex)
        {
        } catch (final IllegalArgumentException ex)
        {
        } catch (final IllegalAccessException ex)
        {
        }
        throw new IllegalArgumentException(String.format(
                "Cannot set field '%s' of class '%s' to given new value '%s'.", fieldName, clazz.getSimpleName(),
                newValue));
    }

    /**
     * Gets declared field named <var>fieldName</var> in given class or superclass of it.
     * 
     * @return <code>null</code> if given <var>fieldName</var> could not be found.
     */
    private final static Field tryGetDeclaredField(final Class<?> c, final String fieldName)
    {
        assert c != null : "Unspecified class.";
        assert StringUtils.isNotBlank(fieldName) : "Blank field name.";
        Field field = null;
        Class<?> clazz = c;
        do
        {
            try
            {
                field = clazz.getDeclaredField(fieldName);
            } catch (final SecurityException ex)
            {
            } catch (final NoSuchFieldException ex)
            {
            }
        } while (field == null && (clazz = clazz.getSuperclass()) != null);
        return field;
    }
}
