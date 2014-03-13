package com.gravspace.util;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.http.NameValuePair;

public class FormUtil {
	public static <T> T map(List<NameValuePair> formValues, T bean){
		return map(formValues, bean, new ArrayList<NameValuePair>());
	}
	
	public static <T> T map(List<NameValuePair> formValues, T bean, List<NameValuePair> unMapped){
		for (NameValuePair element: formValues){
			try {
				PropertyDescriptor descriptor = PropertyUtils.getPropertyDescriptor(bean, 
						element.getName());
				if (descriptor == null){
					unMapped.add(element);
					continue;
				} else {
					Class<?> type = descriptor.getPropertyType();
					if (List.class.isAssignableFrom(type)){
						addElementToList(bean, element, descriptor);
						continue;
					}
					if (Set.class.isAssignableFrom(type)){
						addElementToSet(bean, element, descriptor);
						continue;
					}
					BeanUtils.setProperty(bean,
							element.getName(), 
							element.getValue());
				}
			} catch (IllegalAccessException | InvocationTargetException
					| NoSuchMethodException e) {
				e.printStackTrace();
			}
		}
		return bean;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static <T> void addElementToList(T bean, NameValuePair element,
			PropertyDescriptor descriptor) throws IllegalAccessException,
			InvocationTargetException, NoSuchMethodException {
		List obj = (List)PropertyUtils.getProperty(bean, element.getName());
		if (obj == null){
			obj = new ArrayList();
		}
		Class<?> clazz = getListGenericType(descriptor);
		obj.add(ConvertUtils.convert(element.getValue(), clazz));
				
		PropertyUtils.setProperty(bean,
				element.getName(), 
				obj);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static <T> void addElementToSet(T bean, NameValuePair element,
			PropertyDescriptor descriptor) throws IllegalAccessException,
			InvocationTargetException, NoSuchMethodException {
		Set obj = (Set)PropertyUtils.getProperty(bean, element.getName());
		if (obj == null){
			obj = new HashSet();
		}
		Class<?> clazz = getListGenericType(descriptor);
		obj.add(ConvertUtils.convert(element.getValue(), clazz));
		
		PropertyUtils.setProperty(bean,
				element.getName(), 
				obj);
	}

	private static Class<?> getListGenericType(PropertyDescriptor descriptor) {
		Type[] types = descriptor.getWriteMethod().getGenericParameterTypes();
		ParameterizedType pType = (ParameterizedType) types[0];
		Class<?> clazz = (Class<?>) pType.getActualTypeArguments()[0];
		return clazz;
	}
}
