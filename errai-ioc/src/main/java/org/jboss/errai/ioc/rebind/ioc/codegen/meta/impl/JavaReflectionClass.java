package org.jboss.errai.ioc.rebind.ioc.codegen.meta.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.util.TypeLiteral;

import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaConstructor;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaField;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaMethod;

public class JavaReflectionClass extends AbstractMetaClass<Class> {
    private Annotation[] annotationsCache;
    private TypeLiteral typeLiteral;
    
    public JavaReflectionClass(Class clazz) {
        super(clazz);
        this.annotationsCache = clazz.getAnnotations();
    }

    public JavaReflectionClass(TypeLiteral typeLiteral) {
        super(typeLiteral.getRawType());
        this.typeLiteral = typeLiteral;
    }
    
    public String getName() {
        return getEnclosedMetaObject().getSimpleName();
    }

    public String getFullyQualifedName() {
        return getEnclosedMetaObject().getName();
    }

    private static MetaMethod[] fromMethodArray(Method[] methods) {
        List<MetaMethod> methodList = new ArrayList<MetaMethod>();

        for (Method m : methods) {
            methodList.add(new JavaReflectionMethod(m));
        }

        return methodList.toArray(new MetaMethod[methodList.size()]);
    }

    public MetaMethod[] getMethods() {
        return fromMethodArray(getEnclosedMetaObject().getMethods());
    }

    public MetaMethod[] getDeclaredMethods() {
        return fromMethodArray(getEnclosedMetaObject().getDeclaredMethods());
    }

    private static MetaField[] fromFieldArray(Field[] methods) {
        List<MetaField> methodList = new ArrayList<MetaField>();

        for (Field f : methods) {
            methodList.add(new JavaReflectionField(f));
        }

        return methodList.toArray(new MetaField[methodList.size()]);
    }

    public MetaField[] getFields() {
        return fromFieldArray(getEnclosedMetaObject().getFields());
    }

    public MetaField[] getDeclaredFields() {
        return fromFieldArray(getEnclosedMetaObject().getDeclaredFields());
    }

    public MetaField getField(String name) {
        try {
            return new JavaReflectionField(getEnclosedMetaObject().getField(name));
        } catch (Exception e) {
            throw new RuntimeException("Could not get field: " + name, e);
        }
    }

    public MetaField getDeclaredField(String name) {
        try {
            return new JavaReflectionField(getEnclosedMetaObject().getDeclaredField(name));
        } catch (Exception e) {
            throw new RuntimeException("Could not get field: " + name, e);
        }
    }

    public MetaConstructor[] getConstructors() {
        List<MetaConstructor> constructorList = new ArrayList<MetaConstructor>();

        for (Constructor c : getEnclosedMetaObject().getConstructors()) {
            constructorList.add(new JavaReflectionConstructor(c));
        }

        return constructorList.toArray(new MetaConstructor[constructorList.size()]);
    }

    public MetaConstructor[] getDeclaredConstructors() {
        List<MetaConstructor> constructorList = new ArrayList<MetaConstructor>();

        for (Constructor c : getEnclosedMetaObject().getDeclaredConstructors()) {
            constructorList.add(new JavaReflectionConstructor(c));
        }

        return constructorList.toArray(new MetaConstructor[constructorList.size()]);
    }

    public MetaConstructor getConstructor(Class... parameters) {
        try {
            return new JavaReflectionConstructor(getEnclosedMetaObject().getConstructor(parameters));
        } catch (Exception e) {
            throw new RuntimeException("Could not get constructor", e);
        }
    }

    public MetaConstructor getDeclaredConstructor(Class... parameters) {
        try {
            return new JavaReflectionConstructor(getEnclosedMetaObject().getDeclaredConstructor(parameters));
        } catch (Exception e) {
            throw new RuntimeException("Could not get constructor", e);
        }
    }

    public Annotation[] getAnnotations() {
        if (annotationsCache == null) {
            annotationsCache = getEnclosedMetaObject().getAnnotations();
        }
        return annotationsCache;
    }

    public MetaClass[] getParameterizedTypes() {
        List<MetaClass> parameterizedTypes = new ArrayList<MetaClass>();
        
        if(typeLiteral!=null && typeLiteral.getType() instanceof ParameterizedType) {
            ParameterizedType parameterizedType = ((ParameterizedType) typeLiteral.getType());
            for (Type type : parameterizedType.getActualTypeArguments()) {
                parameterizedTypes.add(new JavaReflectionClass((Class)type));
            }
        }
        return parameterizedTypes.toArray(new MetaClass[parameterizedTypes.size()]);
    }
}
