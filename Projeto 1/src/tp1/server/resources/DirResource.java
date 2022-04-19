package tp1.server.resources;
import java.io.File;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import jakarta.inject.Singleton;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import tp1.Discovery;
import tp1.api.FileInfo;
import tp1.api.User;
import tp1.api.service.rest.RestDirectory;
import tp1.api.service.rest.RestFiles;
import tp1.api.service.rest.RestUsers;
import tp1.server.RESTFilesServer;
import tp1.server.RESTUsersServer;
import jakarta.ws.rs.WebApplicationException;

@Singleton
public class DirResource extends RestClient implements RestDirectory{

    //Mapping of files to their owner and the people with access to the file
    //Pre-Condition: The key is a String concatenated as "userId/filename"
	
    private final Map<String, HashMap<String, FileInfo>> filesInfo = new HashMap<String, HashMap<String,FileInfo>>();
    private final HashMap<String, FileInfo> filesInfo2 = new HashMap<String,FileInfo>();
    /*Discovery system variables and constants*/
    private final Discovery discoverySystem;
    private static final String SERVICE = "directory";
    private static final String SERVER_URI_FMT = "http://%s:%s/rest";
    public static final int PORT = 8080;
    /*----------------------------------------*/
 
    
	private static Logger Log = Logger.getLogger(FilesResource.class.getName());

	public DirResource() throws UnknownHostException {
        super(URI.create(String.format(SERVER_URI_FMT, InetAddress.getLocalHost().getHostAddress(), PORT)));

        /*Initialize discovery system code*/
     
        discoverySystem = new Discovery(Discovery.DISCOVERY_ADDR, SERVICE, serverURI.toString());
        discoverySystem.listener(); 
        discoverySystem.announce(SERVICE, serverURI.toString());

   
	}

    @Override
    public FileInfo writeFile(String filename, byte[] data, String userId, String password) {
        Log.info("writeFile : " + filename);
        
        //Verify userId and password
        Response r = reTry( () -> clt_checkUser(userId, password));
        if(r.getStatus() == Status.NOT_FOUND.getStatusCode()){
            throw new WebApplicationException(Status.NOT_FOUND);
        }
        else if(r.getStatus() == Status.FORBIDDEN.getStatusCode()){
            throw new WebApplicationException(Status.FORBIDDEN);
        }

        //Try to add a file to the server
        FileInfo fileInfo = reTry( () -> clt_writeFile(userId, filename, data));
        String fileId = String.format("%s/%s", userId, filename);

        if(!filesInfo.containsKey(fileId)){
            filesInfo.put(userId, filesInfo2.put(fileId, fileInfo));
        }
        else{
            filesInfo.get(userId).replace(fileId, fileInfo);
        }

        return fileInfo;
    }
    

    @Override
    public void deleteFile(String filename, String userId, String password) {
       Log.info("deleteFile : " + filename);
        
        //Verify userId and password
        Response r = reTry( () -> clt_checkUser(userId, password));
        if(r.getStatus() == Status.NOT_FOUND.getStatusCode()){
            throw new WebApplicationException(Status.NOT_FOUND);
        }
        else if(r.getStatus() == Status.FORBIDDEN.getStatusCode()){
            throw new WebApplicationException(Status.FORBIDDEN);
        }
        
        String fileId = String.format("%s/%s", userId, filename);

        if(!filesInfo.get(userId).containsKey(fileId)){
            throw new WebApplicationException(Status.NOT_FOUND);
        }
       
	    filesInfo.get(userId).remove(fileId);
    }

    @Override
    public void shareFile(String filename, String userId, String userIdShare, String password) {

        //Check if userIdShare exists
        Response r = reTry( () -> clt_checkUser(userIdShare, ""));
        if(r.getStatus() == Status.NOT_FOUND.getStatusCode()){
            throw new WebApplicationException(Status.NOT_FOUND);
        }
        
        //Check if filename exists
        String fileId = String.format("%s/%s", userId, filename);

        if(!filesInfo.get(userId).containsKey(fileId)){
            throw new WebApplicationException(Status.NOT_FOUND);
        }

        //Verify userId and password
        r = reTry( () -> clt_checkUser(userId, password));
        if(r.getStatus() == Status.NOT_FOUND.getStatusCode()){
            throw new WebApplicationException(Status.NOT_FOUND);
        }
        else if(r.getStatus() == Status.FORBIDDEN.getStatusCode()){
            throw new WebApplicationException(Status.FORBIDDEN);
        }
        
        //If everything is correct then add to shared files
        filesInfo.get(userId).get(fileId).getSharedWith().add(userIdShare);
        
        
    }

    @Override
    public void unshareFile(String filename, String userId, String userIdShare, String password) {

        //Check if userIdShare exists
        Response r = reTry( () -> clt_checkUser(userIdShare, ""));
        if(r.getStatus() == Status.NOT_FOUND.getStatusCode()){
            throw new WebApplicationException(Status.NOT_FOUND);
        }
        
        //Check if filename exists
        String fileId = String.format("%s/%s", userId, filename);

        if(!filesInfo.get(userId).containsKey(fileId)){
            throw new WebApplicationException(Status.NOT_FOUND);
        }

        //Verify userId and password
        r = reTry( () -> clt_checkUser(userId, password));
        if(r.getStatus() == Status.NOT_FOUND.getStatusCode()){
            throw new WebApplicationException(Status.NOT_FOUND);
        }
        else if(r.getStatus() == Status.FORBIDDEN.getStatusCode()){
            throw new WebApplicationException(Status.FORBIDDEN);
        }
        //If everything is correct then remove from shared files

        if(!filesInfo.get(userId).get(fileId).getSharedWith().contains(userIdShare)){
            throw new WebApplicationException(Status.NOT_FOUND);
        }

        filesInfo.get(userId).get(fileId).getSharedWith().remove(userIdShare);
        
    }

    @Override
    public byte[] getFile(String filename, String userId, String accUserId, String password) {

        //Check if userIdShare exists
        Response r = reTry( () -> clt_checkUser(accUserId, password));
        if(r.getStatus() == Status.NOT_FOUND.getStatusCode()){
            throw new WebApplicationException(Status.NOT_FOUND);
        }
        else if(r.getStatus() == Status.FORBIDDEN.getStatusCode()){
            throw new WebApplicationException(Status.FORBIDDEN);
        }

        //Check if the userID exists AND if the password if correct
        r = reTry( () -> clt_checkUser(userId, ""));
        if(r.getStatus() == Status.NOT_FOUND.getStatusCode()){
            throw new WebApplicationException(Status.NOT_FOUND);
        }

        //Redirect request to File Server
        String fileId = String.format("%s/%s", userId, filename);
        if(!filesInfo.get(userId).containsKey(fileId)){
            throw new WebApplicationException(Status.NOT_FOUND);
        }
        
       
       if(!filesInfo.get(userId).get(fileId).getSharedWith().contains(accUserId) && !accUserId.equals(userId)){
            throw new WebApplicationException(Status.FORBIDDEN);
        }    
        
        throw new WebApplicationException(Response.temporaryRedirect(URI.create(filesInfo.get(fileId).getFileURL())).build());      
    }

    @Override
    public List<FileInfo> lsFile(String userId, String password) {

        //Check if userId exists
        Response r = reTry( () -> clt_checkUser(userId, password));
        if(r.getStatus() == Status.NOT_FOUND.getStatusCode()){
            throw new WebApplicationException(Status.NOT_FOUND);
        }
        else if(r.getStatus() == Status.FORBIDDEN.getStatusCode()){
            throw new WebApplicationException(Status.FORBIDDEN);
        }
        
        List<FileInfo> filesList = new ArrayList<FileInfo>();

        for(Map.Entry<String, FileInfo> entry : filesInfo2.entrySet()){
            if(filesInfo2.get(entry.getKey()).getSharedWith().contains(userId) ||
               entry.getKey().split("/")[0].equals(userId))
                filesList.add(entry.getValue());
        }

        return filesList;
    }
    

    /*Auxiliary methods*/
		
    private Response clt_checkUser(String userId, String password){
        URI[] userServiceURIS = discoverySystem.knownUrisOf(RESTUsersServer.SERVICE);
        while(userServiceURIS.length == 0){
            userServiceURIS = discoverySystem.knownUrisOf(RESTUsersServer.SERVICE);
        }
        

        WebTarget target = client.target(userServiceURIS[0]).path(RestUsers.PATH);
        Response r = target.path(userId)
				.queryParam(RestUsers.PASSWORD, password).request()
				.accept(MediaType.APPLICATION_JSON)
				.get();
        return r;
    }

   
    private FileInfo clt_writeFile(String userId, String filename, byte[] data){

        URI[] fileServiceURIS = discoverySystem.knownUrisOf(RESTFilesServer.SERVICE);
        while(fileServiceURIS.length == 0){
            fileServiceURIS = discoverySystem.knownUrisOf(RESTFilesServer.SERVICE);
        }

        String fileId = String.format("%s_%s", userId, filename);
        WebTarget target = client.target(fileServiceURIS[0]).path(RestFiles.PATH).path(fileId);
        Response r = target
                    .request()
                    .accept(MediaType.APPLICATION_JSON)
                    .post(Entity.entity(data, MediaType.APPLICATION_OCTET_STREAM));

        FileInfo fileInfo = new FileInfo(userId, filename, target.getUri().toString(), new HashSet<String>());
        
        return fileInfo;
    }
	

}
