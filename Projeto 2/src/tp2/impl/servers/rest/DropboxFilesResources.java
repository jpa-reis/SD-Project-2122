package tp2.impl.servers.rest;

import java.util.logging.Logger;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

import jakarta.inject.Singleton;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.Response;
import tp2.api.service.java.Result;
import tp2.api.service.rest.RestFiles;
import org.pac4j.scribe.builder.api.DropboxApi20;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.google.gson.Gson;
import tp2.impl.servers.rest.records.DeleteFileArgs;
import tp2.impl.servers.rest.records.GetFileArgs;
import tp2.impl.servers.rest.records.UploadFileArgs;

import static tp2.api.service.java.Result.error;
import static tp2.api.service.java.Result.ok;

@Singleton
public class DropboxFilesResources extends RestResource implements RestFiles {
	private static Logger Log = Logger.getLogger(DropboxFilesResources.class.getName());
	static final String DELIMITER = "$$$";
	//KEYS
	private static final String apiKey = "m8vaidvv3hknd8x";
	private static final String apiSecret = "669zf51aonq1hsq";
	private static final String accessTokenStr = "sl.BIwmITyTV-2673X9iVxL2Zgmfqet1WBLCsMnso84CoeoKDHIR22yKLpLj5b2W-Igmh62EKBHL3PTrYTBzmnjwSJ2EXnp42HT_kRA_z4WkCFPY2UOOtAaGZV8Qx2fYx7OYM9Y5JfVAssA";
	private static final String USER = "user";
	private static final String CREATE_FILE_URL = "https://content.dropboxapi.com/2/files/upload";
	private static final String DELETE_FILE_URL = "https://api.dropboxapi.com/2/files/delete_v2";
	private static final String GET_FILE_URL = "https://content.dropboxapi.com/2/files/download";
	private static final String DELETE_USER_FILES_URL = "https://api.dropboxapi.com/2/files/delete_v2";

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


	public DropboxFilesResources() {
		json = new Gson();
		accessToken = new OAuth2AccessToken(accessTokenStr);
		service = new ServiceBuilder(apiKey).apiSecret(apiSecret).build(DropboxApi20.INSTANCE);

	}

	@Override
	public void writeFile(String fileId, byte[] data, String token) {
		var uploadFile = new OAuthRequest(Verb.POST, CREATE_FILE_URL);
		uploadFile.addHeader(DROPBOX_API_ARG, json.toJson(new UploadFileArgs("/" + fileId.split(Pattern.quote(DELIMITER))[0] + "/" + fileId, "overwrite", false, false, false)));
		uploadFile.addHeader(CONTENT_TYPE_HDR, OCTET_CONTENT_TYPE);
		uploadFile.setPayload(data);
		service.signRequest(accessToken, uploadFile);
		try {
			super.resultOrThrow(toJavaResult(service.execute(uploadFile)));
		}
		catch (InterruptedException | ExecutionException | IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void deleteFile(String fileId, String token) {
		var deleteFile = new OAuthRequest(Verb.POST, DELETE_FILE_URL);
		deleteFile.addHeader(CONTENT_TYPE_HDR, JSON_CONTENT_TYPE);
		deleteFile.setPayload(json.toJson(new DeleteFileArgs("/" + fileId.split(Pattern.quote(DELIMITER))[0]  + "/" + fileId)));
		service.signRequest(accessToken, deleteFile);
		try {
			super.resultOrThrow(toJavaResult(service.execute(deleteFile)));
		} catch (InterruptedException | ExecutionException | IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public byte[] getFile(String fileId, String token) {
		var getFile = new OAuthRequest(Verb.GET, GET_FILE_URL);
		getFile.addHeader(DROPBOX_API_ARG, json.toJson(new GetFileArgs("/" + fileId.split(Pattern.quote(DELIMITER))[0]  + "/" + fileId)));
		service.signRequest(accessToken, getFile);
		try {
			com.github.scribejava.core.model.Response r = service.execute(getFile);
			super.resultOrThrowGetFiles(toJavaResult(r));
			return r.getStream().readAllBytes();
		} catch (InterruptedException | ExecutionException | IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public void deleteUserFiles(String userId, String token) {
		var deleteFile = new OAuthRequest(Verb.POST, DELETE_FILE_URL);
		deleteFile.addHeader(CONTENT_TYPE_HDR, JSON_CONTENT_TYPE);
		deleteFile.setPayload(json.toJson(new DeleteFileArgs("/" + userId)));
		service.signRequest(accessToken, deleteFile);
		try {
			super.resultOrThrow(toJavaResult(service.execute(deleteFile)));
		} catch (InterruptedException | ExecutionException | IOException e) {
			e.printStackTrace();
		}
	}

	private Result<Void> toJavaResult(com.github.scribejava.core.model.Response r) {
		try {
			var status = r.getCode();
			if (status == Response.Status.OK.getStatusCode())
				return ok();
			else
				return error(getErrorCodeFrom(status));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private Result<byte[]> toJavaResult(com.github.scribejava.core.model.Response r, GenericType<byte[]> gtype) {
		try {
			var status = r.getCode();
			if (status == Response.Status.OK.getStatusCode()){
				return  ok(r.getBody().getBytes());
			}
			else
				return error(getErrorCodeFrom(status));
		}  catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	static private Result.ErrorCode getErrorCodeFrom(int status) {
		return switch (status) {
			case 200, 209 -> Result.ErrorCode.OK;
			case 409 -> Result.ErrorCode.CONFLICT;
			case 403 -> Result.ErrorCode.FORBIDDEN;
			case 404 -> Result.ErrorCode.NOT_FOUND;
			case 400 -> Result.ErrorCode.BAD_REQUEST;
			case 500 -> Result.ErrorCode.INTERNAL_ERROR;
			case 501 -> Result.ErrorCode.NOT_IMPLEMENTED;
			default -> Result.ErrorCode.INTERNAL_ERROR;
		};
	}
}
