package edu.emory.cci.bindaas.security.api;

public class AuthenticationException  extends Exception {

	private String userOrToken;
		public AuthenticationException(String userOrToken)
		{
			super();
			this.userOrToken = userOrToken;
		}
		@Override
		public String getMessage() {
			return "Authentication Failed for  [" + userOrToken + "]";
		}
		@Override
		public String toString() {
			
			return getMessage();
		}
		
		
}
