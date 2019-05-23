package edu.emory.cci.bindaas.core.jwt;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;

import edu.emory.cci.bindaas.core.jwt.IJWTManager;
import edu.emory.cci.bindaas.core.jwt.JWTManagerException;
import edu.emory.cci.bindaas.core.jwt.JWTManagerException.Reason;
import edu.emory.cci.bindaas.core.model.hibernate.HistoryLog;
import edu.emory.cci.bindaas.core.model.hibernate.HistoryLog.ActivityType;
import edu.emory.cci.bindaas.core.model.hibernate.UserRequest;
import edu.emory.cci.bindaas.core.model.hibernate.UserRequest.Stage;
import edu.emory.cci.bindaas.security.api.BindaasUser;


public class DefaultJWTManager implements IJWTManager {

	private Log log = LogFactory.getLog(getClass());
	private SessionFactory sessionFactory;
	
	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	private final static String secret = "fj32Jfv02Mq33g0f8ioDkw";


	@Override
	public String generateJWT()
			throws JWTManagerException {
		Session session = sessionFactory.openSession();

		try {
			session.beginTransaction();

			String jws = JWT.create()
					.withIssuer("bindaas")
					.withClaim("email", "example@email.com")
					.sign(Algorithm.HMAC256(secret));

			log.info("JWTManager generated: "+jws);
			UserRequest userRequest = new UserRequest();
			userRequest.setStage(Stage.accepted);
			userRequest.setJWT(jws);
//			userRequest.setDateExpires(dateExpires);

			userRequest.setEmailAddress("example@email.com");
			userRequest.setFirstName("firstName");
			userRequest.setLastName("lastName");

			session.save(userRequest);
			HistoryLog historyLog = new HistoryLog();
//			historyLog.setActivityType(activityType.toString());
//			historyLog.setComments(comments);
//			historyLog.setInitiatedBy(initiatedBy);
			historyLog.setUserRequest(userRequest);

			session.save(historyLog);
			session.getTransaction().commit();
			return jws;
		}

		//catch (JWTManagerException e) { throw e ;}
		catch (Exception e) {
			log.error(e);
			session.getTransaction().rollback();
			throw new JWTManagerException(e,Reason.PROCESSING_ERROR);
		} finally {
			session.close();
		}

	}

	public void init() throws Exception
	{
		log.info("DefaultJWTManager started");
		String jws = generateJWT();
		Algorithm algorithm = Algorithm.HMAC256(secret);
		JWTVerifier verifier = JWT.require(algorithm)
				.withIssuer("bindaas")
				.build();
		DecodedJWT decodedJWT = verifier.verify(jws);
		Claim claim = decodedJWT.getClaim("email");
		log.info(claim.asString());
	}

}
