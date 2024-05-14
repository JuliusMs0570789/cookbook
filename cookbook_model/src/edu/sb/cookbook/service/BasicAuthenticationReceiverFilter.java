package edu.sb.cookbook.service;

import static javax.ws.rs.core.HttpHeaders.WWW_AUTHENTICATE;
import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;
// import java.util.Base64;
import javax.annotation.Priority;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import edu.sb.cookbook.persistence.Person;
import edu.sb.tool.Copyright;
import edu.sb.tool.HashCodes;


/**
 * JAX-RS filter provider that performs HTTP "basic" authentication on any REST service request. This aspect-oriented
 * design swaps "Authorization" headers for "Requester-Identity" during authentication.
 */
@Provider
@Priority(Priorities.AUTHENTICATION)
@Copyright(year = 2017, holders = "Sascha Baumeister")
public class BasicAuthenticationReceiverFilter implements ContainerRequestFilter {

	/**
	 * HTTP request header for the authenticated requester's identity.
	 */
	static public final String REQUESTER_IDENTITY = "X-Requester-Identity";
	
	 @PersistenceContext
	    private EntityManager entityManager;


	/**
	 * Performs HTTP "basic" authentication by calculating a password hash from the password contained in the request's
	 * "Authorization" header, and comparing it to the one stored in the person matching said header's username. The
	 * "Authorization" header is consumed in any case, and upon success replaced by a new "Requester-Identity" header that
	 * contains the authenticated person's identity. The filter chain is aborted in case of a problem.
	 * @param requestContext {@inheritDoc}
	 * @throws NullPointerException if the given argument is {@code null}
	 * @throws ClientErrorException (400) if the "Authorization" header is malformed, or if there is a pre-existing
	 *         "Requester-Identity" header
	 */
	public void filter (final ContainerRequestContext requestContext) throws NullPointerException, ClientErrorException {
		// - Throw a ClientErrorException(Status.BAD_REQUEST) if the given context's headers map already contains a
		//   "Requester-Identity" key, in order to prevent spoofing attacks.
		if (requestContext.getHeaders().containsKey(REQUESTER_IDENTITY)) {
			throw new ClientErrorException(UNAUTHORIZED);
		}

		// - Remove the "Authorization" header from said map and store the first of it's values in a variable
		//   "textCredentials", or null if the header value is either null or empty.
		String authHeader = requestContext.getHeaderString("Authorization");
		if (authHeader == null || authHeader.isEmpty()) {
			throw new ClientErrorException(UNAUTHORIZED);
		}

		// - if the "textCredentials" variable is not null, parse it programmatically using Base64.getDecoder().decode(),
		//   use the resulting byte array to create a new String instance, and store the resulting <email>:<password>
		//   combination in variable "credentials". 
		String[] authParts = authHeader.split(" ");
		if (authParts.length != 2 || !authParts[0].equalsIgnoreCase("Basic")) {
			throw new ClientErrorException(UNAUTHORIZED);
		}
		String credentials = new String(java.util.Base64.getDecoder().decode(authParts[1]));
		String[] parts = credentials.split(":");
		if (parts.length != 2) {
			throw new ClientErrorException(UNAUTHORIZED);
		}
		String email = parts[0];
		String password = parts[1];

		// - Perform the PQL-Query "select p from Person as p where p.email = :email"), using the credentials email
		//   address part. Note that this query will go to the second level cache before hitting the database if the
		//   Person#email field is annotated using @CacheIndex(updateable = true)! 
		TypedQuery<Person> query = entityManager.createQuery("SELECT p FROM Person p WHERE p.email = :email", Person.class);
		query.setParameter("email", email);

		try {
		    Person person = query.getSingleResult();
            
    		// - if the resulting people list contains exactly one element, calculate the hex-string representation
    		//   (i.e. 2 digits per byte) of the SHA2-256 hash code of the credential's password part using
    		//   HashCodes.sha2HashText(256, text).
            String hashedPassword = HashCodes.sha2HashText(256, password);

    		// - if this hash representation is equal to queried person's password hash, add a new "Requester-Identity"
    		//   header to the request headers, using the person's identity (converted to String) as value, and return
    		//   from this method.
            if (hashedPassword.equals(person.getPasswordHash())) {
                // Authentication successful, add Requester-Identity header
                requestContext.getHeaders().add(REQUESTER_IDENTITY, Long.toString(person.getIdentity()));
                return;
            }
        } catch (NoResultException e) {
            // Person not found
        }

    	// - in all other cases, abort the request using requestContext.abortWith() in order to challenging the client
		//   to provide HTTP Basic credentials (i.e. status code 401, and "WWW-Authenticate" header value "Basic").
		//   Note that the alternative of throwing NotAuthorizedException("Basic") comes with the disadvantage that
		//   failed authentication attempts clutter the server log with stack traces.
        requestContext.abortWith(Response.status(UNAUTHORIZED).header(WWW_AUTHENTICATE, "Basic").build());
		// TODO:
	
		
		


	
	}
}