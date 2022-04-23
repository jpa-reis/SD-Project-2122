package tp1.server.soap;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.xml.namespace.QName;

import tp1.api.FileInfo;
import tp1.Discovery;
import jakarta.jws.WebService;
import jakarta.xml.ws.Service;
import tp1.api.service.soap.SoapDirectory;
import tp1.api.service.soap.SoapFiles;
import tp1.api.service.soap.SoapUsers;
import tp1.api.service.soap.UsersException;
import tp1.server.resources.RestClient;
import tp1.api.service.soap.DirectoryException;
import tp1.api.service.soap.FilesException;

@WebService(serviceName=SoapDirectory.NAME, targetNamespace=SoapDirectory.NAMESPACE, endpointInterface=SoapDirectory.INTERFACE)
public class SoapDirWebService extends RestClient implements SoapDirectory{

    private final Map<String, HashMap<String, FileInfo>> filesInfo = new HashMap<String, HashMap<String,FileInfo>>();
    /*Discovery system variables and constants*/
    private final Discovery discoverySystem;
    private static final String SERVICE = "directory";
    private static final String SERVER_URI_FMT = "http://%s:%s/soap";
    public static final int PORT = 8080;
    public final String IP;

    private static final String CONFLICT = "Conflict";
	private static final String BAD_REQUEST = "Bad Request";
	private static final String NOT_FOUND = "Not found";
	private static final String FORBIDDEN = "Wrong password";

	static Logger Log = Logger.getLogger(SoapDirWebService.class.getName());

    public SoapDirWebService()throws UnknownHostException {
        super(URI.create(String.format(SERVER_URI_FMT, InetAddress.getLocalHost().getHostAddress(), PORT)));

        IP = InetAddress.getLocalHost().getHostAddress();
        /*Initialize discovery system code*/
     
        discoverySystem = new Discovery(Discovery.DISCOVERY_ADDR, SERVICE, serverURI.toString());
        discoverySystem.listener(); 
        discoverySystem.announce(SERVICE, serverURI.toString());

	}


	@Override
	public FileInfo writeFile(String filename, byte []data, String userId, String password) throws DirectoryException{
        Log.info("writeFile : " + filename);
        
        //Verify userId and password
        UsersException r = reTry( () -> clt_checkUser(userId, password));
        if(r != null){
            if(r.getMessage().equals(NOT_FOUND)){
                throw new DirectoryException(NOT_FOUND);
            }
            else if(r.getMessage().equals(FORBIDDEN)){
                throw new DirectoryException(FORBIDDEN);
            }
        }
       

        //Try to add a file to the server
        FileInfo fileInfo = reTry( () -> clt_writeFile(userId, filename, data));

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
    public void deleteFile(String filename, String userId, String password) throws DirectoryException {
         Log.info("deleteFile : " + filename);
        
        //Verify userId and password
        UsersException r = reTry( () -> clt_checkUser(userId, password));
        if(r != null){
            if(r.getMessage().equals(NOT_FOUND)){
                throw new DirectoryException(NOT_FOUND);
            }
            else if(r.getMessage().equals(FORBIDDEN)){
                throw new DirectoryException(FORBIDDEN);
            }
        }
        
        
        
        if(!filesInfo.containsKey(userId)){
            throw new DirectoryException(NOT_FOUND);
        }

        if(!filesInfo.get(userId).containsKey(filename)){
            throw new DirectoryException(NOT_FOUND);
        }
        
        
        reTry( () -> clt_deleteFile(String.format("%s_%s", userId, filename)));
        
       
	    filesInfo.get(userId).remove(filename);
        
    }


    @Override
    public void shareFile(String filename, String userId, String userIdShare, String password)
            throws DirectoryException {
        
        //Check if userIdShare exists
        UsersException r = reTry( () -> clt_checkUser(userIdShare, ""));
        if(r != null){
            if(r.getMessage().equals(NOT_FOUND)){
                throw new DirectoryException(NOT_FOUND);
            }
        }

        //Verify userId and password
        r = reTry( () -> clt_checkUser(userId, password));
        if(r != null){
            if(r.getMessage().equals(NOT_FOUND)){
                throw new DirectoryException(NOT_FOUND);
            }
            else if(r.getMessage().equals(FORBIDDEN)){
                throw new DirectoryException(FORBIDDEN);
            }
        }

        //Check if filename exists
        if(!filesInfo.get(userId).containsKey(filename)){
            throw new DirectoryException(NOT_FOUND);
        }

        //If everything is correct then add to shared files
        filesInfo.get(userId).get(filename).getSharedWith().add(userIdShare);
        
        
    }


    @Override
    public void unshareFile(String filename, String userId, String userIdShare, String password)
            throws DirectoryException {
       
        //Check if userIdShare exists
        UsersException r = reTry( () -> clt_checkUser(userIdShare, ""));
        if(r != null){
            if(r.getMessage().equals(NOT_FOUND)){
                throw new DirectoryException(NOT_FOUND);
            }
        }

        //Verify userId and password
        r = reTry( () -> clt_checkUser(userId, password));
        if(r != null){
            if(r.getMessage().equals(NOT_FOUND)){
                throw new DirectoryException(NOT_FOUND);
            }
            else if(r.getMessage().equals(FORBIDDEN)){
                throw new DirectoryException(FORBIDDEN);
            }
        }
        
        //Check if filename exists
        if(!filesInfo.get(userId).containsKey(filename)){
            throw new DirectoryException(NOT_FOUND);
        }

        //If everything is correct then remove from shared files
        if(!filesInfo.get(userId).get(filename).getSharedWith().contains(userIdShare)){
            throw new DirectoryException(NOT_FOUND);
        }

        filesInfo.get(userId).get(filename).getSharedWith().remove(userIdShare);
        
        
    }


    @Override
    public byte[] getFile(String filename, String userId, String accUserId, String password) throws DirectoryException {
        //Check if userIdShare exists
        UsersException r = reTry( () -> clt_checkUser(accUserId, password));
        if(r != null){
            if(r.getMessage().equals(NOT_FOUND)){
                throw new DirectoryException(NOT_FOUND);
            }
            else if(r.getMessage().equals(FORBIDDEN)){
                throw new DirectoryException(FORBIDDEN);
            }
        }
        //Check if the userID exists AND if the password if correct
        r = reTry( () -> clt_checkUser(userId, ""));
        if(r != null){
            if(r.getMessage().equals(NOT_FOUND)){
                throw new DirectoryException(NOT_FOUND);
            }
        }

        //Redirect request to File Server
        if(!filesInfo.get(userId).containsKey(filename)){
            throw new DirectoryException(NOT_FOUND);
        }
        
       
       if(!filesInfo.get(userId).get(filename).getSharedWith().contains(accUserId) && !accUserId.equals(userId)){
            throw new DirectoryException(FORBIDDEN);
        }    

        return reTry( () -> clt_getFile(userId, filename));    
    
    }


    @Override
    public List<FileInfo> lsFile(String userId, String password) throws DirectoryException {
        //Check if userId exists
        UsersException r = reTry( () -> clt_checkUser(userId, password));
        if(r != null){
            if(r.getMessage().equals(NOT_FOUND)){
                throw new DirectoryException(NOT_FOUND);
            }
            else if(r.getMessage().equals(FORBIDDEN)){
                throw new DirectoryException(FORBIDDEN);
            }
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
	public void deleteUserS(String userId) {

        for(Map.Entry<String, FileInfo> entry: filesInfo.get(userId).entrySet()){
           reTry( () -> clt_deleteFile(String.format("%s_%s", userId, entry.getKey())));
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

    private byte[] clt_getFile(String userId, String filename){

        QName qname = new QName(SoapFiles.NAMESPACE, SoapFiles.NAME);		
		Service service;
        try {
            service = Service.create( URI.create(filesInfo.get(userId).get(filename).getFileURL()+ "?wsdl").toURL(), qname);
        } catch (MalformedURLException e1) {
            e1.printStackTrace();
            return null;
        }	

		SoapFiles files = service.getPort(tp1.api.service.soap.SoapFiles.class);

        try {
            return files.getFile(String.format("%s_%s", userId, filename), "");
        } catch (FilesException e1) {
        }

        return null;
        
    }
		
    private UsersException clt_checkUser(String userId, String password){
        URI[] userServiceURIS = discoverySystem.knownUrisOf(SOAPUsersServer.SERVICE_NAME);
        while(userServiceURIS.length == 0){
            userServiceURIS = discoverySystem.knownUrisOf(SOAPUsersServer.SERVICE_NAME);
        }
        
        QName qname = new QName(SoapUsers.NAMESPACE, SoapUsers.NAME);		
		Service service;
        try {
            service = Service.create( URI.create(userServiceURIS[0]+ "?wsdl").toURL(), qname);
        } catch (MalformedURLException e1) {
            e1.printStackTrace();
            return null;
        }	

		SoapUsers users = service.getPort(tp1.api.service.soap.SoapUsers.class);

       try {
            users.getUser(userId, password);
        } catch (UsersException e1) {
            return e1;
        }

        return null;
    }

   
    private FileInfo clt_writeFile(String userId, String filename, byte[] data){

        URI[] fileServiceURIS = discoverySystem.knownUrisOf(SOAPFilesServer.SERVICE_NAME);
        while(fileServiceURIS.length == 0){
            fileServiceURIS = discoverySystem.knownUrisOf(SOAPFilesServer.SERVICE_NAME);
        }

        QName qname = new QName(SoapFiles.NAMESPACE, SoapFiles.NAME);		
		Service service;
        try {
            service = Service.create( URI.create(fileServiceURIS[0] + "?wsdl").toURL(), qname);
        } catch (MalformedURLException e1) {
            e1.printStackTrace();
            return null;
        }		
		SoapFiles files = service.getPort(tp1.api.service.soap.SoapFiles.class);
        
        try {
            files.writeFile(String.format("%s_%s", userId, filename), data, "");
        } catch (FilesException e) {
            e.printStackTrace();
        }
        
        FileInfo fileInfo = new FileInfo(userId, filename, String.format("%s/%s_%s", fileServiceURIS[0], userId, filename), new HashSet<String>());
        
        return fileInfo;
    }

	 private FileInfo clt_deleteFile(String fileId){

        URI[] fileServiceURIS = discoverySystem.knownUrisOf(SOAPFilesServer.SERVICE_NAME);
        while(fileServiceURIS.length == 0){
            fileServiceURIS = discoverySystem.knownUrisOf(SOAPFilesServer.SERVICE_NAME);
        }

        QName qname = new QName(SoapFiles.NAMESPACE, SoapFiles.NAME);		
		Service service;
        try {
            service = Service.create( URI.create(fileServiceURIS[0]+ "?wsdl").toURL(), qname);
        } catch (MalformedURLException e1) {
            e1.printStackTrace();
            return null;
        }		
		SoapFiles files = service.getPort(tp1.api.service.soap.SoapFiles.class);
        
        try {
            files.deleteFile(fileId, "");
        } catch (FilesException e) {
            e.printStackTrace();
        }
                    
        return null;
    }
    

}
