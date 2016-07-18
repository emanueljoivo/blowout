package org.fogbowcloud.blowout.scheduler.core.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.log4j.Logger;

public abstract class Job implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6111900503095749695L;

	public static enum TaskState{
		READY,RUNNING,COMPLETED,FAILED
	}
	
	public static final Logger LOGGER = Logger.getLogger(Job.class);
	
	protected List<Task> tasksReady = new ArrayList<Task>();
	protected List<Task> tasksRunning = new ArrayList<Task>();
	protected List<Task> tasksCompleted = new ArrayList<Task>();
	protected List<Task> tasksFailed = new ArrayList<Task>();
	
	protected ReentrantReadWriteLock taskReadyLock = new ReentrantReadWriteLock();
	protected ReentrantReadWriteLock taskCompletedLock = new ReentrantReadWriteLock();
	
	public void addTask(Task task) {
		LOGGER.debug("Adding task " + task.getId());
		taskReadyLock.writeLock().lock();
		try {
			tasksReady.add(task);
		} finally {
			taskReadyLock.writeLock().unlock();
		}
	}
	
	public void addFakeTask(Task task) {
		LOGGER.debug("Adding fake completed task " + task.getId());
		taskCompletedLock.writeLock().lock();
		try {
			tasksCompleted.add(task);
		} finally {
			taskCompletedLock.writeLock().unlock();
		}
	}

	public List<Task> getByState(TaskState state) {
		if (state.equals(TaskState.READY)) {
			return new ArrayList<Task>(tasksReady);
		} else if (state.equals(TaskState.RUNNING)) {
			return new ArrayList<Task>(tasksRunning);
		} else if (state.equals(TaskState.COMPLETED)) {
			return new ArrayList<Task>(tasksCompleted);
		} else if (state.equals(TaskState.FAILED)) {
			return new ArrayList<Task>(tasksFailed);
		} 
		return null;
	}
	
	public abstract void run(Task task);
	
	public abstract void finish(Task task);

	public abstract void fail(Task task);
	
	public void recoverTask(Task task) {
		LOGGER.debug("Recovering task " + task.getId());
		Task taskClone = task.clone();
		taskReadyLock.writeLock().lock();
		try {
			tasksReady.add(0, taskClone);
		} finally {
			taskReadyLock.writeLock().unlock();
		}
	}
	
	public String getId(){
		return null;
	}
}
