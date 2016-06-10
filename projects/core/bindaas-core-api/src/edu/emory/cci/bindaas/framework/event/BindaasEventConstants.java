package edu.emory.cci.bindaas.framework.event;

/**
 * superclass of all other events
 * 
 * @author nadir
 * 
 */
public class BindaasEventConstants {

	// Workspace Events
	public static final String CREATE_WORKSPACE_TOPIC = "edu/emory/cci/bindaas/framework/event/workspace/create";
	public static final String DELETE_WORKSPACE_TOPIC = "edu/emory/cci/bindaas/framework/event/workspace/delete";

	// Profile Events
	public static final String CREATE_PROFILE_TOPIC = "edu/emory/cci/bindaas/framework/event/profile/create";
	public static final String UPDATE_PROFILE_TOPIC = "edu/emory/cci/bindaas/framework/event/profile/update";
	public static final String DELETE_PROFILE_TOPIC = "edu/emory/cci/bindaas/framework/event/profile/delete";

	// Query Events
	public static final String CREATE_QUERY_TOPIC = "edu/emory/cci/bindaas/framework/event/query/create";
	public static final String UPDATE_QUERY_TOPIC = "edu/emory/cci/bindaas/framework/event/query/update";
	public static final String DELETE_QUERY_TOPIC = "edu/emory/cci/bindaas/framework/event/query/delete";

	public static final String PRE_QUERY_MODIFIER_TOPIC = "edu/emory/cci/bindaas/framework/event/query/pre-modifier";
	public static final String PRE_QUERY_EXECUTION_TOPIC = "edu/emory/cci/bindaas/framework/event/query/pre-execution";
	public static final String POST_QUERY_EXECUTION_TOPIC = "edu/emory/cci/bindaas/framework/event/query/post-execution";
	public static final String POST_QUERY_RESULT_TOPIC = "edu/emory/cci/bindaas/framework/event/query/post-result";

	// Delete Events
	public static final String CREATE_DELETE_TOPIC = "edu/emory/cci/bindaas/framework/event/delete/create";
	public static final String UPDATE_DELETE_TOPIC = "edu/emory/cci/bindaas/framework/event/delete/update";
	public static final String DELETE_DELETE_TOPIC = "edu/emory/cci/bindaas/framework/event/delete/delete";

	public static final String PRE_DELETE_EXECUTION_TOPIC = "edu/emory/cci/bindaas/framework/event/delete/pre-execution";
	public static final String POST_DELETE_EXECUTION_TOPIC = "edu/emory/cci/bindaas/framework/event/delete/post-execution";

	// Submit Events
	public static final String CREATE_SUBMIT_TOPIC = "edu/emory/cci/bindaas/framework/event/submit/create";
	public static final String UPDATE_SUBMIT_TOPIC = "edu/emory/cci/bindaas/framework/event/submit/update";
	public static final String DELETE_SUBMIT_TOPIC = "edu/emory/cci/bindaas/framework/event/submit/delete";

	public static final String PRE_PAYLOAD_MODIFIER_TOPIC = "edu/emory/cci/bindaas/framework/event/submit/pre-modifier";
	public static final String PRE_SUBMIT_EXECUTION_TOPIC = "edu/emory/cci/bindaas/framework/event/submit/pre-execution";
	public static final String POST_SUBMIT_EXECUTION_TOPIC = "edu/emory/cci/bindaas/framework/event/submit/post-execution";

	// Security Events
	public static final String AUTHENTICATION_TOPIC = "edu/emory/cci/bindaas/framework/event/authentication";
	public static final String AUTHORIZATION_TOPIC = "edu/emory/cci/bindaas/framework/event/authorization";

	// System Events
	public static final String BINDAAS_START = "edu/emory/cci/bindaas/framework/event/system/start";
	public static final String BINDAAS_STOP = "edu/emory/cci/bindaas/framework/event/system/stop";
	public static final String DEADLOCK_DETECTED = "edu/emory/cci/bindaas/framework/event/system/thread/deadlock";
	public static final String CRITICAL_MEMORY = "edu/emory/cci/bindaas/framework/event/system/memory/critical";
	public static final String CRITICAL_DISK_SPACE = "edu/emory/cci/bindaas/framework/event/system/diskspace/critical";

}
