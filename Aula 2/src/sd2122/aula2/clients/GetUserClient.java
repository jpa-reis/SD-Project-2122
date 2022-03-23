package sd2122.aula2.clients;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import org.glassfish.jersey.client.ClientConfig;

import sd2122.aula2.Discovery;
import sd2122.aula2.api.User;
import sd2122.aula2.api.service.RestUsers;

public class GetUserClient {

	public static final int PORT = 8080;
	public static final String SERVICE = "UsersService";
	private static final String SERVER_URI_FMT = "http://%s:%s/rest";

	public static void main(String[] args) throws IOException {
		
		if( args.length != 2) {
			System.err.println( "Use: java sd2122.aula2.clients.GetUserClient userId password");
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
		
		ClientConfig config = new ClientConfig();
		Client client = ClientBuilder.newClient(config);
		
		WebTarget target = client.target(userServiceURIS[0]).path( RestUsers.PATH );
		
		Response r = target.path( userId )
				.queryParam(RestUsers.PASSWORD, password).request()
				.accept(MediaType.APPLICATION_JSON)
				.get();

		if( r.getStatus() == Status.OK.getStatusCode() && r.hasEntity() ) {
			System.out.println("Success:");
			User u = r.readEntity(User.class);
			System.out.println( "User : " + u);
		} else
			System.out.println("Error, HTTP error status: " + r.getStatus() );
		System.exit(0);
	}
	
}
