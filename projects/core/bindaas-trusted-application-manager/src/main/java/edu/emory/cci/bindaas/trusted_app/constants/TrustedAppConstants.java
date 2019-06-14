package edu.emory.cci.bindaas.trusted_app.constants;

public class TrustedAppConstants {

	// Set a hard-limit for the maximum life time for the short lived API keys. In hours.
	public final static int MAXIMUM_LIFE_TIME_FOR_SHORT_LIVED_API_KEYS_IN_HOURS = 3;

	// Set a hard-limit for the maximum life time for the short lived API keys. In seconds.
	public final static int MAXIMUM_LIFE_TIME_FOR_SHORT_LIVED_API_KEYS =
			3600 * MAXIMUM_LIFE_TIME_FOR_SHORT_LIVED_API_KEYS_IN_HOURS;

	// Set a hard-limit for the minimum life time for the short lived API keys. In seconds.
	public final static int DEFAULT_LIFESPAN_OF_KEY_IN_SECONDS = 3600;

	// Set a hard-limit for the maximum life time for the short lived JWT. In hours.
	public final static int MAXIMUM_LIFE_TIME_FOR_SHORT_LIVED_JWT_IN_HOURS = 3;

	// Set a hard-limit for the maximum life time for the short lived JWT. In seconds.
	public final static int MAXIMUM_LIFE_TIME_FOR_SHORT_LIVED_JWT =
			3600 * MAXIMUM_LIFE_TIME_FOR_SHORT_LIVED_JWT_IN_HOURS;

	// Set a hard-limit for the minimum life time for the short lived JWT. In seconds.
	public final static int DEFAULT_LIFESPAN_OF_JWT_IN_SECONDS = 3600;


}