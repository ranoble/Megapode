package com.gravspace.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class QueuedThreadExecuter extends Thread {
	LinkedBlockingQueue<Thread> queuedTasks;
	ExecutorService threadExecutor;
	public QueuedThreadExecuter(){
		threadExecutor = Executors.newFixedThreadPool(1);
		queuedTasks = new LinkedBlockingQueue<Thread>();
	}
	
	public void addTask(Thread task){
		queuedTasks.add(task);
	}
	
	public void run(){
		for (;;){
			try {
				Thread task = queuedTasks.take();
				threadExecutor.submit(task);
			} catch (InterruptedException e) {
				break;
			}
		}
	}
}
