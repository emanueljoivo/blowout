package org.fogbowcloud.infrastructure;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.fogbowcloud.scheduler.core.model.Resource;
import org.fogbowcloud.scheduler.core.model.Specification;
import org.fogbowcloud.scheduler.core.util.AppPropertiesConstants;
import org.fogbowcloud.scheduler.infrastructure.InfrastructureManager;
import org.fogbowcloud.scheduler.infrastructure.InfrastructureProvider;

public class InfrastructureMain implements ResourceNotifier {
	
	private Resource resource;
	
	private static final Logger LOGGER = Logger.getLogger(InfrastructureMain.class);

	public static void main(String[] args) throws Exception {
		
		//FIXME: mover para o objeto?
		LOGGER.debug("Starting infrastructure creation process...");

		//FIXME: block 1 parse args
		Properties properties = new Properties();
		FileInputStream input = new FileInputStream(args[0]);
		properties.load(input);
		
		String specsFilePath = args[1];
		
		// TODO optional?
		String wantStorage = args[2];
		
		boolean blockWhileInitializing = new Boolean(
				properties
						.getProperty(AppPropertiesConstants.INFRA_SPECS_BLOCK_CREATING))
				.booleanValue();

		//TODO: block2 create spec
		// TODO: Check later what need to be changed to support context-script
		List<Specification> specs = getSpecs(properties, specsFilePath);
		
		//TODO: create manager
		InfrastructureProvider infraProvider = createInfraProviderInstance(properties);
		
		InfrastructureManager infraManager = new InfrastructureManager(null, true,
				infraProvider, properties);
		infraManager.start(blockWhileInitializing);
		
		InfrastructureMain infraMain = new InfrastructureMain();

		//TODO: create and wait for compute resource
		infraManager.orderResource(specs.get(0), infraMain, 1);
		
		while (infraMain.resource == null) {
			Thread.sleep(2000);
		}
		
		infraManager.stop(false);

		//TODO: move to the end?
		String resourceStr = resourceAsString(infraMain.resource);
		
		//TODO: attach storage
		System.out.println(resourceStr);
		
		if (wantStorage.equals("true")) {
			String fogbowRequirements = specs.get(0).getRequirementValue(
					"FogbowRequirements");
			String[] splitRequirements = fogbowRequirements.split("&&");
			String requirement = splitRequirements[splitRequirements.length - 1];
			requirement = requirement.substring(1);
			
			String resourceComputeIdUncut = infraManager.getResourceComputeId(infraMain.resource);
			String[] splitResourceComputeId = resourceComputeIdUncut.split("@");
			String resourceComputeId = splitResourceComputeId[0];

			StorageInitializer storageInitializer = new StorageInitializer(
					resourceComputeId, requirement);
			storageInitializer.init();
		}

		LOGGER.debug("Infrastructure created.");

		//FIXME: parece que alguma thread to Infra estah pendurada. 
		System.exit(0);
	}
	
	private static List<Specification> getSpecs(Properties properties,
			String specsFilePath) throws IOException {

		LOGGER.info("Getting specs from file " + specsFilePath);
		return Specification.getSpecificationsFromJSonFile(specsFilePath);

	}
	
	private static InfrastructureProvider createInfraProviderInstance(Properties properties)
			throws Exception {

		String providerClassName = properties
				.getProperty(AppPropertiesConstants.INFRA_PROVIDER_CLASS_NAME);

		Object clazz = Class.forName(providerClassName).getConstructor(Properties.class).newInstance(properties);
		if (!(clazz instanceof InfrastructureProvider)) {
			throw new Exception(
					"Provider Class Name is not a InfrastructureProvider implementation");
		}

		return (InfrastructureProvider) clazz;
	}
	
	private static String resourceAsString(Resource resource) {
		return "USER NAME: "
				+ resource.getMetadataValue(Resource.METADATA_SSH_USERNAME_ATT)
				+ "\nSSH HOST: "
				+ resource.getMetadataValue(Resource.METADATA_SSH_HOST)
				+ "\nSSH PORT: "
				+ resource.getMetadataValue(Resource.METADATA_SSH_PORT)
				+ "\nEXTRA PORT: "
				+ resource.getMetadataValue(Resource.METADATA_EXTRA_PORTS_ATT);
	}

	@Override
	public void resourceReady(Resource resource) {
		LOGGER.debug("Receiving new assigned resource...");
		
		if(resource == null) {
			LOGGER.error("Received resource is null");
			return;
		}
		
		this.resource = resource;
		
		LOGGER.debug("Process finished.");
	}
	
}