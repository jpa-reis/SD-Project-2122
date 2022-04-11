package tp1.server.resources;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import jakarta.inject.Singleton;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response.Status;
import tp1.api.User;
import tp1.api.service.rest.RestUsers;

@Singleton
public class UsersResource implements RestUsers {

	private final Map<String,User> users = new HashMap<>();

	private static Logger Log = Logger.getLogger(UsersResource.class.getName());
	
	public UsersResource() {
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
