package edu.emory.cci.bindaas.webconsole.util;

import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.tools.generic.EscapeTool;

public class VelocityEngineWrapper {
	
	public VelocityEngine getVelocityEngine() {
		return velocityEngine;
	}

	public void setVelocityEngine(VelocityEngine velocityEngine) {
		this.velocityEngine = velocityEngine;
	}

	public Properties getProps() {
		return props;
	}

	public void setProps(Properties props) {
		this.props = props;
	}

	private final static String TEMPLATE_DIRECTORY_PATH = "META-INF/templates";
	private EscapeTool escapeTool = new EscapeTool();
	private Properties props ;
	private Log log = LogFactory.getLog(getClass());
	private VelocityEngine velocityEngine; 
	
	public void init() throws Exception
	{
		velocityEngine = new VelocityEngine();
		velocityEngine.init(props);	
		log.trace("VelocityEngine initialized");
	}
	
	
	public  Template getVelocityTemplateByName(String templateName)
	{
		String templateLoc = TEMPLATE_DIRECTORY_PATH + "/" + templateName;
		return velocityEngine.getTemplate(templateLoc);
	}

	public EscapeTool getEscapeTool() {
		return escapeTool;
	}

	public void setEscapeTool(EscapeTool escapeTool) {
		this.escapeTool = escapeTool;
	}
}
