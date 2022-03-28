package sd2122.aula3.clients;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;

import sd2122.aula3.Discovery;

public class DeleteUserClient {

	public static final int PORT = 8080;
	public static final String SERVICE = "UsersService";
	private static final String SERVER_URI_FMT = "http://%s:%s/rest";

	public static void main(String[] args) throws IOException {
		
		if( args.length != 2) {
			System.err.println( "Use: java sd2122.aula2.clients.DeleteUserClient userId password");
			return;
		}
		
		String userId = args[0];
		String password = args[1];

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

		new RestUsersClient(userServiceURIS[0]).deleteUser(userId, password);
	}
	
}
