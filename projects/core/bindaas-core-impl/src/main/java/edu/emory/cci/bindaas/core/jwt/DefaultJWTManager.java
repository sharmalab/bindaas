package edu.emory.cci.bindaas.core.jwt;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.interfaces.RSAPublicKey;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;

import com.auth0.jwk.GuavaCachedJwkProvider;
import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkException;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.UrlJwkProvider;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.JWT;

import edu.emory.cci.bindaas.core.jwt.JWTManagerException.Reason;
import edu.emory.cci.bindaas.core.model.hibernate.HistoryLog;
import edu.emory.cci.bindaas.core.model.hibernate.HistoryLog.ActivityType;
import edu.emory.cci.bindaas.core.model.hibernate.UserRequest;
import edu.emory.cci.bindaas.core.model.hibernate.UserRequest.Stage;
import edu.emory.cci.bindaas.framework.util.GSONUtil;
import edu.emory.cci.bindaas.security.api.BindaasUser;

import static edu.emory.cci.bindaas.core.rest.security.SecurityHandler.invalidateJWT;

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
	// FIXME: read values from file
	private final static String domain = "tushar-97.auth0.com";


	@Override
	public Boolean verifyToken(String token)
			throws JWTManagerException {

		try{
			DecodedJWT jwt = JWT.decode(token);

			JwkProvider http = new UrlJwkProvider(domain);
			JwkProvider provider = new GuavaCachedJwkProvider(http);

			Jwk jwk = provider.get(jwt.getKeyId());
			Algorithm algorithm = Algorithm.RSA256((RSAPublicKey) jwk.getPublicKey(),null);

			algorithm.verify(jwt);

			return true;
		}
		catch(JwkException e){
			log.error(e);
			throw new JWTManagerException(e, Reason.PROCESSING_ERROR);

		}

	}

	@Override
	public BindaasUser createUser(String token, String initiatedBy, String comments, ActivityType activityType)
			throws JWTManagerException{
		Session session = sessionFactory.openSession();

		try{
			session.beginTransaction();

			URL urlForUserInfo = new URL("https://" + domain +"/userinfo") ;

			HttpURLConnection request = (HttpURLConnection) urlForUserInfo.openConnection();
			request.setRequestMethod("GET");
			request.setRequestProperty("Authorization","Bearer "+token);

			StringBuilder content;

			try (BufferedReader in = new BufferedReader(
					new InputStreamReader(request.getInputStream()))) {

				String line;
				content = new StringBuilder();

				while ((line = in.readLine()) != null) {
					content.append(line);
				}
			}

			UserInfo userInfo = GSONUtil.getGSONInstance().fromJson(content.toString(), UserInfo.class);


			String firstName = userInfo.getGivenName();
			String lastName = userInfo.getFamilyName();

			if(firstName == null || lastName == null) {
				if(userInfo.getName().contains(" ")) {
					firstName = userInfo.getName().split(" ")[0];
					lastName = userInfo.getName().split(" ")[1];
				}
				else if(userInfo.getName().contains("@")) {
					firstName = userInfo.getName().split("@")[0];
					lastName = userInfo.getName().split("@")[0];
				}
				else {
					firstName = userInfo.getName();
					lastName = userInfo.getName();
				}
			}

			String emailAddress = userInfo.getEmail();

			UserRequest userRequest = new UserRequest();
			userRequest.setStage(Stage.accepted);
			userRequest.setJWT(token);
			userRequest.setDateExpires(getExpires(token));

			userRequest.setFirstName(firstName);
			userRequest.setLastName(lastName);
			userRequest.setEmailAddress(emailAddress);

			session.save(userRequest);
			HistoryLog historyLog = new HistoryLog();
			historyLog.setActivityType(activityType.toString());
			historyLog.setComments(comments);
			historyLog.setInitiatedBy(initiatedBy);
			historyLog.setUserRequest(userRequest);

			session.save(historyLog);
			session.getTransaction().commit();

			BindaasUser principal = new BindaasUser(userInfo.getEmail());
			principal.setName(firstName);
			principal.addProperty(BindaasUser.FIRST_NAME,firstName);
			principal.addProperty(BindaasUser.LAST_NAME,lastName);
			principal.addProperty(BindaasUser.EMAIL_ADDRESS,emailAddress);
			principal.addProperty("jwt",token);

			return principal;

		}
		catch (Exception e){
			log.error(e);
			throw new JWTManagerException(e,Reason.PROCESSING_ERROR);
		}

	}

	// usage in trusted-app-client only
	@Override
	public String generateJWT(BindaasUser bindaasUser , Date dateExpires, String initiatedBy , String comments , ActivityType activityType , boolean throwErrorIfAlreadyExists)
			throws JWTManagerException {
		Session session = sessionFactory.openSession();

		try {
			session.beginTransaction();

			String emailAddress = bindaasUser
					.getProperty(BindaasUser.EMAIL_ADDRESS) != null ? bindaasUser
					.getProperty(BindaasUser.EMAIL_ADDRESS).toString()
					: bindaasUser.getName() + "@" + bindaasUser.getDomain();
			String firstName = bindaasUser
					.getProperty(BindaasUser.FIRST_NAME) != null ? bindaasUser
					.getProperty(BindaasUser.FIRST_NAME).toString()
					: bindaasUser.getName();
			String lastName = bindaasUser.getProperty(BindaasUser.LAST_NAME) != null ? bindaasUser
					.getProperty(BindaasUser.LAST_NAME).toString()
					: bindaasUser.getName();
			
			@SuppressWarnings("unchecked")
			List<UserRequest> listOfValidTokens = (List<UserRequest>) session
					.createCriteria(UserRequest.class)
					.add(Restrictions.eq("stage", Stage.accepted.name()))
					.add(Restrictions.eq("emailAddress", emailAddress))
					.add(Restrictions.gt("dateExpires", new Date()))
					.add(Restrictions.isNotNull("jwt"))
					.list();

			if (listOfValidTokens != null && listOfValidTokens.size() > 0) {
				UserRequest request = listOfValidTokens.get(0);
				if(throwErrorIfAlreadyExists)
				{
					throw new JWTManagerException("JWT for the user already exists" , Reason.TOKEN_ALREADY_EXIST);
				}
				else
				{
					return request.getJWT();
				}
			}


			// generates JWT for first time user
			String jws = JWT.create()
					.withIssuer("bindaas")
					.withExpiresAt(dateExpires)
					.sign(Algorithm.HMAC256(secret));

			UserRequest userRequest = new UserRequest();
			userRequest.setStage(Stage.accepted);
			userRequest.setJWT(jws);
			userRequest.setDateExpires(dateExpires);

			userRequest.setEmailAddress(emailAddress);
			userRequest.setFirstName(firstName);
			userRequest.setLastName(lastName);

			session.save(userRequest);
			HistoryLog historyLog = new HistoryLog();
			historyLog.setActivityType(activityType.toString());
			historyLog.setComments(comments);
			historyLog.setInitiatedBy(initiatedBy);
			historyLog.setUserRequest(userRequest);

			session.save(historyLog);
			session.getTransaction().commit();
			return jws;
		}

		catch (JWTManagerException e) { throw e ;}
		catch (Exception e) {
			log.error(e);
			session.getTransaction().rollback();
			throw new JWTManagerException(e,Reason.PROCESSING_ERROR);
		} finally {
			session.close();
		}

	}

	// usage in trusted-app-client only
	@Override
	public String createShortLivedJWT(BindaasUser bindaasUser, int lifetime , String applicationId) throws JWTManagerException {
		Session session = sessionFactory.openSession();
		try {
			session.beginTransaction();

			String emailAddress = bindaasUser
					.getProperty(BindaasUser.EMAIL_ADDRESS) != null ? bindaasUser
					.getProperty(BindaasUser.EMAIL_ADDRESS).toString()
					: bindaasUser.getName() + "@" + bindaasUser.getDomain();


			@SuppressWarnings("unchecked")
			List<UserRequest> listOfValidTokens = (List<UserRequest>) session
					.createCriteria(UserRequest.class)
					.add(Restrictions.eq("stage", Stage.accepted.name()))
					.add(Restrictions.eq("emailAddress", emailAddress))
					.add(Restrictions.gt("dateExpires", new Date()))
					.add(Restrictions.isNotNull("jwt"))
					.list();

			if (listOfValidTokens != null && listOfValidTokens.size() > 0) {
				UserRequest request = listOfValidTokens.get(0);

				// generate a short lived JWT for this user
				GregorianCalendar calendar = new GregorianCalendar();
				calendar.add(Calendar.SECOND , lifetime);

				Date newExpirationDate = request.getDateExpires().before(calendar.getTime()) ? request.getDateExpires() : calendar.getTime();

				String jws = JWT.create()
						.withIssuer("bindaas")
						.withExpiresAt(newExpirationDate)
						.sign(Algorithm.HMAC256(secret));

				UserRequest sessionKey = new UserRequest();

				sessionKey.setJWT(jws);
				sessionKey.setFirstName(request.getFirstName());
				sessionKey.setLastName(request.getLastName());
				sessionKey.setEmailAddress(emailAddress);
				sessionKey.setReason(request.getReason());
				sessionKey.setRequestDate(new Date());
				sessionKey.setStage(Stage.accepted);
				sessionKey.setDateExpires(newExpirationDate);

				session.save(sessionKey);

				HistoryLog historyLog = new HistoryLog();
				historyLog.setActivityType(ActivityType.SYSTEM_APPROVE);
				historyLog.setComments("Short-Lived JWT");
				historyLog.setInitiatedBy(applicationId);
				historyLog.setUserRequest(sessionKey);

				session.save(historyLog);
				session.getTransaction().commit();

				return jws;

			} else {
				throw new JWTManagerException("Cannot generate JWT for [" + bindaasUser + "]. The User must apply for it first!!" , Reason.TOKEN_DOES_NOT_EXIST);
			}

		}
		catch (JWTManagerException e) {
			throw e;
		}
		catch (Exception e) {
			log.error(e);
			session.getTransaction().rollback();
			throw new JWTManagerException(e, Reason.PROCESSING_ERROR);
		} finally {
			session.close();
		}
	}

	@Override
	public void modifyJWT(Long id, Stage stage, Date dateExpires,
							   String initiatedBy, String comments, ActivityType activityType)
			throws JWTManagerException {
		Session session = sessionFactory.openSession();

		try {
			session.beginTransaction();
			@SuppressWarnings("unchecked")
			List<UserRequest> list = session.createCriteria(UserRequest.class)
					.add(Restrictions.eq("id", id)).list();
			if (list != null && list.size() > 0) {
				UserRequest userRequest = list.get(0);
				HistoryLog historyLog = new HistoryLog();
				historyLog.setComments(comments);
				historyLog.setInitiatedBy(initiatedBy);
				historyLog.setUserRequest(userRequest);
				historyLog.setActivityType(activityType);

				userRequest.setStage(stage);

				invalidateJWT(userRequest.getJWT());

				session.save(userRequest);
				session.save(historyLog);
				session.getTransaction().commit();

			} else {
				throw new Exception("No results found matching id = [" + id
						+ "]");
			}

		} catch (Exception e) {
			session.getTransaction().rollback();
			log.error(e);
			throw new JWTManagerException(e , Reason.PROCESSING_ERROR);
		} finally {
			session.close();
		}

	}

	@Override
	public BindaasUser lookupUser(String token) throws JWTManagerException {
		Session session = sessionFactory.openSession();
		try {

			if(verifyToken(token)){
				@SuppressWarnings("unchecked")
				List<UserRequest> listOfValidTokens = (List<UserRequest>) session.createCriteria(UserRequest.class).
						add(Restrictions.eq("stage",	Stage.accepted.name())).
						add(Restrictions.eq("jwt", token)).
						list();

				if(listOfValidTokens!=null && listOfValidTokens.size() > 0)
				{
					UserRequest request = listOfValidTokens.get(0);
					BindaasUser bindaasUser = new BindaasUser(request.getEmailAddress());
					bindaasUser.addProperty(BindaasUser.EMAIL_ADDRESS, request.getEmailAddress());
					bindaasUser.addProperty(BindaasUser.FIRST_NAME, request.getFirstName());
					bindaasUser.addProperty(BindaasUser.LAST_NAME, request.getLastName());
					return bindaasUser;
				}
			}

		} catch (Exception e) {
			log.error(e);
			throw new JWTManagerException(e, Reason.PROCESSING_ERROR);
		}

		return null;
	}

	// usage in trusted-app-client only
	@Override
	public List<UserRequest> listJWT() throws JWTManagerException {
		Session session = sessionFactory.openSession();

		try {
			@SuppressWarnings("unchecked")
			List<UserRequest> listOfValidTokens = (List<UserRequest>) session
					.createCriteria(UserRequest.class)
					.add(Restrictions.eq("stage", Stage.accepted.name()))
					.add(Restrictions.gt("dateExpires", new Date()))
					.add(Restrictions.isNotNull("jwt"))
					.list();
			return listOfValidTokens;
		} catch (Exception e) {
			log.error(e);
			throw new JWTManagerException(e , Reason.PROCESSING_ERROR);
		} finally {
			session.close();
		}
	}

	// usage in trusted-app-client only
	@Override
	public Integer revokeJWT(BindaasUser bindaasUser, String initiatedBy ,String comments , ActivityType activityType)
			throws JWTManagerException {
		Session session = sessionFactory.openSession();
		try{
			String emailAddress = bindaasUser
					.getProperty(BindaasUser.EMAIL_ADDRESS) != null ? bindaasUser
					.getProperty(BindaasUser.EMAIL_ADDRESS).toString()
					: bindaasUser.getName() + "@" + bindaasUser.getDomain();

			@SuppressWarnings("unchecked")
			List<UserRequest> listOfValidTokens = (List<UserRequest>) session.createCriteria(UserRequest.class).
					add(Restrictions.eq("stage", Stage.accepted.name())).
					add(Restrictions.eq("emailAddress", emailAddress)).
					add(Restrictions.isNotNull("jwt")).
					list();

			if(listOfValidTokens!=null && listOfValidTokens.size() > 0)
			{
				session.beginTransaction();
				HistoryLog historyLog = new HistoryLog();
				for(UserRequest usrRequest : listOfValidTokens)
				{
					usrRequest.setStage(Stage.revoked);
					invalidateJWT(usrRequest.getJWT());

					historyLog.setActivityType(activityType.toString());
					historyLog.setComments(comments);
					historyLog.setInitiatedBy(initiatedBy);
					historyLog.setUserRequest(usrRequest);
					session.save(historyLog);
				}
				session.getTransaction().commit();

				return listOfValidTokens.size();
			}
			else
				return 0;


		}catch(Exception e)
		{
			log.error(e);
			session.getTransaction().rollback();
			throw new JWTManagerException(e ,Reason.PROCESSING_ERROR);
		}
		finally{
			if(session!=null)
				session.close();
		}

	}

	// usage in trusted-app-client
	public Date getExpires(String token) {
		DecodedJWT jwt = JWT.decode(token);
		return jwt.getExpiresAt();
	}

	// usage in trusted-app-client
	public String getEmailAddress(Long id){
		Session session = sessionFactory.openSession();

		@SuppressWarnings("unchecked")
		List<UserRequest> userRequest = (List<UserRequest>) session.createCriteria(UserRequest.class).
				add(Restrictions.eq("id", id)).list();

		return userRequest.get(0).getEmailAddress();
	}


	public void init() throws Exception {

	}

}
