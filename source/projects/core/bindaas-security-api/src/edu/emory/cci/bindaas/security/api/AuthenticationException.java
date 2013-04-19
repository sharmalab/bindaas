package edu.emory.cci.bindaas.security.api;

public class AuthenticationException  extends Exception {

	
	
	private static final long serialVersionUID = 1L;
	private String message;
		public AuthenticationException()
		{
			this.message = "Authentication Failed. Please provide api_key in the HTTP Header";
		}
		public AuthenticationException(String userOrToken)
		{
			super();
			this.message = "Authentication Failed for  [" + userOrToken + "]";
		}
		@Override
		public String getMessage() {
			return message;
		}
		@Override
		public String toString() {
			
			return getMessage();
		}
		
		
}
