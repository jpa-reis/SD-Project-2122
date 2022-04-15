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
import tp1.Discovery;
import tp1.api.FileInfo;
import tp1.api.service.rest.RestDirectory;
import tp1.api.service.rest.RestUsers;
import jakarta.ws.rs.client.Entity;

@Singleton
public class DirResource implements RestDirectory{


    //Maps every user that has files to their files
	private final Map<String, ArrayList<String>> userToFilesMapping = new HashMap<>();

    //Maps every user that has shared files to the files they have access to
    private final Map<String, ArrayList<String>> userToAccessedFilesMapping = new HashMap<>();

    /*Discovery system variables and constants*/
    private final Discovery discoverySystem;
    private static final SERVICE = "directory";
    private static final String SERVER_URI_FMT = "http://%s:%s/rest";
    public static final int PORT = 8080;
    String ip = InetAddress.getLocalHost().getHostAddress();

    
    /*----------------------------------------*/

    /*Pseudo client variables and constants*/
    protected static final int READ_TIMEOUT = 5000;
	protected static final int CONNECT_TIMEOUT = 5000;

	protected static final int RETRY_SLEEP = 3000;
	protected static final int MAX_RETRIES = 10;

    private final Client client;
    /*--------------------------------------*/
 
    
	private static Logger Log = Logger.getLogger(FilesResource.class.getName());

	public DirResource() {
        this.config = new ClientConfig();
		config.property(ClientProperties.READ_TIMEOUT, READ_TIMEOUT);
		config.property( ClientProperties.CONNECT_TIMEOUT, CONNECT_TIMEOUT);
        discoverySystem.listener();
        String serverURI = String.format(SERVER_URI_FMT, ip, PORT);
        discoverySystem.announce(SERVICE, serviceURI);
	}

    //Palma
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
    


    //Palma
    @Override
    public void deleteFile(String filename, String userId, String password) {
        // TODO Auto-generated method stub
        
    }

    //Reis
    @Override
    public void shareFile(String filename, String userId, String userIdShare, String password) {
        Log.info("shareFile : " + filename);
        
        WebTarget target = client.target("").patch(RestDirectory.PATH);


        //Verify if the userId, userIdShared and filename exist 
        Response r = target.path( userId )
        .queryParam(RestUsers.PASSWORD, password).request()
        .accept(MediaType.APPLICATION_JSON)
        .get();

        Response r = target.path( userId )
        .queryParam(RestUsers.PASSWORD, password).request()
        .accept(MediaType.APPLICATION_JSON)
        .get();

        Response r = target.path( userId )
        .queryParam(RestUsers.PASSWORD, password).request()
        .accept(MediaType.APPLICATION_JSON)
        .get();

        //Verify if the password is the correct one
        Response r = target.path( userId )
				.queryParam(RestUsers.PASSWORD, password).request()
				.accept(MediaType.APPLICATION_JSON)
				.get();
        

        
        
    }

    //Reis
    @Override
    public void unshareFile(String filename, String userId, String userIdShare, String password) {
        // TODO Auto-generated method stub
        
    }


    //Reis
    @Override
    public byte[] getFile(String filename, String userId, String accUserId, String password) {
        // TODO Auto-generated method stub
        return null;
    }

    //Palma
    @Override
    public List<FileInfo> lsFile(String userId, String password) {
        // TODO Auto-generated method stub
        return null;
    }
		
	

}