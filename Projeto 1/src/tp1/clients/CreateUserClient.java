package tp1.clients;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;

import tp1.Discovery;
import tp1.api.User;

public class CreateUserClient{

	public static final int PORT = 8080;
	public static final String SERVICE = "UsersService";
	private static final String SERVER_URI_FMT = "http://%s:%s/rest";
	public static void main(String[] args) throws IOException {

		if( args.length != 4) {
			System.err.println( "Use: java sd2122.aula2.clients.CreateUserClient userId fullName email password");
			return;
		}
		
		String userId = args[0];
		String fullName = args[1];
		String email = args[2];
		String password = args[3];
		
		User u = new User(userId, fullName, email, password);


		String ip = InetAddress.getLocalHost().getHostAddress();
		String clientURI = String.format(SERVER_URI_FMT, ip, PORT);
		Discovery listener = new Discovery(Discovery.DISCOVERY_ADDR, SERVICE, clientURI);
		listener.listener();

		while(listener.knownUrisOf(SERVICE).length == 0){
				try {
					System.out.println("Searching for servers...");
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

		}

		URI[] userServiceURIS = listener.knownUrisOf(SERVICE);

		System.out.println("Sending request to server.");
		
		var result = new RestUsersClient(userServiceURIS[0]).createUser(u);
		System.out.println("Result: " + result);
	}
	
}
