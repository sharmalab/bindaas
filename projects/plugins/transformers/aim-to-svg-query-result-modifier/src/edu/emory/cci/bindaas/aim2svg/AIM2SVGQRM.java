package edu.emory.cci.bindaas.aim2svg;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.regex.Pattern;

import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;

import com.google.gson.JsonObject;

import edu.emory.cci.bindaas.aim2svg.bundle.Activator;
import edu.emory.cci.bindaas.framework.api.IQueryResultModifier;
import edu.emory.cci.bindaas.framework.model.ModifierException;
import edu.emory.cci.bindaas.framework.model.QueryResult;
import edu.emory.cci.bindaas.framework.model.QueryResult.Callback;
import edu.emory.cci.bindaas.framework.model.RequestContext;
import edu.emory.cci.bindaas.framework.provider.exception.AbstractHttpCodeException;
import edu.emory.cci.bindaas.framework.provider.exception.ModifierExecutionFailedException;
import edu.emory.cci.bindaas.framework.util.DocumentationUtil;

public class AIM2SVGQRM implements IQueryResultModifier {

	private Log log = LogFactory.getLog(getClass());
	private static final String DOCUMENTATION_RESOURCES_LOCATION = "META-INF/documentation";
	private JsonObject documentation;
	private Templates template;
	private Pattern inPattern;
	private Pattern outPattern;

	@Override
	public JsonObject getDocumentation() {

		return documentation;
	}

	public void init() throws TransformerConfigurationException, IOException {
		BundleContext context = Activator.getContext();
		
		InputStream xslt;
		try {
			xslt = context.getBundle().getEntry("META-INF/AIMv3r11MapToSVG11.xslt").openStream();
		} catch (IOException e1) {
			log.error(e1);
			throw e1;			
		}

		TransformerFactory transFact = new net.sf.saxon.TransformerFactoryImpl();
        try {
			template = transFact.newTemplates(new javax.xml.transform.stream.StreamSource(xslt));
		} catch (TransformerConfigurationException e) {
			log.error(e);
			throw e;
		}
        inPattern = Pattern.compile("<ImageAnnotation.*?</ImageAnnotation>", Pattern.DOTALL);
        outPattern = Pattern.compile("<svg.*</svg>", Pattern.DOTALL);

		documentation = DocumentationUtil.getProviderDocumentation(context,
				DOCUMENTATION_RESOURCES_LOCATION);
		Dictionary<String, String> props = new Hashtable<String, String>();
		props.put("class", getClass().getName());
		context.registerService(IQueryResultModifier.class.getName(), this,
				props);
	}

	@Override
	public void validate() throws ModifierException {
		// TODO Auto-generated method stub

	}

	@Override
	public QueryResult modifyQueryResult(final QueryResult queryResult,
			JsonObject dataSource, RequestContext requestContext, JsonObject modifierProperties , Map<String,String> runtimeParameters)
			throws AbstractHttpCodeException {
		
		queryResult.setMimeType("image/svg+xml");
		queryResult.setCallback(new Callback() {
			
			@Override
			public void callback(OutputStream servletOutputStream,
					Properties context) throws AbstractHttpCodeException {
				
				try {
						
			        javax.xml.transform.Transformer trans;
					trans = template.newTransformer();
			        
			        String tag = "<results>\n";
			        servletOutputStream.write(tag.getBytes());
			        String eol = "\n";
			        
					Scanner s = new Scanner(queryResult.getData());
					String nextMatch = s.findWithinHorizon(inPattern, 0);
					while (nextMatch != null) {

						ByteArrayOutputStream baos = new ByteArrayOutputStream();
				        javax.xml.transform.Result result =
				                new javax.xml.transform.stream.StreamResult(baos);
						
				        javax.xml.transform.Source xmlSource =
				        		new javax.xml.transform.stream.StreamSource(new StringReader(nextMatch));
				        trans.transform(xmlSource, result);
				        
				        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
				        Scanner s2 = new Scanner(bais);
				        String svg = s2.findWithinHorizon(outPattern, 0);
				        
				        servletOutputStream.write(svg.getBytes());
				        servletOutputStream.write(eol.getBytes());
						
						nextMatch = s.findWithinHorizon(inPattern, 0);
					}
					tag = "</results>\n";
					servletOutputStream.write(tag.getBytes());

				} catch (Exception e) {
					log.error(e);
					throw new ModifierExecutionFailedException(getClass().getName() , 1 , e);
				}
				
			}

		});

		return queryResult;
	}


	@Override
	public String getDescriptiveName() {

		return "Convert AIM annotations to SVG";
	}

}
