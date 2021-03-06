package org.fogbowcloud.blowout.scheduler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.fogbowcloud.blowout.core.model.task.Task;
import org.fogbowcloud.blowout.core.model.task.TaskProcess;
import org.fogbowcloud.blowout.core.model.task.TaskProcessImpl;
import org.fogbowcloud.blowout.core.monitor.TaskMonitor;
import org.fogbowcloud.blowout.core.model.resource.ResourceState;
import org.fogbowcloud.blowout.core.model.resource.AbstractResource;

public class DefaultScheduler implements Scheduler {
	private static final Logger LOGGER = Logger.getLogger(DefaultScheduler.class);

	private Map<AbstractResource, Task> runningTasks;
	private TaskMonitor taskMonitor;

	public DefaultScheduler(TaskMonitor taskMonitor) {
		this.runningTasks = new ConcurrentHashMap<>();
		this.taskMonitor = taskMonitor;
	}

	@Override
	public void act(List<Task> tasksPool, List<AbstractResource> resourcesPool) {
		LOGGER.debug("Calling act from the Thread " + Thread.currentThread().getId() +
				" of entity: " + Thread.currentThread().getName());
		LOGGER.debug("Task Pool in Scheduler Act: " + toStringTasks(tasksPool));
		removeUselessTasks(tasksPool);
		for (AbstractResource resource : resourcesPool) {
			actOnResource(resource, tasksPool);
		}
		for (AbstractResource resourceInUse : this.runningTasks.keySet()) {
			if (!resourcesPool.contains(resourceInUse)) {
				stopTask(getTaskRunningInResource(resourceInUse));
			}
		}
	}

	private void removeUselessTasks(List<Task> tasksPool){
		for (Task runningTask : this.runningTasks.values()) {
			if (!tasksPool.contains(runningTask)) {
				stopTask(runningTask);
			}
		}
	}

	private String toStringTasks(List<Task> Tasks){
		String output = "";
		for(Task task : Tasks){
			output += " Id: " + task.getId();
		}
		return output;
	}


	private Task getTaskRunningInResource(AbstractResource resource) {
		return this.runningTasks.get(resource);
	}

	protected void actOnResource(AbstractResource resource, List<Task> tasks) {
		if (resource.getState().equals(ResourceState.IDLE)) {
			Task task = chooseTaskForRunning(resource, tasks);
			if (task != null) {
				LOGGER.info("Found task " + task.getId() + "for resource " + resource.getId());
				runTask(task, resource);
			} else {
				LOGGER.info("Not found task for resource " + resource.getId());
			}
		}
		
		if (resource.getState().equals(ResourceState.TO_REMOVE)) {
			this.runningTasks.remove(resource);
		}
	}

	protected Task chooseTaskForRunning(AbstractResource resource, List<Task> tasks) {
		LOGGER.debug("Choosing task for resource " + resource.getId());
		for (Task task : tasks) {
			boolean isSameSpecification = resource.getRequestedSpec().equals(task.getSpecification());
			if (!task.isFinished() && !this.runningTasks.containsValue(task) && isSameSpecification) {

				return task;
			}
		}
		return null;
	}

	@Override
	public void stopTasks(List<Task> tasks){
		LOGGER.info("Stopping list of Tasks");
		this.taskMonitor.stopTasks(tasks);
		this.removeTasksOfRunningTasks(tasks);
	}

	private void removeTasksOfRunningTasks(List<Task> tasksToRemove){
		for(Task taskToRemove : tasksToRemove){
			for (AbstractResource resource : this.runningTasks.keySet()) {
				Task task = runningTasks.get(resource);
				if (task.getId().equals(taskToRemove.getId())) {
					this.runningTasks.remove(resource);
				}
			}
		}
	}

	@Override
	public void stopTask(Task task) {
		// TODO: Find out how to stop the execution of the process
		for (AbstractResource resource : this.runningTasks.keySet()) {
			if (this.runningTasks.get(resource).equals(task)) {
				LOGGER.debug("Stopping task with id: " + task.getId());
				this.taskMonitor.stopTask(task);
				this.runningTasks.remove(resource);
			}
		}
	}

	@Override
	public void runTask(Task task, AbstractResource resource) {
		task.setRetries(task.getRetries() + 1);
		LOGGER.debug("Submitting task " + task.getId() + " to Task Monitor with " + task.getRetries() +
				" retries.");
		this.runningTasks.put(resource, task);

		submitToMonitor(task, resource);
	}

	public void submitToMonitor(Task task, AbstractResource resource) {
		this.taskMonitor.runTask(task, resource);
	}

	protected TaskProcess createProcess(Task task) { //FIXME: IS NOT USED
		return new TaskProcessImpl(task.getId(), task.getAllCommands(), task.getSpecification(), task.getUUID());
	}

	@Override
	public List<Task> getRunningTasks() {
		return new ArrayList<>(runningTasks.values());
	}
	
	protected void setRunningTasks(Map<AbstractResource, Task> runningTasks) {
		this.runningTasks = runningTasks;
	}
}
