package tp1.server.resources;
import java.io.File;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import jakarta.inject.Singleton;
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

    //Maps every user that has files to their files
	private final Map<String, ArrayList<String>> userToFilesMapping = new HashMap<>();

    //Maps every user that has shared files to the files they have access to
    private final Map<String, ArrayList<String>> userToAccessedFilesMapping = new HashMap<>();

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

    //Palma
    @Override
    public FileInfo writeFile(String filename, byte[] data, String userId, String password) {
        Log.info("writeFile : " + filename);
	    
	URI[] fileServiceURIS = discoverySystem.knownUrisOf(RESTFilesServer.SERVICE);
        WebTarget target = client.target(fileServiceURIS[0]).path(RestFiles.PATH);
                
        //Verify userId and password
        reTry( () -> clt_checkUser(userId, password));

        FileInfo file = new FileInfo(userId, filename, target.path(filename).getUri(), fileSharedWith(filename)));
            
        Response r = target.path(filename))
                    .accept(MediaType.APPLICATION_JSON)
                    .post(Entity.entity(file ,MediaType.APPLICATION_JSON));

        if(userToFilesMapping.get(userId).contains(String.format("%s/%s", userId, filename))){
            int index;
            for(int i = 0; i < userToFilesMapping.get(userId).size(); i++)
                if(userToFilesMapping.get(userId).get(i).equals(String.format("%s/%s", userId, filename)))
                    index = i;

            usertoFileMapping.get(userId).set(index, String.format("%s/%s", userId, filename));
        }else if(!userToFilesMapping.containsKey(userId) ){
            ArrayList newList = new ArrayList<String>();
            userToFilesMapping.put(userId, newList);
            userToFilesMapping.get(userId).add(String.format("%s/%s", userId, filename));
        }else{
            userToFilesMapping.get(userId).add(String.format("%s/%s", userId, filename));
        }

        return file;
    }
    


    //Palma
    @Override
    public void deleteFile(String filename, String userId, String password) {
       Log.info("deleteFile : " + filename);
        
        //Verify userId and password
        reTry( () -> clt_checkUser(userId, password));

        //Verify the filename
        reTry( () -> clt_checkFile(filename));

        
        if(userToFilesMapping.get(userId).contains(String.format("%s/%s", userId, filename))) {
            int index;
            for(int i = 0; i < userToFilesMapping.get(userId).size(); i++)
                if(userToFilesMapping.get(userId).get(i).equals(String.format("%s/%s", userId, filename)))
                    index = i;

			userToFilesMapping.get(userId).remove(i);
		}else{
            Log.info("Filename does not exist in the user files.");
			throw new WebApplicationException(Status.NOT_FOUND);
        } 
    }

    @Override
    public void shareFile(String filename, String userId, String userIdShare, String password) {

        //Check if userIdShare exists
        reTry( () -> clt_checkUser(userIdShare, ""));
        
        
        //Check if filename exists
        reTry( () -> clt_checkFile(filename));
        

        //Check if the userID exists AND if the password if correct
        reTry( () -> clt_checkUser(userId, password));
        
        //If everything is correct then add to shared files
        if(userToFilesMapping.get(userId) == null){
            userToAccessedFilesMapping.put(userIdShare, new ArrayList<String>(Arrays.asList(String.format("%s/%s", userId, filename))));
        }
        else if(!userToFilesMapping.get(userIdShare).contains(String.format("%s/%s", userId, filename))){
           userToAccessedFilesMapping.get(userIdShare).add(String.format("%s/%s", userId, filename));
        }
        
        
    }

    @Override
    public void unshareFile(String filename, String userId, String userIdShare, String password) {

        //Check if userIdShare exists
        reTry( () -> clt_checkUser(userIdShare, ""));
        
        
        //Check if filename exists
        reTry( () -> clt_checkFile(filename));
        

        //Check if the userID exists AND if the password if correct
        reTry( () -> clt_checkUser(userId, password));

        //If everything is correct then remove from shared files
       if(userToAccessedFilesMapping.get(userIdShare) != null && !userToFilesMapping.get(userIdShare).contains(String.format("%s/%s", userId, filename))){
           userToAccessedFilesMapping.get(userIdShare).remove(String.format("%s/%s", userId, filename));
        }
        
    }


    //Reis
    @Override
    public byte[] getFile(String filename, String userId, String accUserId, String password) {
        URI[] userServiceURIS = discoverySystem.knownUrisOf(RESTUsersServer.SERVICE);
        URI[] fileServiceURIS = discoverySystem.knownUrisOf(RESTFilesServer.SERVICE);

        //Check if userIdShare exists
        reTry( () -> clt_checkUser(accUserId, ""));

        //Check if the userID exists AND if the password if correct
        reTry( () -> clt_checkUser(userId, password));

        //Redirect request to File Server 
        WebTarget target = client.target(userServiceURIS[0]).path(RestUsers.PATH);
        target = client.target(fileServiceURIS[0]).path(RestFiles.PATH);

        throw new WebApplicationException(Response.temporaryRedirect(target.path(filename).getUri()).build());
    }

    //Palma
    @Override
    public List<FileInfo> lsFile(String userId, String password) {
        URI[] fileServiceURIS = discoverySystem.knownUrisOf(RESTFilesServer.SERVICE);
        WebTarget target = client.target(fileServiceURIS[0]).path(RestFiles.PATH);

        //Verify userId and password
        reTry( () -> clt_checkUser(userId, password));

        //Files that the user own
        List<String> ownerList = userToFilesMapping.get(userId);
        //Files that the user has access
        List<String> sharedList = userToAccessedFilesMapping.get(userId);
        List<FileInfo> filesList = new ArrayList<>();

        for(int i = 0; i < ownerList.size(); i++){
            String[] userIdFile = ownerList.get(i).split("/");
            FileInfo file = new FileInfo(userId, userIdFile[1], target.path(filename).getUri(), fileSharedWith(userIdFile[1]));
            filesList.add(file);
        }

        for(int i = 0; i < sharedList.size(); i++){
            String[] userIdFile = sharedList.get(i).split("/");
            FileInfo file = new FileInfo(userIdFile[0], userIdFile[1], target.path(filename).getUri(), fileSharedWith(userIdFile[1]));
            filesList.add(file);
        }

        return filesList;
    }
    

    /*Auxiliary methods*/


    private Set<String> fileSharedWith(String filename){

        Set<String> sharedWith = new HashSet<String>(); 

        for (Map.Entry<String, ArrayList<String>> entry : userToAccessedFilesMapping.entrySet()){
            List<String> list = entry.getValue();
            for(int i = 0; i < list.size(); i++)
                if(list.get(i).equals(filename))
                    sharedWith.add(entry.getKey());
        }

        return sharedWith;
    }
		
    private User clt_checkUser(String userId, String password){
        URI[] userServiceURIS = discoverySystem.knownUrisOf(RESTUsersServer.SERVICE);

        WebTarget target = client.target(userServiceURIS[0]).path(RestUsers.PATH);
        Response r = target.path( userId)
				.queryParam(RestUsers.PASSWORD, password).request()
				.accept(MediaType.APPLICATION_JSON)
				.get();
        if(r.getStatus() == Status.NOT_FOUND.getStatusCode()){
            throw new WebApplicationException(Status.NOT_FOUND);
        }
        else if(r.getStatus() == Status.FORBIDDEN.getStatusCode()){
            throw new WebApplicationException(Status.FORBIDDEN);
        }

        return null;
    }

    private File clt_checkFile(String filename){
        URI[] fileServiceURIS = discoverySystem.knownUrisOf(RESTFilesServer.SERVICE);
        WebTarget target = client.target(fileServiceURIS[0]).path(RestFiles.PATH);

        Response r = target.path(filename)
                .request()
				.accept(MediaType.APPLICATION_JSON)
				.get();

        if(r.getStatus() == Status.NOT_FOUND.getStatusCode()){
            throw new WebApplicationException(Status.NOT_FOUND);
        }
        return null;
    }
	

}
