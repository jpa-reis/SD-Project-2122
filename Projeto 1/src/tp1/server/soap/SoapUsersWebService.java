package tp1.server.soap;


import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.xml.namespace.QName;

import jakarta.jws.WebService;
import jakarta.xml.ws.Service;
import tp1.Discovery;
import tp1.api.User;
import tp1.api.service.soap.DirectoryException;
import tp1.api.service.soap.SoapDirectory;
import tp1.api.service.soap.SoapUsers;
import tp1.api.service.soap.UsersException;
import tp1.server.resources.RestClient;

@WebService(serviceName=SoapUsers.NAME, targetNamespace=SoapUsers.NAMESPACE, endpointInterface=SoapUsers.INTERFACE)
public class SoapUsersWebService extends RestClient implements SoapUsers {

	private static final String CONFLICT = "Conflict";
	private static final String BAD_REQUEST = "Bad Request";
	private static final String NOT_FOUND = "Not found";
	private static final String FORBIDDEN = "Wrong password";

	private final Discovery discoverySystem;
    private static final String SERVICE = "users";
    private static final String SERVER_URI_FMT = "http://%s:%s/soap";
    public static final int PORT = 8080;

	static Logger Log = Logger.getLogger(SoapUsersWebService.class.getName());

	final protected Map<String, User> users = new HashMap<>();
	
	public SoapUsersWebService() throws UnknownHostException {
		super(URI.create(String.format(SERVER_URI_FMT, InetAddress.getLocalHost().getHostAddress(), PORT)));
		/*Initialize discovery system code*/
     
        discoverySystem = new Discovery(Discovery.DISCOVERY_ADDR, SERVICE, serverURI.toString());
        discoverySystem.listener(); 
        discoverySystem.announce(SERVICE, serverURI.toString());
	
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

		URI[] dirServiceURIS = discoverySystem.knownUrisOf(SOAPDirServer.SERVICE_NAME);
        while(dirServiceURIS.length == 0){
        	dirServiceURIS = discoverySystem.knownUrisOf(SOAPDirServer.SERVICE_NAME);
		}

		QName qname = new QName(SoapDirectory.NAMESPACE, SoapDirectory.NAME);		
		Service service;
        try {
            service = Service.create( URI.create(dirServiceURIS[0]+ "?wsdl").toURL(), qname);
        } catch (MalformedURLException e1) {
            e1.printStackTrace();
            return null;
        }	

		SoapDirectory directory = service.getPort(tp1.api.service.soap.SoapDirectory.class);
		directory.deleteUserS(userId);
		
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
