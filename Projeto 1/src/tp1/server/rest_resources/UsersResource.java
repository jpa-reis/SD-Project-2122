package tp1.server.rest_resources;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import jakarta.inject.Singleton;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import tp1.Discovery;
import tp1.api.User;
import tp1.api.service.rest.RestDirectory;
import tp1.api.service.rest.RestUsers;
import tp1.clients.RestClient;
import tp1.server.rest.RESTDirServer;

@Singleton
public class UsersResource extends RestClient implements RestUsers {

	private final Map<String,User> users = new ConcurrentHashMap<>();

	private static Logger Log = Logger.getLogger(UsersResource.class.getName());
	
	/*Discovery system variables and constants*/
    private final Discovery discoverySystem;
    private static final String SERVICE = "users";
    private static final String SERVER_URI_FMT = "http://%s:%s/rest";
    public static final int PORT = 8080;
    /*----------------------------------------*/
 
	
	public UsersResource() throws UnknownHostException {
		super();

        /*Initialize discovery system code*/
		URI serverURI = URI.create(String.format(SERVER_URI_FMT, InetAddress.getLocalHost().getHostAddress(), PORT));
        discoverySystem = new Discovery(Discovery.DISCOVERY_ADDR, SERVICE, serverURI.toString());
        discoverySystem.listener(); 
        discoverySystem.announce(SERVICE, serverURI.toString());
	}
		
	@Override
	public String createUser(User user) {
		Log.info("createUser : " + user);
		
		// Check if user data is valid
		if(user.getUserId() == null || user.getPassword() == null || user.getFullName() == null || 
				user.getEmail() == null) {
			Log.info("User object invalid.");
			throw new WebApplicationException( Status.BAD_REQUEST );
		}
		
		// Check if userId already exists
		if( users.containsKey(user.getUserId())) {
			Log.info("User already exists.");
			throw new WebApplicationException( Status.CONFLICT );
		}

		//Add the user to the map of users
		users.put(user.getUserId(), user);
		return user.getUserId();
	}


	@Override
	public User getUser(String userId, String password) {
		Log.info("getUser : user = " + userId + "; pwd = " + password);
		
		User user = users.get(userId);
		
		// Check if user exists 
		if( user == null) {
			Log.info("User does not exist.");
			throw new WebApplicationException( Status.NOT_FOUND );
		}
		
		//Check if the password is correct
		if( !user.getPassword().equals(password)) {
			Log.info("Password is incorrect.");
			throw new WebApplicationException( Status.FORBIDDEN );
		}
					
		return user;
	}


	@Override
	public User updateUser(String userId, String password, User user) {
		Log.info("updateUser : user = " + userId + "; pwd = " + password + " ; user = " + user);

		User original = users.get(userId);

		//Check if there is a user with the provided userId
		if(users.get(userId) == null){
			Log.info("User does not exist.");
			throw new WebApplicationException(Status.NOT_FOUND);
		}
		//Check if the password if incorrect
		if(!password.equals(users.get(userId).getPassword())){
			Log.info("Password is incorrect.");
			throw new WebApplicationException(Status.FORBIDDEN);
		}
		if(user.getEmail() == null) user.setEmail(original.getEmail());
		if(user.getFullName() == null) user.setFullName(original.getFullName());
		if(user.getUserId() != original.getUserId()) user.setUserId(original.getUserId());
		if(user.getPassword() == null) user.setPassword(original.getPassword());
		users.replace(userId, user);

		return user;
	}


	@Override
	public User deleteUser(String userId, String password) {
		Log.info("deleteUser : user = " + userId + "; pwd = " + password);

		User user = users.get(userId);
		
		// Check if user exists 
		if( user == null ) {
			Log.info("User does not exist.");
			throw new WebApplicationException( Status.NOT_FOUND );
		}
		
		//Check if the password is correct
		if( !user.getPassword().equals( password)) {
			Log.info("Password is incorrect.");
			throw new WebApplicationException( Status.FORBIDDEN );
		}

		URI[] dirServiceURIS = discoverySystem.knownUrisOf(RESTDirServer.SERVICE);
        while(dirServiceURIS.length == 0){
            dirServiceURIS = discoverySystem.knownUrisOf(RESTDirServer.SERVICE);
        }
		WebTarget target = client.target(dirServiceURIS[0]).path(RestDirectory.PATH).path(userId);
        Response r = target
                    .request()
                    .delete();
		users.remove(userId);
		
		return user;
	}


	@Override
	public List<User> searchUsers(String pattern) {
		Log.info("searchUsers : pattern = " + pattern);

		List<User> listUsers = new ArrayList<User>();

		//Check if there are no users
		if(users.isEmpty()){
			return listUsers;
		}

		Set<String> userIds = users.keySet();

		// Check if the pattern is valid
		for (String userId : userIds){
			String name = users.get(userId).getFullName();
			if(name.toUpperCase().contains(pattern.toUpperCase())){
				User currentUser = users.get(userId);
				listUsers.add(new User(currentUser.getUserId(),currentUser.getFullName(),currentUser.getEmail(),""));
			}
					
		}
		return listUsers;
	}

}
