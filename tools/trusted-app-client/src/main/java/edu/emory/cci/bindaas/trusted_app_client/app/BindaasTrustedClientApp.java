package edu.emory.cci.bindaas.trusted_app_client.app;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import edu.emory.cci.bindaas.trusted_app_client.core.TrustedAppClientImpl;

public class BindaasTrustedClientApp {

	private Log log = LogFactory.getLog(getClass());
	private Option action;
	private Option protocol;
	private Option applicationID;
	private Option applicationSecret;
	private Option baseUrl;
	private Option username;
	private Option expires;
	private Option comments;
	private Option lifetime;
	
	private Options options;
	private SimpleDateFormat dateFormat;
	private HelpFormatter formatter;
	private Gson gson;

	public void init()
	{
		dateFormat = new SimpleDateFormat("MM/dd/yyyy");
		gson = new GsonBuilder().setPrettyPrinting().create();
		action = new Option("action" , true  , "Action to be performed. Allowed values are :\n a - authorize new user \n r - revoke user \n i - issue short-lived API Key"); 
		protocol = new Option("protocol",true,"Authentication protocol to use. Allowed values are :\n api_key - use API KEY\n jwt - use JSON Web Token");
		applicationID = new Option("id" , true , "Application ID");
		applicationSecret = new Option("secret" , true , "Application Secret ");
		username = new Option("username" , true , "Username");
		expires = new Option("expires" , true , "Expiration Date. Must be specified with authorize new user action in mm/dd/yyyy format");
		baseUrl = new Option("url" , true , "Base URL of the service");
		lifetime = new Option("lifetime" , true , "lifetime of the short-lived API Key in seconds. Optional");
		comments = new Option("comments" , true , "comments. Optional");
		

		this.options = new Options();
		options.addOption(action);
		options.addOption(protocol);
		options.addOption(applicationID);
		options.addOption(applicationSecret);
		options.addOption(username);
		options.addOption(expires);
		options.addOption(baseUrl);
		options.addOption(lifetime);
		options.addOption(comments);
		
		formatter = new HelpFormatter();
	}
	
	private void printHelp()
	{
		formatter.printHelp("java -jar App.jar ", options);
	}
	
	
	
	public void parseArguments(String[] args)
	{

		
		CommandLineParser parser = new BasicParser();
	    try {
	
	    	CommandLine line = parser.parse( options, args );
	    	if(line.hasOption("action"))
	    	{
	    		String action = line.getOptionValue("action");
	    		if(action.equals("a"))
	    		{
	    			Arguments authArg = parseAuthorizeArguments(line);
	    			TrustedAppClientImpl client = new TrustedAppClientImpl(authArg.baseUrl,authArg.applicationID, authArg.applicationSecret);
	    			JsonObject response = client.authorizeNewUser(authArg.protocol, authArg.username, authArg.expiry.getTime(), authArg.comments);
	    			log.info("Server Returned :\n" + gson.toJson(response));
	    		}else if(action.equals("r"))
	    		{
	    			Arguments authArg = parseRevokeArguments(line);
	    			TrustedAppClientImpl client = new TrustedAppClientImpl(authArg.baseUrl,authArg.applicationID, authArg.applicationSecret);
					JsonObject response = client.revokeAccess(authArg.protocol, authArg.username, authArg.comments);
					log.info("Server Returned :\n" + gson.toJson(response));
	    		} else if (action.equals("i"))
	    		{
	    			Arguments authArg = parseShortLivedAuthenticationTokenArguments(line);
	    			TrustedAppClientImpl client = new TrustedAppClientImpl(authArg.baseUrl,authArg.applicationID, authArg.applicationSecret);
					JsonObject response = client.getShortLivedAuthenticationToken(authArg.protocol, authArg.username,authArg.lifetime);
					log.info("Server Returned :\n" + gson.toJson(response));
	    		}
	    		else if (action.equals("l"))
	    		{
	    			Arguments authArg = parseListAuthenticationTokensArguments(line);
	    			TrustedAppClientImpl client = new TrustedAppClientImpl(authArg.baseUrl,authArg.applicationID, authArg.applicationSecret);
					JsonArray response = client.listAuthenticationTokens(authArg.protocol);
					log.info("Server Returned :\n" + gson.toJson(response));
	    		}
	    		else
	    		{
	    			throw new IllegalArgumentException("Allowed values for action are [a|r|i]");
	    		}
	    	}
	    	else
	    	{
	    		throw new IllegalArgumentException("Missing mandatory option [action]"); 
	    	}
	    	
	        
	    }
	    catch( ParseException exp ) {
	    	log.error( "Parsing failed. Reason: " + exp.getMessage());
	    }
	    catch(IllegalArgumentException e)
	    {
	    	log.error(e.getMessage());
	    	printHelp();
	    }
	    catch(Exception e)
	    {
	    	log.error(e);
	    }
	}
	
	
	public static void main(String[] args) {
		BindaasTrustedClientApp app = new BindaasTrustedClientApp();
		app.init();
		app.parseArguments(args);
		
	}
	
	private Arguments parseAuthorizeArguments(CommandLine line) throws IllegalArgumentException
	{
		Arguments args = new Arguments();
		
		if(line.hasOption("url"))
		{
			args.baseUrl = line.getOptionValue("url");
		}
		else
		{
			throw new IllegalArgumentException("[url] not specified");
		}
		
		if(line.hasOption("id"))
		{
			args.applicationID = line.getOptionValue("id");
		}
		else
		{
			throw new IllegalArgumentException("[id] not specified");
		}
		
		if(line.hasOption("secret"))
		{
			args.applicationSecret = line.getOptionValue("secret");
		}
		else
		{
			throw new IllegalArgumentException("[secret] not specified");
		}
		
		if(line.hasOption("username"))
		{
			args.username = line.getOptionValue("username");
		}
		else
		{
			throw new IllegalArgumentException("[username] not specified");
		}

		if(line.hasOption("protocol"))
		{
			if(line.getOptionValue("protocol").matches("api_key|jwt")){
				args.protocol = line.getOptionValue("protocol");
			}
			else {
				throw new IllegalArgumentException("Illegal value for [protocol]");
			}

		}
		else
		{
			args.protocol = "api_key";
			log.warn("[protocol] not specified. Using default value of api_key");
		}

		if(line.hasOption("id"))
		{
			args.applicationID = line.getOptionValue("id");
		}
		else
		{
			throw new IllegalArgumentException("[id] not specified");
		}
		
		if(line.hasOption("expires"))
		{
			try {
				args.expiry = dateFormat.parse(line.getOptionValue("expires"));
			} catch (java.text.ParseException e) {
				throw new IllegalArgumentException("Illegal Date Format for [expires]", e);
			}
		}
		else
		{
			throw new IllegalArgumentException("[expires] not specified");
		}
		
		if(line.hasOption("comments"))
		{
			args.comments = line.getOptionValue("comments");
		}
			
		return args;
	}
	
	private Arguments parseShortLivedAuthenticationTokenArguments(CommandLine line) throws IllegalArgumentException
	{
		Arguments args = new Arguments();
		if(line.hasOption("url"))
		{
			args.baseUrl = line.getOptionValue("url");
		}
		else
		{
			throw new IllegalArgumentException("[url] not specified");
		}
		
		if(line.hasOption("id"))
		{
			args.applicationID = line.getOptionValue("id");
		}
		else
		{
			throw new IllegalArgumentException("[id] not specified");
		}
		
		if(line.hasOption("secret"))
		{
			args.applicationSecret = line.getOptionValue("secret");
		}
		else
		{
			throw new IllegalArgumentException("[secret] not specified");
		}
		
		if(line.hasOption("username"))
		{
			args.username = line.getOptionValue("username");
		}
		else
		{
			throw new IllegalArgumentException("[username] not specified");
		}

		if(line.hasOption("protocol"))
		{
			if(line.getOptionValue("protocol").matches("api_key|jwt")){
				args.protocol = line.getOptionValue("protocol");
			}
			else {
				throw new IllegalArgumentException("Illegal value for [protocol]");
			}

		}
		else
		{
			args.protocol = "api_key";
			log.warn("[protocol] not specified. Using default value of api_key");
		}

		if(line.hasOption("id"))
		{
			args.applicationID = line.getOptionValue("id");
		}
		else
		{
			throw new IllegalArgumentException("[id] not specified");
		}
		
		if(line.hasOption("lifetime"))
		{
			args.lifetime = Integer.parseInt(line.getOptionValue("lifetime"));
		}
		
	
		return args;
	}
	
	private Arguments parseListAuthenticationTokensArguments(CommandLine line) throws IllegalArgumentException
	{
		Arguments args = new Arguments();
		if(line.hasOption("url"))
		{
			args.baseUrl = line.getOptionValue("url");
		}
		else
		{
			throw new IllegalArgumentException("[url] not specified");
		}
		
		if(line.hasOption("secret"))
		{
			args.applicationSecret = line.getOptionValue("secret");
		}
		else
		{
			throw new IllegalArgumentException("[secret] not specified");
		}

		if(line.hasOption("protocol"))
		{
			if(line.getOptionValue("protocol").matches("api_key|jwt")){
				args.protocol = line.getOptionValue("protocol");
			}
			else {
				throw new IllegalArgumentException("Illegal value for [protocol]");
			}

		}
		else
		{
			args.protocol = "api_key";
			log.warn("[protocol] not specified. Using default value of api_key");
		}
		
		if(line.hasOption("id"))
		{
			args.applicationID = line.getOptionValue("id");
		}
		else
		{
			throw new IllegalArgumentException("[id] not specified");
		}
		
	
		return args;
	}
	
	private Arguments parseRevokeArguments(CommandLine line) throws IllegalArgumentException
	{
		Arguments args = new Arguments();

		if(line.hasOption("url"))
		{
			args.baseUrl = line.getOptionValue("url");
		}
		else
		{
			throw new IllegalArgumentException("[url] not specified");
		}
		
		if(line.hasOption("id"))
		{
			args.applicationID = line.getOptionValue("id");
		}
		else
		{
			throw new IllegalArgumentException("[id] not specified");
		}
		
		if(line.hasOption("secret"))
		{
			args.applicationSecret = line.getOptionValue("secret");
		}
		else
		{
			throw new IllegalArgumentException("[secret] not specified");
		}
		
		if(line.hasOption("username"))
		{
			args.username = line.getOptionValue("username");
		}
		else
		{
			throw new IllegalArgumentException("[username] not specified");
		}

		if(line.hasOption("protocol"))
		{
			if(line.getOptionValue("protocol").matches("api_key|jwt")){
				args.protocol = line.getOptionValue("protocol");
			}
			else {
				throw new IllegalArgumentException("Illegal value for [protocol]");
			}

		}
		else
		{
			args.protocol = "api_key";
			log.warn("[protocol] not specified. Using default value of api_key");
		}

		if(line.hasOption("id"))
		{
			args.applicationID = line.getOptionValue("id");
		}
		else
		{
			throw new IllegalArgumentException("[id] not specified");
		}
		
		if(line.hasOption("comments"))
		{
			args.comments = line.getOptionValue("comments");
		}
	
		return args;
	}
	
	
	private static class Arguments {
		String baseUrl;
		String username;
		String applicationID;
		String applicationSecret;
		String protocol;
		Date expiry;
		String comments;
		Integer lifetime;
		
	}
}
