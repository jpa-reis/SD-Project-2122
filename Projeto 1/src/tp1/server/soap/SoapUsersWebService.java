package tp1.server.soap;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import jakarta.jws.WebService;
import tp1.api.User;
import tp1.api.service.soap.SoapUsers;
import tp1.api.service.soap.UsersException;

@WebService(serviceName=SoapUsers.NAME, targetNamespace=SoapUsers.NAMESPACE, endpointInterface=SoapUsers.INTERFACE)
public class SoapUsersWebService implements SoapUsers {

	private static final String CONFLICT = "Conflict";
	private static final String BAD_REQUEST = "Bad Request";
	private static final String NOT_FOUND = "Not found";
	private static final String FORBIDDEN = "Wrong password";

	static Logger Log = Logger.getLogger(SoapUsersWebService.class.getName());

	final protected Map<String, User> users = new HashMap<>();
	
	public SoapUsersWebService() {
	}

	@Override
	public String createUser(User user) throws UsersException {
		Log.info(String.format("SOAP createUser: user = %s\n", user));

		if( badUserData(user ))
			throw new UsersException(BAD_REQUEST);
		
		var userId = user.getUserId();
		var res = users.putIfAbsent(userId, user);
		
		if (res != null)
			throw new UsersException(CONFLICT);
		else {
			return userId;
		}
	}

	@Override
	public User getUser(String userId, String password) throws UsersException {
		Log.info("getUser : user = " + userId + "; pwd = " + password);
		
		User user = users.get(userId);

		// Check if user exists 
		if(user == null) {
			Log.info("User does not exist.");
			throw new UsersException(NOT_FOUND);
		}
		
		//Check if the password is correct
		if( !user.getPassword().equals(password)) {
			Log.info("Password is incorrect.");
			throw new UsersException(FORBIDDEN);
		}
					
		return user;
	}

	@Override
	public User updateUser(String userId, String password, User user) throws UsersException {
		Log.info("updateUser : user = " + userId + "; pwd = " + password);
		
		User original = users.get(userId);

		// Check if user exists 
		if(user == null) {
			Log.info("User does not exist.");
			throw new UsersException(NOT_FOUND);
		}
		
		//Check if the password is correct
		if( !user.getPassword().equals(password)) {
			Log.info("Password is incorrect.");
			throw new UsersException(FORBIDDEN);
		}
		
		if(user.getEmail() == null) user.setEmail(original.getEmail());
		if(user.getFullName() == null) user.setFullName(original.getFullName());
		if(user.getUserId() != original.getUserId()) user.setUserId(original.getUserId());
		if(user.getPassword() == null) user.setPassword(original.getPassword());
		users.replace(userId, user);

		return user;
	}

	@Override
	public User deleteUser(String userId, String password) throws UsersException {
		Log.info("deleteUser : user = " + userId + "; pwd = " + password);
		
		User user = users.get(userId);
		
		// Check if user exists 
		if( user == null ) {
			Log.info("User does not exist.");
			throw new UsersException(NOT_FOUND);
		}
		
		//Check if the password is correct
		if( !user.getPassword().equals( password)) {
			Log.info("Password is incorrect.");
			throw new UsersException(FORBIDDEN);
		}

		users.remove(userId);
		
		return user;
	}

	@Override
	public List<User> searchUsers(String pattern) throws UsersException {
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

	
	private boolean badUserData(User user) {
		if(user.getEmail() == null || user.getFullName() == null || user.getPassword() == null || user.getUserId() == null)
			return true;
		return false;
	}
	
}
