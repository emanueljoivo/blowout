package org.fogbowcloud.blowout.scheduler;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import java.util.ArrayList;
import java.util.List;

import org.fogbowcloud.blowout.core.model.Specification;
import org.fogbowcloud.blowout.core.model.task.Task;
import org.fogbowcloud.blowout.core.model.task.TaskImpl;
import org.fogbowcloud.blowout.scheduler.DefaultScheduler;
import org.fogbowcloud.blowout.scheduler.Scheduler;
import org.fogbowcloud.blowout.core.monitor.TaskMonitor;
import org.fogbowcloud.blowout.infrastructure.model.FogbowResource;
import org.fogbowcloud.blowout.core.model.resource.ResourceState;
import org.fogbowcloud.blowout.core.model.resource.AbstractResource;
import org.fogbowcloud.blowout.pool.ResourceStateHelper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;


public class TestDefaultScheduler {

	private static final String FAKE_UUID = "1234";
	Scheduler sched;
	TaskMonitor taskMon;
	
	@Before
	public void setUp() {
		this.taskMon = Mockito.mock(TaskMonitor.class);
		this.sched = spy(new DefaultScheduler(taskMon));
	}
	
	@Test
	public void testActOnEmptyLists() {
		List<Task> tasks = new ArrayList<Task>();
		List<AbstractResource> resources = new ArrayList<AbstractResource>();
		sched.act(tasks, resources);
		
		
		List<Task> emptyTaskList = new ArrayList<Task>();
		assertEquals(sched.getRunningTasks(), emptyTaskList);
	}

	@Test
	public void testActOnEmptyResourceList() {
		List<Task> tasks = new ArrayList<Task>();
		Task task = new TaskImpl("fakeId", mock(Specification.class), FAKE_UUID);
		tasks.add(task);
		List<AbstractResource> resources = new ArrayList<AbstractResource>();
		sched.act(tasks, resources);
		
		
		List<Task> emptyTaskList = new ArrayList<Task>();
		assertEquals(sched.getRunningTasks(), emptyTaskList);
	}
	
	@Test
	public void testActOnEmptyTaskList() {
		List<Task> tasks = new ArrayList<Task>();
		List<AbstractResource> resources = new ArrayList<AbstractResource>();
		
		AbstractResource resource = new FogbowResource("resourceId", "fakeOrderId", mock(Specification.class));
		
		resources.add(resource);
		
		sched.act(tasks, resources);
		
		
		List<Task> emptyTaskList = new ArrayList<Task>();
		assertEquals(sched.getRunningTasks(), emptyTaskList);
	}
	
	@Test
	public void testActGoldePath() {
		Specification spec = mock(Specification.class);		
		List<Task> tasks = new ArrayList<Task>();
		Task task = new TaskImpl("fakeId", spec, FAKE_UUID);
		tasks.add(task);
		List<AbstractResource> resources = new ArrayList<AbstractResource>();
		AbstractResource resource = new FogbowResource("resourceId", "fakeOrderId", spec);
		ResourceStateHelper.changeResourceToState(resource, ResourceState.IDLE);
		resources.add(resource);
		
		sched.act(tasks, resources);
		
		
		List<Task> runningTaskList = new ArrayList<Task>();
		runningTaskList.add(task);
		assertEquals(sched.getRunningTasks(), runningTaskList);
	}
	
	@Test
	public void testActOnFailedResource() {
		List<Task> tasks = new ArrayList<Task>();
		Task task = new TaskImpl("fakeId", mock(Specification.class), FAKE_UUID);
		tasks.add(task);
		
		List<AbstractResource> resources = new ArrayList<AbstractResource>();
		sched.act(tasks, resources);
		
		List<Task> emptyTaskList = new ArrayList<Task>();
		assertEquals(sched.getRunningTasks(), emptyTaskList);
	}
	
	@Test
	public void testActOnRemovedTask() {
		List<Task> tasks = new ArrayList<Task>();
		Task task = new TaskImpl("fakeId", mock(Specification.class), FAKE_UUID);
		tasks.add(task);
		List<AbstractResource> resources = new ArrayList<AbstractResource>();
		sched.act(tasks, resources);
		
		
		List<Task> emptyTaskList = new ArrayList<Task>();
		assertEquals(sched.getRunningTasks(), emptyTaskList);
	}
	
	@Test
	public void testActOnRemovedResource() {
		List<Task> tasks = new ArrayList<Task>();
		Task task = new TaskImpl("fakeId", mock(Specification.class), FAKE_UUID);
		tasks.add(task);
		List<AbstractResource> resources = new ArrayList<AbstractResource>();
		sched.act(tasks, resources);
		
		
		List<Task> emptyTaskList = new ArrayList<Task>();
		assertEquals(sched.getRunningTasks(), emptyTaskList);
	}
	

	@Test
	public void testChooseTaskForRunning() {
		
	}
	
	@Test
	public void testStopTask(){
		
	}
	
	@Test
	public void testRunTask(){
		
	}
	
	@Test
	public void testSubmitToMonitor(){
		
	}
	
	@Test
	public void testCreateProcess(){
		
	}
	
}
