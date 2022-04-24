package tp1.server.interoperable.clients.directory_clients;

import java.net.URI;
import java.util.List;

import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;
import tp1.api.FileInfo;
import tp1.api.service.rest.RestDirectory;
import tp1.api.service.util.Directory;
import tp1.api.service.util.Result;
import tp1.clients.RestClient;

public class RestDirectoryClient extends RestClient implements Directory{
    
    private URI serverURI;

    public RestDirectoryClient(URI serverURI){
        super();
        this.serverURI = serverURI;
    }

	@Override
	public Result<FileInfo> writeFile(String filename, byte[] data, String userId, String password) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Result<Void> deleteFile(String filename, String userId, String password) {	
        // TODO Auto-generated method stub
        return null;
	}
	@Override
	public Result<Void> shareFile(String filename, String userId, String userIdShare, String password) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Result<Void> unshareFile(String filename, String userId, String userIdShare, String password) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Result<byte[]> getFile(String filename, String userId, String accUserId, String password) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Result<List<FileInfo>> lsFile(String userId, String password) {
		// TODO Auto-generated method stub
		return null;
	}

    @Override
    public Result<Void> deleteUserS(String userId) {
		reTry(() -> {
			WebTarget target = client.target(serverURI).path(RestDirectory.PATH).path(userId);
			Response r = target
						.request()
						.delete();
			
			return null;
		});
		return Result.ok();
    }
}
