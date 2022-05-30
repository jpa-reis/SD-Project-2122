package tp2.server.factory_clients.files_clients;

import java.io.IOException;
import java.net.URI;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import com.github.scribejava.core.model.Response;
import tp2.api.service.rest.RestFiles;
import tp2.api.service.util.Files;
import tp2.api.service.util.Result;
import tp2.api.service.util.Result.ErrorCode;
import tp2.clients.RestClient;
import tp2.server.JavaFiles;

import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.google.gson.Gson;

import org.pac4j.scribe.builder.api.DropboxApi20;

public class RestFilesClient  extends RestClient implements Files{
    protected static Logger Log = Logger.getLogger(JavaFiles.class.getName());

    private URI serverURI;
    private boolean canRedirect;

    //KEYS
    private static final String apiKey = "m8vaidvv3hknd8x";
	private static final String apiSecret = "669zf51aonq1hsq";
	private static final String accessTokenStr = "sl.BHS5_9QqgPtVDk-rk0FAsmoHVRsqmY72vrpys3s6P4AQFjQtohOeS0E8eI5GWCZ2U1GFbn7jENRKNxa7PmgNsnh2R04ISXzhXQBTBqQZ1L2sIe1zEdTUj1ntysXb_Sou5nvNRBA";
	
    //REQUESTS
	private static final String CREATE_FILE_URL = "https://content.dropboxapi.com/2/files/upload";
    private static final String DELETE_FILE_URL = "https://api.dropboxapi.com/2/files/delete_v2";
    private static final String GET_FILE_URL = "https://api.dropboxapi.com/2/files/get_metadata";
	
    //HTTP_CODES
    private static final int HTTP_SUCCESS = 200;

    //Enconding
	private static final String CONTENT_TYPE_HDR = "Content-Type";
	private static final String JSON_CONTENT_TYPE = "application/json; charset=utf-8";


    private final Gson json;
	private final OAuth2AccessToken accessToken;
    OAuth20Service service;
    
    public RestFilesClient(URI serverURI, boolean canRedirect){
        json = new Gson();
		accessToken = new OAuth2AccessToken(accessTokenStr);
        service = new ServiceBuilder(apiKey).apiSecret(apiSecret).build(DropboxApi20.INSTANCE);
    }

	@Override
	public Result<Void> writeFile(String fileId, byte[] data, String token) {
        return reTry(() -> {
            var uploadFile = new OAuthRequest(Verb.POST, CREATE_FILE_URL);
            uploadFile.addHeader(CONTENT_TYPE_HDR, JSON_CONTENT_TYPE);

            uploadFile.setPayload(json.toJson(new UploadFileArgs("/teste", "add", false, true, false, data)));
            service.signRequest(accessToken, uploadFile);

            Response r;
            try {
                r = service.execute(uploadFile);
                if(r.getCode()  == HTTP_SUCCESS){
                    return Result.ok();
                }
                else{
                    return Result.error(getErrorCode(r.getCode()));
                }
            } catch (InterruptedException | ExecutionException | IOException e) {
                e.printStackTrace();
            }
            return null;
        });
        
	}

	@Override
	public Result<Void> deleteFile(String fileId, String token) {
		
        return reTry(() -> {
            var deleteFile = new OAuthRequest(Verb.DELETE, DELETE_FILE_URL);
            deleteFile.addHeader(CONTENT_TYPE_HDR, JSON_CONTENT_TYPE);

            deleteFile.setPayload(json.toJson(new DeleteFileArgs("/" + fileId)));

            service.signRequest(accessToken, deleteFile);

            Response r;
            try {
                r = service.execute(deleteFile);
                if(r.getCode()  == HTTP_SUCCESS){
                    return Result.ok();
                }
                else{
                    return Result.error(getErrorCode(r.getCode()));
                    
                }
            } catch (InterruptedException | ExecutionException | IOException e) {
                e.printStackTrace();
            }
            return null;
        });

       
	}

	@Override
	public Result<byte[]> getFile(String fileId, String token) {
        return reTry(() -> {
            var getFile = new OAuthRequest(Verb.GET, GET_FILE_URL);
            getFile.addHeader(CONTENT_TYPE_HDR, JSON_CONTENT_TYPE);

            getFile.setPayload(json.toJson(new GetFileArgs("/" + fileId, false, false, false)));

            service.signRequest(accessToken, getFile);

            Response r;
            try {
                r = service.execute(getFile);
                if(r.getCode()  == HTTP_SUCCESS){
                    
                    return Result.ok();
                }
                else{
                    Log.info("" + r.getCode());
                    return Result.error(getErrorCode(r.getCode()));
                }
            } catch (InterruptedException | ExecutionException | IOException e) {
                e.printStackTrace();
            }
            return null;
        });
		
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
