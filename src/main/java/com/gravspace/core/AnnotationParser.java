package com.gravspace.core;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.reflections.Reflections;

import com.gravspace.abstractions.ICalculation;
import com.gravspace.abstractions.IWidget;
import com.gravspace.abstractions.IPage;
import com.gravspace.abstractions.IDataAccessor;
import com.gravspace.abstractions.IRenderer;
import com.gravspace.abstractions.ITask;
import com.gravspace.abstractions.PageRoute;
import com.gravspace.annotations.Calculation;
import com.gravspace.annotations.Page;
import com.gravspace.annotations.PersistanceAccessor;
import com.gravspace.annotations.Renderer;
import com.gravspace.annotations.Task;
import com.gravspace.annotations.Widget;

public class AnnotationParser {
	
	public static List<Class<? extends ITask>> getAnnotatedTasks(List<String> packages){
		List<Class<? extends ITask>> taskSet = new ArrayList<Class<? extends ITask>>();
		for (String pkg: packages){
			Reflections reflections = new Reflections(pkg);
			Set<Class<?>> tasks = 
		               reflections.getTypesAnnotatedWith(Task.class);
			for (Class<?> task: tasks){
				if (ITask.class.isAssignableFrom(task)){
					taskSet.add((Class<ITask>) task);
				}
			}
		}
		return taskSet;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		List<Class<? extends ITask>> res = getAnnotatedTasks(Arrays.asList(new String[]{"com.gravspace.page"}));
		for (Class<? extends ITask> item: res){
			System.out.println(item.getCanonicalName());
		}

	}

	public static List<Class<? extends ICalculation>> getAnnotatedCalculations(
			List<String> packages) {
		List<Class<? extends ICalculation>> calculationSet = new ArrayList<Class<? extends ICalculation>>();
		for (String pkg: packages){
			Reflections reflections = new Reflections(pkg);
			Set<Class<?>> tasks = 
		               reflections.getTypesAnnotatedWith(Calculation.class);
			for (Class<?> task: tasks){
				if (ICalculation.class.isAssignableFrom(task)){
					calculationSet.add((Class<ICalculation>) task);
				}
			}
		}
		return calculationSet;
	}

	public static List<Class<? extends IDataAccessor>> getAnnotatedDataAccessors(
			List<String> packages) {
		List<Class<? extends IDataAccessor>> calculationSet = new ArrayList<Class<? extends IDataAccessor>>();
		for (String pkg: packages){
			Reflections reflections = new Reflections(pkg);
			Set<Class<?>> tasks = 
		               reflections.getTypesAnnotatedWith(PersistanceAccessor.class);
			for (Class<?> task: tasks){
				if (IDataAccessor.class.isAssignableFrom(task)){
					calculationSet.add((Class<IDataAccessor>) task);
				}
			}
		}
		return calculationSet;
	}

	public static List<Class<? extends IRenderer>> getAnnotatedRenderers(
			List<String> packages) {
		List<Class<? extends IRenderer>> calculationSet = new ArrayList<Class<? extends IRenderer>>();
		for (String pkg: packages){
			Reflections reflections = new Reflections(pkg);
			Set<Class<?>> tasks = 
		               reflections.getTypesAnnotatedWith(Renderer.class);
			for (Class<?> task: tasks){
				if (IRenderer.class.isAssignableFrom(task)){
					calculationSet.add((Class<IRenderer>) task);
				}
			}
		}
		return calculationSet;
	}

	//we can use generics to clean this up with T and the like....
	public static List<Class<? extends IWidget>> getAnnotatedWidgets(
			List<String> packages) {
		List<Class<? extends IWidget>> calculationSet = new ArrayList<Class<? extends IWidget>>();
		for (String pkg: packages){
			Reflections reflections = new Reflections(pkg);
			Set<Class<?>> tasks = 
		               reflections.getTypesAnnotatedWith(Widget.class);
			for (Class<?> task: tasks){
				if (IWidget.class.isAssignableFrom(task)){
					calculationSet.add((Class<IWidget>) task);
				}
			}
		}
		return calculationSet;
	}

	public static List<PageRoute> getAnnotatedPages(List<String> packages) {
		List<PageRoute> pages = new ArrayList<PageRoute>();
		for (String pkg: packages){
			Reflections reflections = new Reflections(pkg);
			Set<Class<?>> pageClasses = 
		               reflections.getTypesAnnotatedWith(Page.class);
			for (Class<?> page: pageClasses){
				if (IPage.class.isAssignableFrom(page)){
					Page annotation = page.getAnnotation(Page.class);
					pages.add(new PageRoute(annotation.path(), (Class<? extends IPage>)page));
				}
			}
		}
		return pages;
	}

}
