package tp1.server.rest_resources;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import jakarta.inject.Singleton;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import tp1.Discovery;
import tp1.api.FileInfo;
import tp1.api.service.rest.RestDirectory;
import tp1.api.service.rest.RestFiles;
import tp1.api.service.rest.RestUsers;
import tp1.clients.RestClient;
import tp1.server.rest.RESTFilesServer;
import tp1.server.rest.RESTUsersServer;
import jakarta.ws.rs.WebApplicationException;

@Singleton
public class DirResource extends RestClient implements RestDirectory{


    //Mapping of files to their owner and the people with access to the file
    //Pre-Condition: The key is a String concatenated as "userId/filename"
	
    private final Map<String, HashMap<String, FileInfo>> filesInfo = new ConcurrentHashMap<String, HashMap<String,FileInfo>>();
    private final Map<String, Integer> capacityOfFileServer = new ConcurrentHashMap<String, Integer>();
    /*Discovery system variables and constants*/
    private final Discovery discoverySystem;
    private static final String SERVICE = "directory";
    private static final String SERVER_URI_FMT = "http://%s:%s/rest";
    public static final int PORT = 8080;
    /*----------------------------------------*/

    private int numberOfFileServersDiscovered = 0;
    private String lastServer;
 
    
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
        if(r == null){
            throw new WebApplicationException(Status.BAD_REQUEST);
        }
        if(r.getStatus() == Status.NOT_FOUND.getStatusCode()){
            throw new WebApplicationException(Status.NOT_FOUND);
        }
        else if(r.getStatus() == Status.FORBIDDEN.getStatusCode()){
            throw new WebApplicationException(Status.FORBIDDEN);
        }

        lastServer = "null";
        //Try to add a file to the server
        FileInfo fileInfo = reTry( () -> clt_writeFile(userId, filename, data, false));
        if(fileInfo == null && numberOfFileServersDiscovered > 1){
            fileInfo = reTry( () -> clt_writeFile(userId, filename, data, true));
        }
        else if(fileInfo == null && numberOfFileServersDiscovered == 1){
            throw new WebApplicationException(Status.BAD_REQUEST);
        }

        if(!filesInfo.containsKey(userId)){
            filesInfo.put(userId, new HashMap<String, FileInfo>());
            filesInfo.get(userId).put(filename, fileInfo);
        }
        else if(!filesInfo.get(userId).containsKey(filename)){
            filesInfo.get(userId).put(filename, fileInfo);
        }
        else{
            filesInfo.get(userId).replace(filename, fileInfo);
        }

        return fileInfo;
    }
    

    @Override
    public void deleteFile(String filename, String userId, String password) {
       Log.info("deleteFile : " + filename);
        
        //Verify userId and password
        Response r = reTry( () -> clt_checkUser(userId, password));
        if(r == null){
            throw new WebApplicationException(Status.BAD_REQUEST);
        }
        if(r.getStatus() == Status.NOT_FOUND.getStatusCode()){
            throw new WebApplicationException(Status.NOT_FOUND);
        }
        else if(r.getStatus() == Status.FORBIDDEN.getStatusCode()){
            throw new WebApplicationException(Status.FORBIDDEN);
        }
        
        if(!filesInfo.containsKey(userId)){
            throw new WebApplicationException(Status.NOT_FOUND);
        }

        if(!filesInfo.get(userId).containsKey(filename)){
            throw new WebApplicationException(Status.NOT_FOUND);
        }

        reTry( () -> clt_deleteFile(userId, filename));

        String server = filesInfo.get(userId).get(filename).getFileURL().split("/files")[0];
        int oldCapacity = capacityOfFileServer.get(server);
        capacityOfFileServer.replace(server, oldCapacity - 1);

	    filesInfo.get(userId).remove(filename);
    }

    @Override
    public void shareFile(String filename, String userId, String userIdShare, String password) {

        //Check if userIdShare exists
        Response r = reTry( () -> clt_checkUser(userIdShare, ""));
        if(r == null){
            throw new WebApplicationException(Status.BAD_REQUEST);
        }
        if(r.getStatus() == Status.NOT_FOUND.getStatusCode()){
            throw new WebApplicationException(Status.NOT_FOUND);
        }
        
        //Verify userId and password
        r = reTry( () -> clt_checkUser(userId, password));
        if(r == null){
            throw new WebApplicationException(Status.BAD_REQUEST);
        }
        if(r.getStatus() == Status.NOT_FOUND.getStatusCode()){
            throw new WebApplicationException(Status.NOT_FOUND);
        }
        else if(r.getStatus() == Status.FORBIDDEN.getStatusCode()){
            throw new WebApplicationException(Status.FORBIDDEN);
        }
        
        //Check if filename exist
        if(!filesInfo.get(userId).containsKey(filename)){
            throw new WebApplicationException(Status.NOT_FOUND);
        }
        
        //If everything is correct then add to shared files
        filesInfo.get(userId).get(filename).getSharedWith().add(userIdShare);
        
        
    }

    @Override
    public void unshareFile(String filename, String userId, String userIdShare, String password) {

        //Check if userIdShare exists
        Response r = reTry( () -> clt_checkUser(userIdShare, ""));
        if(r == null){
            throw new WebApplicationException(Status.BAD_REQUEST);
        }
        if(r.getStatus() == Status.NOT_FOUND.getStatusCode()){
            throw new WebApplicationException(Status.NOT_FOUND);
        }
        
        
        //Verify userId and password
        r = reTry( () -> clt_checkUser(userId, password));
        if(r == null){
            throw new WebApplicationException(Status.BAD_REQUEST);
        }
        if(r.getStatus() == Status.NOT_FOUND.getStatusCode()){
            throw new WebApplicationException(Status.NOT_FOUND);
        }
        else if(r.getStatus() == Status.FORBIDDEN.getStatusCode()){
            throw new WebApplicationException(Status.FORBIDDEN);
        }
        
        //Check if filename exists
        if(!filesInfo.get(userId).containsKey(filename)){
            throw new WebApplicationException(Status.NOT_FOUND);
        }

        //If everything is correct then remove from shared files
        if(!filesInfo.get(userId).get(filename).getSharedWith().contains(userIdShare)){
            throw new WebApplicationException(Status.NOT_FOUND);
        }

        filesInfo.get(userId).get(filename).getSharedWith().remove(userIdShare);
        
    }

    @Override
    public byte[] getFile(String filename, String userId, String accUserId, String password) {

        //Check if userIdShare exists
        Response r = reTry( () -> clt_checkUser(accUserId, password));
        if(r == null){
            throw new WebApplicationException(Status.BAD_REQUEST);
        }
        if(r.getStatus() == Status.NOT_FOUND.getStatusCode()){
            throw new WebApplicationException(Status.NOT_FOUND);
        }
        else if(r.getStatus() == Status.FORBIDDEN.getStatusCode()){
            throw new WebApplicationException(Status.FORBIDDEN);
        }

        //Check if the userID exists AND if the password if correct
        r = reTry( () -> clt_checkUser(userId, ""));
        if(r == null){
            throw new WebApplicationException(Status.BAD_REQUEST);
        }
        if(r.getStatus() == Status.NOT_FOUND.getStatusCode()){
            throw new WebApplicationException(Status.NOT_FOUND);
        }

        //Redirect request to File Server
        if(!filesInfo.get(userId).containsKey(filename)){
            throw new WebApplicationException(Status.NOT_FOUND);
        }
        
       
       if(!filesInfo.get(userId).get(filename).getSharedWith().contains(accUserId) && !accUserId.equals(userId)){
            throw new WebApplicationException(Status.FORBIDDEN);
        }    
        
        throw new WebApplicationException(Response.temporaryRedirect(URI.create(filesInfo.get(userId).get(filename).getFileURL())).build());      
    }

    @Override
    public List<FileInfo> lsFile(String userId, String password) {

        //Check if userId exists
        Response r = reTry( () -> clt_checkUser(userId, password));
        if(r == null){
            throw new WebApplicationException(Status.BAD_REQUEST);
        }
        if(r.getStatus() == Status.NOT_FOUND.getStatusCode()){
            throw new WebApplicationException(Status.NOT_FOUND);
        }
        else if(r.getStatus() == Status.FORBIDDEN.getStatusCode()){
            throw new WebApplicationException(Status.FORBIDDEN);
        }
        
        List<FileInfo> filesList = new ArrayList<FileInfo>();

        for(Map.Entry<String, HashMap<String, FileInfo>> entry : filesInfo.entrySet()){
            for(Map.Entry<String, FileInfo> entry2: filesInfo.get(entry.getKey()).entrySet()){
                if(entry.getKey().equals(userId) || entry2.getValue().getSharedWith().contains(userId)){
                    filesList.add(entry2.getValue());
                }
                    
            }
        }       
                
        

        return filesList;
    }
    
    @Override
	public void deleteUser(String userId) {

        for(Map.Entry<String, FileInfo> entry: filesInfo.get(userId).entrySet()){
           reTry( () -> clt_deleteFile(userId, entry.getKey()));
        }

        if(filesInfo.containsKey(userId))
            filesInfo.remove(userId);
		
        for(Map.Entry<String, HashMap<String, FileInfo>> entry : filesInfo.entrySet()){
            for(Map.Entry<String, FileInfo> entry2: filesInfo.get(entry.getKey()).entrySet()){
                if(entry2.getValue().getSharedWith().contains(userId)){
                    entry2.getValue().getSharedWith().remove(userId);
                }               
            }
        } 
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

   
    private FileInfo clt_writeFile(String userId, String filename, byte[] data, boolean failed){

        URI[] fileServiceURIS = discoverySystem.knownUrisOf(RESTFilesServer.SERVICE);
        while(fileServiceURIS.length == 0){
            fileServiceURIS = discoverySystem.knownUrisOf(RESTFilesServer.SERVICE);
        }

        numberOfFileServersDiscovered = fileServiceURIS.length;

        String minServer = fileServiceURIS[0].toString();

        if(failed && fileServiceURIS.length > 1){
            minServer = fileServiceURIS[1].toString();
            int oldCapacity = capacityOfFileServer.get(lastServer)-1;
            capacityOfFileServer.replace(lastServer, oldCapacity);
        }
        
        if(fileServiceURIS.length > 1){
            for(int i = 0; i < fileServiceURIS.length; i++){
                if(!lastServer.equals(fileServiceURIS[i].toString())){
                    if(!capacityOfFileServer.containsKey(fileServiceURIS[i].toString())){
                        capacityOfFileServer.put(fileServiceURIS[i].toString(), 0);
                    }
                    if(!lastServer.equals(minServer) && capacityOfFileServer.get(minServer) > capacityOfFileServer.get(fileServiceURIS[i].toString())){
                        minServer = fileServiceURIS[i].toString();
                    }else if(lastServer.equals(minServer)){
                        minServer = fileServiceURIS[i].toString();
                    }
                }
            }
        }else{
            if(!capacityOfFileServer.containsKey(fileServiceURIS[0].toString())){
                capacityOfFileServer.put(fileServiceURIS[0].toString(), 0);
            }
        }

        lastServer = minServer;

        String fileId = String.format("%s_%s", userId, filename);
        
        WebTarget target = client.target(minServer).path(RestFiles.PATH).path(fileId);
        Response r = target
                    .request()
                    .accept(MediaType.APPLICATION_JSON)
                    .post(Entity.entity(data, MediaType.APPLICATION_OCTET_STREAM));

        Set<String> sharedWith;
        if(!filesInfo.containsKey(userId) || !filesInfo.get(userId).containsKey(filename) || filesInfo.get(userId).get(filename).getSharedWith().isEmpty())
            sharedWith = new HashSet<String>();
        else
            sharedWith = filesInfo.get(userId).get(filename).getSharedWith();

        FileInfo fileInfo = new FileInfo(userId, filename, target.getUri().toString(), sharedWith);
        
        int newCapacity = capacityOfFileServer.get(minServer)+1;
        capacityOfFileServer.replace(minServer, newCapacity);
        
        return fileInfo;

    }

	 private FileInfo clt_deleteFile(String userId, String filename){

        WebTarget target = client.target(filesInfo.get(userId).get(filename).getFileURL());
        Response r = target
                    .request()
                    .delete();
                    
        return null;
    }

}