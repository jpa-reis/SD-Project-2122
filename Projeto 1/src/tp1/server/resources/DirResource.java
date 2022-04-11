package tp1.server.resources;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import jakarta.inject.Singleton;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import tp1.api.FileInfo;
import tp1.api.service.rest.RestDirectory;
import tp1.api.service.rest.RestUsers;
import jakarta.ws.rs.client.Entity;

@Singleton
public class DirResource implements RestDirectory{

	private final Map<String, ArrayList<String>> userToFilesMapping = new HashMap<>();

	private static Logger Log = Logger.getLogger(FilesResource.class.getName());

	public DirResource() {
	}

    @Override
    public FileInfo writeFile(String filename, byte[] data, String userId, String password) {
        Log.info("writeFile : " + filename);
        
        WebTarget target = client.target("").patch(RestDirectory.PATH);

        //Verify password with userID
        Response r = target.path( userId )
				.queryParam(RestUsers.PASSWORD, password).request()
				.accept(MediaType.APPLICATION_JSON)
				.get();
        //Write in a file server 

        return null;
    }
    

}

    @Override
    public void deleteFile(String filename, String userId, String password) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void shareFile(String filename, String userId, String userIdShare, String password) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void unshareFile(String filename, String userId, String userIdShare, String password) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public byte[] getFile(String filename, String userId, String accUserId, String password) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<FileInfo> lsFile(String userId, String password) {
        // TODO Auto-generated method stub
        return null;
    }
		
	

}