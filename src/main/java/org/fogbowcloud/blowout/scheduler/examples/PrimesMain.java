package org.fogbowcloud.blowout.scheduler.examples;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;
import org.fogbowcloud.blowout.scheduler.core.ExecutionMonitor;
import org.fogbowcloud.blowout.scheduler.core.ManagerTimer;
import org.fogbowcloud.blowout.scheduler.core.Scheduler;
import org.fogbowcloud.blowout.scheduler.core.model.Command;
import org.fogbowcloud.blowout.scheduler.core.model.Job;
import org.fogbowcloud.blowout.scheduler.core.model.Resource;
import org.fogbowcloud.blowout.scheduler.core.model.Specification;
import org.fogbowcloud.blowout.scheduler.core.model.Task;
import org.fogbowcloud.blowout.scheduler.core.model.TaskImpl;
import org.fogbowcloud.blowout.scheduler.core.util.AppPropertiesConstants;
import org.fogbowcloud.blowout.scheduler.infrastructure.InfrastructureManager;
import org.fogbowcloud.blowout.scheduler.infrastructure.InfrastructureProvider;
import org.fogbowcloud.blowout.scheduler.infrastructure.fogbow.FogbowRequirementsHelper;

public class PrimesMain {

	private final static String METADATA_REMOTE_OUTPUT_FOLDER = "/tmp/";
	private final static String SPEC_FILE_PATH = "/home/igorvcs/Dev/sebalScheduleEnv/initialSpec";
	private final static String SANDBOX = "/tmp/sandbox";
	private final static String SSH_SCP_PRECOMMAND = "-o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no";

	private static boolean blockWhileInitializing;
	private static boolean isElastic;
	private static ManagerTimer executionMonitorTimer = new ManagerTimer(Executors.newScheduledThreadPool(1));
	private static ManagerTimer schedulerTimer = new ManagerTimer(Executors.newScheduledThreadPool(1));

	private static Properties properties;

	private static final Logger LOGGER = Logger.getLogger(PrimesMain.class);

	public static void main(String[] args) throws Exception {

		LOGGER.debug("Starting ExampleMain");

		properties = new Properties();
		FileInputStream input = new FileInputStream(args[0]);
		properties.load(input);

		loadConfigFromProperties();

		String initialSpecsFilePath = properties.getProperty(AppPropertiesConstants.INFRA_INITIAL_SPECS_FILE_PATH);
		List<Specification> initialSpecs = Specification.getSpecificationsFromJSonFile(initialSpecsFilePath);

		InfrastructureProvider infraProvider = createInfraProvaiderInstance();
		// InfrastructureManager infraManager = new
		// InfrastructureManager(initialSpecs, isElastic, infraProvider,
		// properties);
		InfrastructureManager infraManager = new InfrastructureManager(null, isElastic, infraProvider, properties);
		infraManager.start(blockWhileInitializing, true);

		Job primeJob = new PrimeJob();
		primeJob.addTask(getPrimeTask(1000, 2000, null));

		Job primeJob2 = new PrimeJob();
		primeJob2.addTask(getPrimeTask(2000, 3000, null));
		// primeJob.addTask(getPrimeErrorTask(spec, 7000, 8000));

		Scheduler scheduler = new Scheduler(infraManager, primeJob, primeJob2);
		ExecutionMonitor execMonitor = new ExecutionMonitor(scheduler, primeJob, primeJob2);

		LOGGER.debug("Starting Scheduler and Execution Monitor");
		executionMonitorTimer.scheduleAtFixedRate(execMonitor, 0,
				Integer.parseInt(properties.getProperty("execution_monitor_period")));
		schedulerTimer.scheduleAtFixedRate(scheduler, 0, 30000);

	}

	private static void loadConfigFromProperties() {

		blockWhileInitializing = new Boolean(
				properties.getProperty(AppPropertiesConstants.INFRA_SPECS_BLOCK_CREATING)).booleanValue();
		isElastic = new Boolean(properties.getProperty(AppPropertiesConstants.INFRA_IS_STATIC)).booleanValue();

	}

	private static InfrastructureProvider createInfraProvaiderInstance() throws Exception {

		String providerClassName = properties.getProperty(AppPropertiesConstants.INFRA_PROVIDER_CLASS_NAME);

		Object clazz = Class.forName(providerClassName).getConstructor(Properties.class).newInstance(properties);
		if (!(clazz instanceof InfrastructureProvider)) {
			throw new Exception("Provider Class Name is not a InfrastructureProvider implementation");
		}

		return (InfrastructureProvider) clazz;
	}

	private static Task getPrimeTask(int init, int end, String location) throws IOException {
		List<Specification> taskSpecs = Specification.getSpecificationsFromJSonFile(SPEC_FILE_PATH);
		Specification spec = taskSpecs.get(0);
		LOGGER.debug("Task spec: " + spec.toString());
		if (location != null) {
			spec.addRequirement(FogbowRequirementsHelper.METADATA_FOGBOW_REQUIREMENTS,
					spec.getRequirementValue(FogbowRequirementsHelper.METADATA_FOGBOW_REQUIREMENTS) + " && "
							+ FogbowRequirementsHelper.METADATA_FOGBOW_REQUIREMENTS_1Glue2CloudComputeManagerID + "=="
							+ location);
		}
		TaskImpl task = new TaskImpl(UUID.randomUUID().toString(), spec);
		task.putMetadata(TaskImpl.METADATA_REMOTE_OUTPUT_FOLDER, METADATA_REMOTE_OUTPUT_FOLDER);
		task.putMetadata(TaskImpl.METADATA_REMOTE_OUTPUT_FOLDER, properties.getProperty("local.output"));
		task.putMetadata(TaskImpl.METADATA_SANDBOX, SANDBOX);
		task.putMetadata(TaskImpl.METADATA_REMOTE_COMMAND_EXIT_PATH, METADATA_REMOTE_OUTPUT_FOLDER + "/exit");
		task.putMetadata(TaskImpl.METADATA_TASK_TIMEOUT, "50000000");
		task.addCommand(cleanPreviousExecution(task.getMetadata(TaskImpl.METADATA_SANDBOX)));
		task.addCommand(mkdirRemoteFolder(task.getMetadata(TaskImpl.METADATA_SANDBOX)));
		task.addCommand(stageInCommand("isprime.py", task.getMetadata(TaskImpl.METADATA_SANDBOX) + "/isprime.py"));
		task.addCommand(remoteCommand("\"python /" + task.getMetadata(TaskImpl.METADATA_SANDBOX) + "/isprime.py " + init
				+ " " + end + " ; echo 0 > " + task.getMetadata(TaskImpl.METADATA_REMOTE_COMMAND_EXIT_PATH) + "\""));
		task.addCommand(stageOutCommand("/tmp/primeresult",
				properties.getProperty("local.output") + "/primeresult-" + task.getId()));
		LOGGER.debug("Task specs:" + spec.toString());
		return task;
	}

	private static Task getPrimeErrorTask(Specification spec, int init, int end) {
		TaskImpl task = new TaskImpl(UUID.randomUUID().toString(), spec);
		task.putMetadata(TaskImpl.METADATA_REMOTE_OUTPUT_FOLDER, METADATA_REMOTE_OUTPUT_FOLDER);
		task.putMetadata(TaskImpl.METADATA_REMOTE_OUTPUT_FOLDER, properties.getProperty("local.output"));
		task.putMetadata(TaskImpl.METADATA_SANDBOX, SANDBOX);
		task.putMetadata(TaskImpl.METADATA_REMOTE_COMMAND_EXIT_PATH, METADATA_REMOTE_OUTPUT_FOLDER + "/exit");
		task.addCommand(cleanPreviousExecution(task.getMetadata(TaskImpl.METADATA_SANDBOX)));
		task.addCommand(mkdirRemoteFolder(task.getMetadata(TaskImpl.METADATA_SANDBOX)));
		task.addCommand(stageInCommand("isprime.py", task.getMetadata(TaskImpl.METADATA_SANDBOX) + "/isprime.py"));
		task.addCommand(remoteCommand("\"python /" + task.getMetadata(TaskImpl.METADATA_SANDBOX) + "/isprime.py " + init
				+ " " + end + " ; echo 128 > " + task.getMetadata(TaskImpl.METADATA_REMOTE_COMMAND_EXIT_PATH) + "\""));
		task.addCommand(stageOutCommand("/tmp/primeresult",
				properties.getProperty("local.output") + "/primeresult-" + task.getId()));
		return task;
	}

	private static Command remoteCommand(String remoteCommand) {
		return new Command(remoteCommand, Command.Type.REMOTE);
	}

	private static Command cleanPreviousExecution(String folder) {
		String mkdirCommand = "ssh " + SSH_SCP_PRECOMMAND + " -p $" + Resource.ENV_SSH_PORT + " -i $"
				+ Resource.ENV_PRIVATE_KEY_FILE + " $" + Resource.ENV_SSH_USER + "@" + "$" + Resource.ENV_HOST
				+ " rm -rfv " + folder;
		return new Command(mkdirCommand, Command.Type.PROLOGUE);
	}

	private static Command mkdirRemoteFolder(String folder) {
		String mkdirCommand = "ssh " + SSH_SCP_PRECOMMAND + " -p $" + Resource.ENV_SSH_PORT + " -i $"
				+ Resource.ENV_PRIVATE_KEY_FILE + " $" + Resource.ENV_SSH_USER + "@" + "$" + Resource.ENV_HOST
				+ " mkdir " + folder;
		return new Command(mkdirCommand, Command.Type.PROLOGUE);
	}

	private static Command stageInCommand(String localFile, String remoteFile) {
		String scpCommand = "scp " + SSH_SCP_PRECOMMAND + " -P $" + Resource.ENV_SSH_PORT + " -i $"
				+ Resource.ENV_PRIVATE_KEY_FILE + " " + localFile + " $" + Resource.ENV_SSH_USER + "@" + "$"
				+ Resource.ENV_HOST + ":" + remoteFile;
		return new Command(scpCommand, Command.Type.PROLOGUE);
	}

	private static Command stageOutCommand(String remoteFile, String localFile) {

		String scpCommand = "scp " + SSH_SCP_PRECOMMAND + " -P $" + Resource.ENV_SSH_PORT + " -i $"
				+ Resource.ENV_PRIVATE_KEY_FILE + " $" + Resource.ENV_SSH_USER + "@" + "$" + Resource.ENV_HOST + ":"
				+ remoteFile + " " + localFile + "_[$" + Resource.ENV_SSH_PORT + "]";
		return new Command(scpCommand, Command.Type.EPILOGUE);
	}

	static class PrimeJob extends Job {

		@Override
		public void run(Task task) {
			tasksReady.remove(task);
			tasksRunning.add(task);

		}

		@Override
		public void finish(Task task) {
			tasksRunning.remove(task);
			tasksCompleted.add(task);
		}

		@Override
		public void fail(Task task) {
			tasksRunning.remove(task);
			tasksFailed.add(task);
		}

	}

}
