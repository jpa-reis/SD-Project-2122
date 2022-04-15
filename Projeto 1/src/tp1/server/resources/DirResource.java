package tp1.server.resources;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;

import jakarta.inject.Singleton;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import tp1.Discovery;
import tp1.api.FileInfo;
import tp1.api.service.rest.RestDirectory;
import tp1.api.service.rest.RestFiles;
import tp1.api.service.rest.RestUsers;
import tp1.server.RESTFilesServer;
import tp1.server.RESTUsersServer;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;

@Singleton
public class DirResource implements RestDirectory{


    //Maps every user that has files to their files
	private final Map<String, ArrayList<String>> userToFilesMapping = new HashMap<>();

    //Maps every user that has shared files to the files they have access to
    private final Map<String, ArrayList<String>> userToAccessedFilesMapping = new HashMap<>();

    /*Discovery system variables and constants*/
    private final Discovery discoverySystem;
    private static final String SERVICE = "directory";
    private static final String SERVER_URI_FMT = "http://%s:%s/rest";
    public static final int PORT = 8080;
    final ClientConfig config;
    
    /*----------------------------------------*/

    /*Pseudo client variables and constants*/
    protected static final int READ_TIMEOUT = 5000;
	protected static final int CONNECT_TIMEOUT = 5000;

	protected static final int RETRY_SLEEP = 3000;
	protected static final int MAX_RETRIES = 10;

    private final Client client;
    /*--------------------------------------*/
 
    
	private static Logger Log = Logger.getLogger(FilesResource.class.getName());

	public DirResource() throws UnknownHostException {
        /*Initialize local client code*/
        this.config = new ClientConfig();
		config.property(ClientProperties.READ_TIMEOUT, READ_TIMEOUT);
		config.property( ClientProperties.CONNECT_TIMEOUT, CONNECT_TIMEOUT);
        this.client = ClientBuilder.newClient(config);

        /*Initialize discovery system code*/
        String ip = InetAddress.getLocalHost().getHostAddress();
        String serviceURI = String.format(SERVER_URI_FMT, ip, PORT);
        discoverySystem = new Discovery(Discovery.DISCOVERY_ADDR, SERVICE, serviceURI);
        discoverySystem.listener(); 
        discoverySystem.announce(SERVICE, serviceURI);

   
	}

    //Palma
    @Override
    public FileInfo writeFile(String filename, byte[] data, String userId, String password) {
        // TODO Auto-generated method stub
        return null;
    }
    


    //Palma
    @Override
    public void deleteFile(String filename, String userId, String password) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void shareFile(String filename, String userId, String userIdShare, String password) {

        //Check if userIdShare exists
        checkUser(userIdShare, "");
        
        //Check if userShared exists
        checkFile(filename);

        //Check if the userID exists AND if the password if correct
        checkUser(userId, password);

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
        checkUser(userIdShare, "");
        
        //Check if userShared exists
        checkFile(filename);

        //Check if the userID exists AND if the password if correct
        checkUser(userId, password);

        //If everything is correct then add to shared files
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
        checkUser(accUserId, "");

        //Check if the userID exists AND if the password if correct
        checkUser(userId, password);

        //Redirect request to File Server 
        WebTarget target = client.target(userServiceURIS[0]).path(RestUsers.PATH);
        target = client.target(fileServiceURIS[0]).path(RestFiles.PATH);

        throw new WebApplicationException(Response.temporaryRedirect(target.path(filename).getUri()).build());
    }

    //Palma
    @Override
    public List<FileInfo> lsFile(String userId, String password) {
        // TODO Auto-generated method stub
        return null;
    }
    

    /*Auxiliary methods*/
		
    private void  checkUser(String userId, String password){
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
    }

    private void checkFile(String filename){
        URI[] fileServiceURIS = discoverySystem.knownUrisOf(RESTFilesServer.SERVICE);
        WebTarget target = client.target(fileServiceURIS[0]).path(RestFiles.PATH);

        Response r = target.path(filename)
                .request()
				.accept(MediaType.APPLICATION_JSON)
				.get();
        if(r.getStatus() == Status.NOT_FOUND.getStatusCode()){
            throw new WebApplicationException(Status.NOT_FOUND);
        }
    }
	

}