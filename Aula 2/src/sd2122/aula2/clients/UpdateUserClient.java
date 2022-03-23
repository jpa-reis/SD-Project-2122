package sd2122.aula2.clients;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;

import org.glassfish.jersey.client.ClientConfig;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import sd2122.aula2.Discovery;
import sd2122.aula2.api.User;
import sd2122.aula2.api.service.RestUsers;

public class UpdateUserClient {

	public static final int PORT = 8080;
	public static final String SERVICE = "UsersService";
	private static final String SERVER_URI_FMT = "http://%s:%s/rest";

	public static void main(String[] args) throws IOException {
		
		if( args.length != 5) {
			System.err.println( "Use: java sd2122.aula2.clients.UpdateUserClient userId oldpwd fullName email password");
			return;
		}

		String userId = args[0];
		String oldpwd = args[1];
		String fullName = args[2];
		String email = args[3];
		String password = args[4];
		
		User u = new User( userId, fullName, email, password);

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
		
		ClientConfig config = new ClientConfig();
		Client client = ClientBuilder.newClient(config);
		
		WebTarget target = client.target(userServiceURIS[0]).path( RestUsers.PATH );

		Response r = target.path(userId)
				.queryParam(RestUsers.PASSWORD, oldpwd)
				.request()
				.accept(MediaType.APPLICATION_JSON)
				.put(Entity.entity(u, MediaType.APPLICATION_JSON));
		
				if( r.getStatus() == Status.OK.getStatusCode() && r.hasEntity() )
				System.out.println("Success, user was updated");
			else
				System.out.println("Error, HTTP error status: " + r.getStatus() );
			System.exit(0);
	}
	
}
