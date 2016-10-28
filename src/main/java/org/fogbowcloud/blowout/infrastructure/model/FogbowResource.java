package org.fogbowcloud.blowout.infrastructure.model;

import java.util.Properties;
import java.util.Scanner;

import org.apache.log4j.Logger;
import org.fogbowcloud.blowout.core.model.Specification;
import org.fogbowcloud.blowout.infrastructure.provider.fogbow.FogbowRequirementsHelper;

public class FogbowResource extends AbstractResource{

	private static final Logger LOGGER = Logger.getLogger(FogbowResource.class);

	public FogbowResource(String id, Properties properties) {
		super(id, properties);
	}

	public boolean match(Specification spec) {
		
		String fogbowRequirement = spec.getRequirementValue(FogbowRequirementsHelper.METADATA_FOGBOW_REQUIREMENTS);
		String image = spec.getImage();
		String publicKey = spec.getPublicKey();
		if (fogbowRequirement != null && image != null) {

			if (!FogbowRequirementsHelper.matches(this, fogbowRequirement)) {
				return false;
			}
			if (!image.equalsIgnoreCase(this.getMetadataValue(METADATA_IMAGE))) {
				return false;
			}
			if (!publicKey.equalsIgnoreCase(this.getMetadataValue(METADATA_PUBLIC_KEY))) {
				return false;
			}
		} else {
			return false;
		}

		return true;
	}

	protected boolean internalCheckConnectivity() {
		
		//TODO Implement SSH Resource ?
		
		String host = this.getMetadataValue(METADATA_SSH_HOST);
		String port = this.getMetadataValue(METADATA_SSH_PORT);

		Runtime run = null;
		Process p = null;
		Scanner scanner = null;

		try {
			run = Runtime.getRuntime();
			p = run.exec(new String[] { "/bin/bash", "-c",
					"echo quit | telnet " + host + " " + port + " 2>/dev/null | grep Connected" });
			p.waitFor();
			scanner = new Scanner(p.getInputStream());
			if (scanner.hasNext()) {
				String result = scanner.nextLine();
				if (result != null && !result.isEmpty()) {
					return true;
				}
			}
		} catch (Exception e) {
			return false;
		} finally {
			run = null;
			if (p != null) {
				p.destroy();
			}
			if (scanner != null) {
				scanner.close();
			}
		}
		return false;
	}

}