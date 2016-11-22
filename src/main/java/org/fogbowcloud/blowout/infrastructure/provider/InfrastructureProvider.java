package org.fogbowcloud.blowout.infrastructure.provider;

import org.fogbowcloud.blowout.core.model.Specification;
import org.fogbowcloud.blowout.infrastructure.exception.RequestResourceException;
import org.fogbowcloud.blowout.pool.AbstractResource;

public interface InfrastructureProvider {

	/**
	 * Creates new Request for resource and return the Request ID
	 * @param specification
	 * @return The requested resource
	 */
	AbstractResource requestResource(Specification specification) throws RequestResourceException;
	
	AbstractResource getResource(String resourceId);
	
	void deleteResource(String resourceId) throws Exception;
	
}
