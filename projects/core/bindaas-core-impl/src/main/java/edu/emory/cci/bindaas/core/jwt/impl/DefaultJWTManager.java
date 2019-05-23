package edu.emory.cci.bindaas.core.jwt.impl;

import java.net.URLEncoder;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.UUID;

import edu.emory.cci.bindaas.core.jwt.token.IJWTManager;
import edu.emory.cci.bindaas.core.jwt.token.JWT;
import edu.emory.cci.bindaas.core.jwt.token.JWTManagerException;
import edu.emory.cci.bindaas.core.jwt.token.JWTManagerException.Reason;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.security.Key;

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
	
	private JWT userRequestToJWT(UserRequest userRequest)
	{
		JWT jwt = new JWT();
		jwt.setValue(userRequest.getJWT());
		jwt.setExpires(userRequest.getDateExpires());
		jwt.setEmailAddress(userRequest.getEmailAddress());
		jwt.setFirstName(userRequest.getFirstName());
		jwt.setLastName(userRequest.getLastName());
		return jwt;
	}

	
	
	
	
	@Override
	public JWT generateJWT(BindaasUser bindaasUser , Date dateExpires, String initiatedBy , String comments , ActivityType activityType , boolean throwErrorIfAlreadyExists)
			throws JWTManagerException {
		Session session = sessionFactory.openSession();

		try {
			session.beginTransaction();
			Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
			String jws = Jwts.builder().setSubject("Joe").signWith(key).compact();
			log.info("JWTManager generated: "+jws);
			UserRequest userRequest = new UserRequest();
			userRequest.setStage(Stage.accepted);
			userRequest.setJWT(jws);
			userRequest.setDateExpires(dateExpires);

			userRequest.setEmailAddress("example@email.com");
			userRequest.setFirstName("firstName");
			userRequest.setLastName("lastName");

			session.save(userRequest);
			HistoryLog historyLog = new HistoryLog();
			historyLog.setActivityType(activityType.toString());
			historyLog
					.setComments(comments);
			historyLog.setInitiatedBy(initiatedBy);
			historyLog.setUserRequest(userRequest);

			session.save(historyLog);
			session.getTransaction().commit();
			return userRequestToJWT(userRequest);
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
	}

}
