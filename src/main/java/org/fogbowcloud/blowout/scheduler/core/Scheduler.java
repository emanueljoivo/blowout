package org.fogbowcloud.blowout.scheduler.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;
import org.fogbowcloud.blowout.infrastructure.ResourceNotifier;
import org.fogbowcloud.blowout.scheduler.core.model.Job;
import org.fogbowcloud.blowout.scheduler.core.model.Resource;
import org.fogbowcloud.blowout.scheduler.core.model.Specification;
import org.fogbowcloud.blowout.scheduler.core.model.Task;
import org.fogbowcloud.blowout.scheduler.core.model.TaskImpl;
import org.fogbowcloud.blowout.scheduler.core.model.Job.TaskState;
import org.fogbowcloud.blowout.scheduler.infrastructure.InfrastructureManager;

public class Scheduler implements Runnable, ResourceNotifier {

	private final String id;
	private ArrayList<Job> jobList = new ArrayList<Job>();
	private InfrastructureManager infraManager;
	private Map<String, Resource> runningTasks = new HashMap<String, Resource>();
	private ExecutorService taskExecutor =  Executors.newCachedThreadPool();

	private static final Logger LOGGER = Logger.getLogger(Scheduler.class);

	public Scheduler(InfrastructureManager infraManager, Job... jobs) {
		for(Job aJob : jobs) {
			jobList.add(aJob);
		}
		this.infraManager = infraManager;
		this.id = UUID.randomUUID().toString();
	}

	protected Scheduler(InfrastructureManager infraManager, ExecutorService taskExecutor, Job... jobs) {
		this(infraManager, jobs);
		this.taskExecutor = taskExecutor;
	}

	@Override
	public void run() {
		LOGGER.info("Running scheduler...");
		Map<Specification, Integer> specDemand = new HashMap<Specification, Integer>();		

		List<Task> readyTasks = new ArrayList<Task>();
		for (Job job : jobList){
			readyTasks.addAll(job.getByState(TaskState.READY));
		}
		LOGGER.debug("There are " + readyTasks.size() + " ready tasks.");
		LOGGER.debug("Scheduler running tasks is " + runningTasks.size());

		for (Task task : readyTasks) {
			Specification taskSpec = task.getSpecification();
			if (!specDemand.containsKey(taskSpec)) {
				specDemand.put(taskSpec, 0);
			}
			int currentDemand = specDemand.get(taskSpec); 
			specDemand.put(taskSpec, ++currentDemand);
		}

		LOGGER.debug("Current job demand is " + specDemand);
		for (Specification spec : specDemand.keySet()) {			
			infraManager.orderResource(spec, this, specDemand.get(spec));
		}
	}

	@Override
	public void resourceReady(final Resource resource) {
		LOGGER.debug("Receiving resource ready [ID:"+resource.getId()+"]");
		for (Job job : jobList) {
			for (final Task task : job.getByState(TaskState.READY)) {
				if(resource.match(task.getSpecification())){

					LOGGER.debug("Relating resource [ID:"+resource.getId()+"] with task [ID:"+task.getId()+"]");
					job.run(task);
					runningTasks.put(task.getId(), resource);
					task.startedRunning();
					task.putMetadata(TaskImpl.METADATA_RESOURCE_ID, resource.getId());
					taskExecutor.submit(new Runnable() {
						@Override
						public void run() {
							try {
								resource.executeTask(task);
							} catch (Throwable e) {
								LOGGER.error("Error while executing task.", e);
							}
						}
					});
					return;
				}
			}
		}

		infraManager.releaseResource(resource);
	}

	public void taskFailed(Task task) {
		LOGGER.debug("============================================================");
		LOGGER.debug("==  Task " + task.getId() + " failed and will be cloned.  ==");
		LOGGER.debug("============================================================");
		Job job = getJobOfFailedTask(task);
		if (job != null) {
			job.recoverTask(task);
		} else {
			LOGGER.error("Task was from a non-existing or removed Job");
		}
		infraManager.releaseResource(runningTasks.get(task.getId()));
		runningTasks.remove(task.getId());

	}

	private Job getJobOfFailedTask(Task task) {
		for(Job job : jobList) {
			if (job.getByState(TaskState.FAILED).contains(task)){
				LOGGER.debug("Failed task " + task.getId() + " is from job " + job.getId());
				return job; 
			}
		}
		return null;
	}

	public void taskCompleted(Task task) {
		LOGGER.info("Task " + task.getId() + " was completed.");
		infraManager.releaseResource(runningTasks.get(task.getId()));
		runningTasks.remove(task.getId());
	}

	public Resource getAssociateResource(
			Task task) {
		return runningTasks.get(task.getId());
	}

	protected Map<String, Resource> getRunningTasks(){
		return runningTasks;
	}

	protected String getId() {
		return id;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Scheduler other = (Scheduler) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	public void addJob(Job job) {
		this.jobList.add(job);
	}

	public ArrayList<Job> getJobs() {
		return this.jobList;
	}


	public Job getJobById(String jobId) {
		if (jobId == null) {
			return null;
		}
		for (Job job : this.jobList) {
			if (jobId.equals(job.getId())) {
				return job;
			}
		}
		return null;
	}

	public Job removeJob(String jobId) {
		Job toBeRemoved = getJobById(jobId);

		this.jobList.remove(toBeRemoved);
		for (Task task : toBeRemoved.getByState(TaskState.RUNNING)) {
			this.taskFailed(task);
		}
		return toBeRemoved;
	}
}
