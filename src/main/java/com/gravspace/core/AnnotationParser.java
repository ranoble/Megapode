package com.gravspace.core;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.reflections.Reflections;

import com.gravspace.abstractions.ITask;
import com.gravspace.annotations.Task;

public class AnnotationParser {
	
	public static List<Class<? extends ITask>> getAnnotatedTasks(List<String> packages){
		List<Class<? extends ITask>> taskSet = new ArrayList<Class<? extends ITask>>();
		for (String pkg: packages){
			Reflections reflections = new Reflections(packages);
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
		//		Reflections reflections = new Reflections("com.gravspace.page");
//		
//	    Set<Class<?>> proxies = 
//	               reflections.getTypesAnnotatedWith(Task.class);
//	    for (Class<?> task: proxies){
//			Task annotation = task.getAnnotation(Task.class);
//	    }

	}

}