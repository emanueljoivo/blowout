package org.fogbowcloud.blowout.core.util;

public class AppPropertiesConstants {

	// __________ INFRASTRUCTURE CONSTANTS __________ //
	public static final String INFRA_IS_STATIC = "infra_is_elastic";
	public static final String INFRA_PROVIDER_CLASS_NAME = "infra_provider_class_name";
	public static final String INFRA_MANAGEMENT_SERVICE_TIME = "infra_management_service_time";
	public static final String INFRA_RESOURCE_SERVICE_TIME = "infra_resource_service_time";
	public static final String INFRA_RESOURCE_CONNECTION_TIMEOUT = "infra_resource_connection_timeout";
	public static final String INFRA_RESOURCE_IDLE_LIFETIME = "infra_resource_idle_lifetime";
	public static final String INFRA_RESOURCE_REUSE_TIMES = "max_resource_reuse";
	public static final String INFRA_RESOURCE_CONNECTION_RETRY = "max_resource_connection_retry";
	public static final String INFRA_MONITOR_PERIOD = "infra_monitor_period";

	public static final String INFRA_INITIAL_SPECS_FILE_PATH = "infra_initial_specs_file_path";
	public static final String INFRA_CRAWLER_SPECS_FILE_PATH = "infra_crawler_specs_file_path";
	public static final String INFRA_SCHEDULER_SPECS_FILE_PATH = "infra_scheduler_specs_file_path";
	public static final String INFRA_FETCHER_SPECS_FILE_PATH = "infra_fetcher_specs_file_path";
	public static final String INFRA_SPECS_BLOCK_CREATING = "infra_specs_block_creating";
	public static final String SCHEDULER_PERIOD = "scheduler_period";

	public static final String INFRA_INITIAL_SPECS_BLOCK_CREATING = "infra_initial_specs_block_creating";

	// __________ FOGBOW INFRASTRUCTURE CONSTANTS __________ //
	public static final String INFRA_FOGBOW_MANAGER_BASE_URL = "infra_fogbow_manager_base_url";
	public static final String INFRA_FOGBOW_TOKEN_PUBLIC_KEY_FILEPATH = "infra_fogbow_token_public_key_filepath";
	public static final String INFRA_FOGBOW_USERNAME = "fogbow_username";
	public static final String INFRA_FOGBOW_TOKEN_UPDATE_PLUGIN = "infra_fogbow_token_update_plugin";
	public static final String TOKEN_UPDATE_TIME = "token_update.time";
	public static final String TOKEN_UPDATE_TIME_UNIT = "token_update.time.unit";
	public static final String NAF_IDENTITY_PRIVATE_KEY = "naf_identity_private_key";
	public static final String NAF_IDENTITY_PUBLIC_KEY = "naf_identity_private_key";
	public static final String NAF_IDENTITY_TOKEN_USERNAME = "naf_identity_token_username";
	public static final String NAF_IDENTITY_TOKEN_PASSWORD = "naf_identity_token_password";
	public static final String NAF_IDENTITY_TOKEN_GENERATOR_URL = "naf_identity_token_generator_endpoint";

	public static final String EXECUTION_MONITOR_PERIOD = "execution_monitor_period";

	public static final String REST_SERVER_PORT = "rest_server_port";
	
	public static final String LOCAL_COMMAND_INTERPRETER = "local_command_interpreter";


	// ___________ DB CONSTANTS ______________//

	public static final String DB_MAP_NAME = "jobMap";
	public static final String DB_MAP_USERS = "users";
	public static final String DB_FILE_NAME = "legacyJobs.db";
	public static final String DB_FILE_USERS = "usersmap.db";
	public static final String DB_REST_SERVER_PORT = "db_rest_server_port";
	public static final String DB_DATASTORE_URL = "datastore_url";

	// ___________ APPLICATION HEADERS ____//

	public static final String X_AUTH_NONCE = "X-auth-nonce";
	public static final String X_AUTH_USER = "X-auth-username";
	public static final String X_AUTH_HASH = "X-auth-hash";

	// ___________ SWIFT PROPERTIES ___________//
	public static final String SWIFT_CONTAINER_NAME = "swift_container_name";
	public static final String SWIFT_PSEUD_FOLDER_PREFIX = "swift_pseud_folder_prefix";
	public static final String SWIFT_USERNAME = "swift_username";
	public static final String SWIFT_PASSWORD = "swift_password";
	public static final String SWIFT_TENANT_NAME = "swift_tenant_name";
	public static final String SWIFT_AUTH_URL = "swift_auth_url";
	public static final String SWIFT_IMAGE_EXTENSION = "swift_image_extension";

}
