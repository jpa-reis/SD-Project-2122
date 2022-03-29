package tp1.server.files;

import java.net.URI;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import tp1.api.service.rest.RestFiles;

public class RestFileClient extends RestFile implements RestFiles {

	final WebTarget target;
	
	RestFileClient( URI serverURI ) {
		super( serverURI );
		target = client.target( serverURI ).path( RestFiles.PATH );
	}

	@Override
	public void writeFile(String fileId, byte[] data, String token) {
		super.reTry( () -> {
			return clt_writeFile(fileId, data, token);
		});
	}

	@Override
	public void deleteFile(String fileId, String token) {
		super.reTry( () -> {
			return clt_deleteFile(fileId, token);
		});
	}

	@Override
	public byte[] getFile(String fileId, String token) {
		return super.reTry( () -> {
			return clt_getFile(fileId, token);
		});
	}

	private String clt_writeFile(String fileId, byte[] data, String token) {
		Response r = target.request()
				.accept(MediaType.APPLICATION_JSON)
				.post(Entity.entity(fileId, MediaType.APPLICATION_JSON));

		if( r.getStatus() == Status.OK.getStatusCode() && r.hasEntity() )
			return r.readEntity(String.class);
		else 
			System.out.println("Error, HTTP error status: " + r.getStatus() );
		
		return null;
	}
	
	private String clt_deleteFile(String fileId, String token) {
		Response r = target.path( fileId )
				.queryParam(token).request()
				.accept(MediaType.APPLICATION_JSON)
				.delete();

		if( r.getStatus() == Status.OK.getStatusCode() && r.hasEntity() ) {
			System.out.println("Success:");
		} else
			System.out.println("Error, HTTP error status: " + r.getStatus() );	
		return null;
	}

	private byte[] clt_getFile(String fileId, String token) {
		Response r = target.path( fileId )
				.queryParam(token).request()
				.accept(MediaType.APPLICATION_JSON)
				.get();

		if( r.getStatus() == Status.OK.getStatusCode() && r.hasEntity() ) {
			System.out.println("Success:");
		} else
			System.out.println("Error, HTTP error status: " + r.getStatus() );
		return null;
	}

}
