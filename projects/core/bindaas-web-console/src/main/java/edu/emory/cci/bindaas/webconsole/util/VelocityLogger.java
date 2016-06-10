package edu.emory.cci.bindaas.webconsole.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.runtime.RuntimeServices;

public class VelocityLogger implements org.apache.velocity.runtime.log.LogChute {

	private Log log = LogFactory.getLog(getClass());
	
	@Override
	public void init(RuntimeServices arg0) throws Exception {
		// do nothing
		
	}

	@Override
	public boolean isLevelEnabled(int arg0) {

		return false;
	}

	@Override
	public void log(int arg0, String arg1) {
		log.trace(arg1);
		
	}

	@Override
	public void log(int arg0, String arg1, Throwable arg2) {
		log.error(arg1, arg2);
		
	}

}
