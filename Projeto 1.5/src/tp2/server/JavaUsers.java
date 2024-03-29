package tp2.server;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import tp2.Discovery;
import tp2.api.User;
import tp2.api.service.util.Result;
import tp2.api.service.util.Users;
import tp2.api.service.util.Result.ErrorCode;
import tp2.server.factory_clients.directory_clients.DirectoryClientFactory;
import tp2.server.soap_servers.SOAPDirServer;

public class JavaUsers implements Users{

    private final Map<String,User> users = new ConcurrentHashMap<>();
    
    /*Discovery system variables and constants*/
    private final Discovery discoverySystem;
    static final String SERVICE = "users";
    public static final int PORT = 8080;
    /*----------------------------------------*/
    public JavaUsers(){
        super();
        discoverySystem = new Discovery(Discovery.DISCOVERY_ADDR, SERVICE, "");
        discoverySystem.listener(); 
    }

    @Override
    public Result<String> createUser(User user) {
		
		// Check if user data is valid
		if(user.getUserId() == null || user.getPassword() == null || user.getFullName() == null || user.getEmail() == null) {
            return Result.error(ErrorCode.BAD_REQUEST);
		}
		
		// Check if userId already exists
		if( users.containsKey(user.getUserId())) {
            return Result.error(ErrorCode.CONFLICT);
		}

		//Add the user to the map of users
		users.put(user.getUserId(), user);
        return Result.ok( user.getUserId() );
    }

    @Override
    public Result<User> getUser(String userId, String password) {
		
		User user = users.get(userId);
		
		// Check if user exists 
        if( user == null) {
            return Result.error(ErrorCode.NOT_FOUND);
        }
		
		//Check if the password is correct
		if( !user.getPassword().equals(password)) {
            return Result.error(ErrorCode.FORBIDDEN);
		}
					
		return Result.ok( user );
    }

    @Override
    public Result<User> updateUser(String userId, String password, User user) {
		User original = users.get(userId);

		//Check if there is a user with the provided userId
		if(users.get(userId) == null){
            return Result.error(ErrorCode.NOT_FOUND);
		}
		//Check if the password if incorrect
		if(!password.equals(users.get(userId).getPassword())){
            return Result.error(ErrorCode.FORBIDDEN);
		}
		if(user.getEmail() == null) user.setEmail(original.getEmail());
		if(user.getFullName() == null) user.setFullName(original.getFullName());
		if(user.getUserId() != original.getUserId()) user.setUserId(original.getUserId());
		if(user.getPassword() == null) user.setPassword(original.getPassword());
		users.replace(userId, user);

		return Result.ok(user);
    }

    @Override
    public Result<User> deleteUser(String userId, String password) {
		
		User user = users.get(userId);
		
		// Check if user exists 
		if( user == null ) {
            return Result.error(ErrorCode.NOT_FOUND);
		}
		
		//Check if the password is correct
		if( !user.getPassword().equals( password)) {
            return Result.error(ErrorCode.FORBIDDEN);
		}

        URI[] dirServiceURIS = discoverySystem.knownUrisOf(SOAPDirServer.SERVICE_NAME);
        while(dirServiceURIS.length == 0){
            dirServiceURIS = discoverySystem.knownUrisOf(SOAPDirServer.SERVICE_NAME);
        }

		DirectoryClientFactory client = new DirectoryClientFactory();
        client.getClient(dirServiceURIS[0]).deleteUserReferences(userId);
		users.remove(userId);
		
		return Result.ok(user);
    }

    @Override
    public Result<List<User>> searchUsers(String pattern) {
		List<User> listUsers = new ArrayList<User>();

		// Check if the pattern is valid
		for (String userId : users.keySet()){
			String name = users.get(userId).getFullName();
			if(name.toUpperCase().contains(pattern.toUpperCase())){
				User currentUser = users.get(userId);
                User currentUserWithoutPassword = new User(currentUser.getUserId(),currentUser.getFullName(),currentUser.getEmail(),"");
				listUsers.add(currentUserWithoutPassword);
			}
					
		}
		return Result.ok(listUsers);
    }
    
}
