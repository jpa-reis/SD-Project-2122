package sd2122.aula3.clients;

import java.net.URI;
import java.util.List;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import sd2122.aula3.api.User;
import sd2122.aula3.api.service.RestUsers;

public class RestUsersClient extends RestClient implements RestUsers {

	final WebTarget target;
	
	RestUsersClient( URI serverURI ) {
		super( serverURI );
		target = client.target( serverURI ).path( RestUsers.PATH );
	}
	
	@Override
	public String createUser(User user) {
		return super.reTry( () -> {
			return clt_createUser( user );
		});
	}

	@Override
	public User getUser(String userId, String password) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public User updateUser(String userId, String password, User user) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public User deleteUser(String userId, String password) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<User> searchUsers(String pattern) {
		return super.reTry( () -> clt_searchUsers( pattern ));
	}


	private String clt_createUser( User user) {
		
		Response r = target.request()
				.accept(MediaType.APPLICATION_JSON)
				.post(Entity.entity(user, MediaType.APPLICATION_JSON));

		if( r.getStatus() == Status.OK.getStatusCode() && r.hasEntity() )
			return r.readEntity(String.class);
		else 
			System.out.println("Error, HTTP error status: " + r.getStatus() );
		
		return null;
	}
	
	private List<User> clt_searchUsers(String pattern) {
		Response r = target
				.queryParam(QUERY, pattern)
				.request()
				.accept(MediaType.APPLICATION_JSON)
				.get();

		if( r.getStatus() == Status.OK.getStatusCode() && r.hasEntity() )
			return r.readEntity(new GenericType<List<User>>() {});
		else 
			System.out.println("Error, HTTP error status: " + r.getStatus() );
		
		return null;
	}
}
