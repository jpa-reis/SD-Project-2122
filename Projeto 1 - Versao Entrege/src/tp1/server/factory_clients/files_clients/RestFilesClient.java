package tp1.server.factory_clients.files_clients;

import java.net.URI;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import tp1.api.service.rest.RestFiles;
import tp1.api.service.util.Files;
import tp1.api.service.util.Result;
import tp1.clients.RestClient;
import tp1.server.JavaFiles;
import tp1.api.service.util.Result.ErrorCode;
import java.util.logging.Logger;

public class RestFilesClient  extends RestClient implements Files{
    protected static Logger Log = Logger.getLogger(JavaFiles.class.getName());

    private URI serverURI;
    private boolean canRedirect;

    public RestFilesClient(URI serverURI, boolean canRedirect){
        super();
        this.serverURI = serverURI;
        this.canRedirect = canRedirect;
    }

	@Override
	public Result<Void> writeFile(String fileId, byte[] data, String token) {
        return reTry(() -> {
            WebTarget target = client.target(serverURI).path(RestFiles.PATH).path(fileId);
            Response r = target
                        .request()
                        .accept(MediaType.APPLICATION_JSON)
                        .post(Entity.entity(data, MediaType.APPLICATION_OCTET_STREAM));
            if(r.getStatus() == Status.OK.getStatusCode()){
                return Result.ok();   
            }
            else{
                return Result.error(getErrorCode(r.getStatus()));
            }
        });
        
	}

	@Override
	public Result<Void> deleteFile(String fileId, String token) {
		
        return reTry(() -> {
            WebTarget target = client.target(serverURI);
            Response r = target
                        .request()
                        .delete();
            if(r.getStatus() == Status.OK.getStatusCode()){
                return Result.ok();   
            }
            else{
                return Result.error(getErrorCode(r.getStatus()));
            }
        });

       
	}

	@Override
	public Result<byte[]> getFile(String fileId, String token) {
        if(canRedirect){
            throw new WebApplicationException(Response.temporaryRedirect(serverURI).build());      
        }
        else{
            WebTarget target = client.target(serverURI);
            Response r = target
                        .request()
                        .get();
            if(r.getStatus() == Status.OK.getStatusCode()){
                return Result.ok(r.readEntity(byte[].class));   
            }
            else{
                return Result.error(getErrorCode(r.getStatus()));
            }
        }
		
    
	}

    private ErrorCode getErrorCode(int code){
        switch(code){
            case 404: 
                return ErrorCode.NOT_FOUND;
            case 400:
                return ErrorCode.BAD_REQUEST;
            case 403:
                return ErrorCode.FORBIDDEN;
            default:
                return ErrorCode.CONFLICT;
        }
    }
}
