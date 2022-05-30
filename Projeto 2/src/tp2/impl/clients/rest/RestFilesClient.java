package tp2.impl.clients.rest;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.ExecutionException;

import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.google.gson.Gson;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.pac4j.scribe.builder.api.DropboxApi20;
import tp2.api.service.java.Files;
import tp2.api.service.java.Result;
import tp2.api.service.rest.RestFiles;



public class RestFilesClient extends RestClient implements Files {

	//KEYS
	private static final String apiKey = "m8vaidvv3hknd8x";
	private static final String apiSecret = "669zf51aonq1hsq";
	private static final String accessTokenStr = "sl.BInB8OteOq2lENAHX8e-1DHRnLcPTD6GDH59mF82GEeo76TzBB9uy4vMWbTyRgTkx0WDY42k11DcAuVx1cTO-lymUUCIFpvfypWLs96pNW2ynBnkf9jxE1_FDd0MvzNQB_49qSE";
	private static final String USER = "user";
	private static final String CREATE_FILE_URL = "https://content.dropboxapi.com/2/files/upload";
	private static final String DELETE_FILE_URL = "https://api.dropboxapi.com/2/files/delete_v2";
	private static final String GET_FILE_URL = "https://content.dropboxapi.com/2/files/download";

	//HTTP_CODES
	private static final int HTTP_SUCCESS = 200;

	//Enconding
	private static final String DROPBOX_API_ARG = "Dropbox-API-Arg";
	private static final String CONTENT_TYPE_HDR = "Content-Type";
	private static final String JSON_CONTENT_TYPE = "application/json; charset=utf-8";
	private static final String OCTET_CONTENT_TYPE = "application/octet-stream";

	private final Gson json;
	private final OAuth2AccessToken accessToken;
	OAuth20Service service;

	public RestFilesClient(URI serverUri) {
		super(serverUri, RestFiles.PATH);
		json = new Gson();
		accessToken = new OAuth2AccessToken(accessTokenStr);
		service = new ServiceBuilder(apiKey).apiSecret(apiSecret).build(DropboxApi20.INSTANCE);
	}
	
	@Override
	public Result<byte[]> getFile(String fileId, String token) {
		var getFile = new OAuthRequest(Verb.GET, GET_FILE_URL);
		getFile.addHeader(CONTENT_TYPE_HDR, JSON_CONTENT_TYPE);
		getFile.addHeader(DROPBOX_API_ARG, json.toJson(new GetFileArgs("/" + fileId)));
		service.signRequest(accessToken, getFile);
		try {
			return super.toJavaResult(service.execute(getFile), new GenericType<byte[]>() {});
		} catch (InterruptedException | ExecutionException | IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public Result<Void> deleteFile(String fileId, String token) {
		var deleteFile = new OAuthRequest(Verb.DELETE, DELETE_FILE_URL);
		deleteFile.addHeader(CONTENT_TYPE_HDR, JSON_CONTENT_TYPE);
		deleteFile.setPayload(json.toJson(new DeleteFileArgs("/" + fileId)));
		service.signRequest(accessToken, deleteFile);
		try {
			return super.toJavaResult(service.execute(deleteFile));
		} catch (InterruptedException | ExecutionException | IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public Result<Void> writeFile(String fileId, byte[] data, String token) {
		var uploadFile = new OAuthRequest(Verb.POST, CREATE_FILE_URL);
		uploadFile.addHeader(DROPBOX_API_ARG, json.toJson(new UploadFileArgs("/" + fileId, "add", false, false, false)));
		uploadFile.addHeader(CONTENT_TYPE_HDR, OCTET_CONTENT_TYPE);
		uploadFile.setPayload(data);
		service.signRequest(accessToken, uploadFile);
		try {
			return super.toJavaResult(service.execute(uploadFile));
		}
		catch (InterruptedException | ExecutionException | IOException e) {
			e.printStackTrace();
			return  null;
		}
	}

	@Override
	public Result<Void> deleteUserFiles(String userId, String token) {
		Response r = target.path(USER)
				.path(userId)
				.queryParam(RestFiles.TOKEN, token)
				.request()
				.delete();
		
		return super.toJavaResult(r);
	}	
}
