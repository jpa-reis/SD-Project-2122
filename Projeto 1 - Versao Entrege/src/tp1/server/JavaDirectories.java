package tp1.server;


import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


import tp1.Discovery;
import tp1.api.FileInfo;
import tp1.api.User;
import tp1.api.service.util.Directory;
import tp1.api.service.util.Result;
import tp1.api.service.util.Result.ErrorCode;
import tp1.server.factory_clients.files_clients.FilesClientFactory;
import tp1.server.factory_clients.users_clients.UsersClientFactory;

import java.util.logging.Logger;


public class JavaDirectories implements Directory{

    //Mapping of files to their owner and the people with access to the file
    //Pre-Condition: The key is a String concatenated as "userId/filename"
    private final Map<String, HashMap<String, FileInfo>> filesInfo = new ConcurrentHashMap<String, HashMap<String,FileInfo>>();
    private final Map<String, Integer> capacityOfFileServer = new ConcurrentHashMap<String, Integer>();

    /*Discovery system variables and constants*/
    private final Discovery discoverySystem;
    private static final String SERVICE = "directory";
    public static final int PORT = 8080;
    /*----------------------------------------*/
    protected static Logger Log = Logger.getLogger(JavaDirectories.class.getName());

    private int numberOfFileServersDiscovered = 0;
    private String lastServer;
    private boolean canRedirect;

    FilesClientFactory filesClientFactory;
    UsersClientFactory usersClientFactory;

    public JavaDirectories(boolean canRedirect){
        super();
        this.canRedirect = canRedirect;
        /*Initialize discovery system code*/
        discoverySystem = new Discovery(Discovery.DISCOVERY_ADDR, SERVICE, "");
        discoverySystem.listener(); 

        filesClientFactory = new FilesClientFactory();
        usersClientFactory = new UsersClientFactory();
	}

    @Override
    public Result<FileInfo> writeFile(String filename, byte[] data, String userId, String password) {
        Result<User> r = clt_checkUser(userId, password);
        if(!r.isOK()){
            return Result.error(r.error());
            
        }

        lastServer = "null";
        //Try to add a file to the server
        FileInfo fileInfo = clt_writeFile(userId, filename, data, false);
        if(fileInfo == null && numberOfFileServersDiscovered > 1){
            fileInfo = clt_writeFile(userId, filename, data, true);
        }
        else if(fileInfo == null && numberOfFileServersDiscovered == 1){
            return Result.error(ErrorCode.BAD_REQUEST);
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

        return Result.ok(fileInfo);
    }

    @Override
    public Result<Void> deleteFile(String filename, String userId, String password) {
        
        Result<User> r = clt_checkUser(userId, password);
        if(!r.isOK()){
            return Result.error(r.error());
        }

        if(!filesInfo.containsKey(userId)){
            return Result.error(ErrorCode.NOT_FOUND);
        }

        if(!filesInfo.get(userId).containsKey(filename)){
            return Result.error(ErrorCode.NOT_FOUND);
        }

        clt_deleteFile(userId, filename);

        String server = filesInfo.get(userId).get(filename).getFileURL().split("/files")[0];
        int oldCapacity = capacityOfFileServer.get(server);
        capacityOfFileServer.replace(server, oldCapacity - 1);

	    filesInfo.get(userId).remove(filename);
        return Result.ok();
    }

    @Override
    public Result<Void> shareFile(String filename, String userId, String userIdShare, String password) {
        Result<User> r = clt_checkUser(userIdShare, "");
        if(!r.isOK() && !r.error().equals(ErrorCode.FORBIDDEN)){
            return Result.error(r.error());
        }
        r = clt_checkUser(userId, password);
        if(!r.isOK()){
            return Result.error(r.error());
        }
        //Check if filename exist
        if(!filesInfo.get(userId).containsKey(filename)){
            return Result.error(ErrorCode.NOT_FOUND);
        }
        
        //If everything is correct then add to shared files
        filesInfo.get(userId).get(filename).getSharedWith().add(userIdShare);
        return Result.ok();
    }

    @Override
    public Result<Void> unshareFile(String filename, String userId, String userIdShare, String password) {
        Result<User> r = clt_checkUser(userIdShare, "");
        if(!r.isOK() && !r.error().equals(ErrorCode.FORBIDDEN)){
            return Result.error(r.error());
        }
        
        r = clt_checkUser(userId, password);
        if(!r.isOK()){
            return Result.error(r.error());
        }

        //Check if filename exists
        if(!filesInfo.get(userId).containsKey(filename)){
            return Result.error(ErrorCode.NOT_FOUND);
        }

        //If everything is correct then remove from shared files
        if(!filesInfo.get(userId).get(filename).getSharedWith().contains(userIdShare)){
            return Result.error(ErrorCode.NOT_FOUND);
        }

        filesInfo.get(userId).get(filename).getSharedWith().remove(userIdShare);
        return Result.ok();
    }

    @Override
    public Result<byte[]> getFile(String filename, String userId, String accUserId, String password) {
        Result<User> r = clt_checkUser(accUserId, password);
        if(!r.isOK()){
            return Result.error(r.error());
        }

        r = clt_checkUser(userId, "");
        if(!r.isOK() && !r.error().equals(ErrorCode.FORBIDDEN)){
            return Result.error(r.error());
        }
        if(!filesInfo.get(userId).containsKey(filename)){
            return Result.error(ErrorCode.NOT_FOUND);
        }
        if(!filesInfo.get(userId).get(filename).getSharedWith().contains(accUserId) && !accUserId.equals(userId)){
            return Result.error(ErrorCode.FORBIDDEN);
        }    
        return filesClientFactory.getClient(URI.create(filesInfo.get(userId).get(filename).getFileURL()), String.format("%s_%s", userId, filename), canRedirect).getFile(String.format("%s_%s", userId, filename), "");
         
    }

    @Override
    public Result<List<FileInfo>> lsFile(String userId, String password) {
        Result<User> r = clt_checkUser(userId, password);
        if(!r.isOK()){
            return Result.error(r.error());
        }
        List<FileInfo> filesList = new ArrayList<FileInfo>();

        for(Map.Entry<String, HashMap<String, FileInfo>> entry : filesInfo.entrySet()){
            for(Map.Entry<String, FileInfo> entry2: filesInfo.get(entry.getKey()).entrySet()){
                if(entry.getKey().equals(userId) || entry2.getValue().getSharedWith().contains(userId)){
                    filesList.add(entry2.getValue());
                }
                    
            }
        }       
         
        return Result.ok(filesList);

    }

    @Override
    public Result<Void> deleteUserReferences(String userId) {
        if(filesInfo.containsKey(userId)){

            // delete user files
            for(Map.Entry<String, FileInfo> entry: filesInfo.get(userId).entrySet()){
                clt_deleteFile(userId, entry.getKey());
  
            }

            filesInfo.remove(userId);
            
            // delete references to a shared file
            for(Map.Entry<String, HashMap<String, FileInfo>> entry : filesInfo.entrySet()){
                for(Map.Entry<String, FileInfo> entry2: filesInfo.get(entry.getKey()).entrySet()){
                    if(entry2.getValue().getSharedWith().contains(userId)){
                        entry2.getValue().getSharedWith().remove(userId);
                    }               
                }
            } 
        }
        return Result.ok();
    }

    private Result<User> clt_checkUser(String userId, String password){
        URI[] userServiceURIS = discoverySystem.knownUrisOf(JavaUsers.SERVICE);
        while(userServiceURIS.length == 0){
            userServiceURIS = discoverySystem.knownUrisOf(JavaUsers.SERVICE);
        }
        
        Result<User> r = usersClientFactory.getClient(userServiceURIS[0]).getUser(userId, password);
        
        if(r == null)
            return Result.error(ErrorCode.BAD_REQUEST);
        
        return r;
    }

    private FileInfo clt_writeFile(String userId, String filename, byte[] data, boolean failed){
        URI[] fileServiceURIS = discoverySystem.knownUrisOf(JavaFiles.SERVICE);
        while(fileServiceURIS.length == 0){
            fileServiceURIS = discoverySystem.knownUrisOf(JavaFiles.SERVICE);
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
                    // add a new discovered server
                    if(!capacityOfFileServer.containsKey(fileServiceURIS[i].toString())){
                        capacityOfFileServer.put(fileServiceURIS[i].toString(), 0);
                    }
                    // check which server has the lowest capacity, excluding the lastServer used if the operation previous failed
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

        // store the last server used - only for when the operation fails
        lastServer = minServer;

        String fileId = String.format("%s_%s", userId, filename);
        
        filesClientFactory.getClient(URI.create(minServer), fileId, canRedirect).writeFile(fileId, data, "");

        Set<String> sharedWith;
        if(!filesInfo.containsKey(userId) || !filesInfo.get(userId).containsKey(filename) || filesInfo.get(userId).get(filename).getSharedWith().isEmpty())
            sharedWith = new HashSet<String>();
        else
            sharedWith = filesInfo.get(userId).get(filename).getSharedWith();

        FileInfo fileInfo = new FileInfo(userId, filename, String.format("%s/%s/%s_%s", minServer, "files", userId, filename), sharedWith);
        
        int newCapacity = capacityOfFileServer.get(minServer)+1;
        capacityOfFileServer.replace(minServer, newCapacity);
        
        return fileInfo;
    }

    private void clt_deleteFile(String userId, String filename){
        filesClientFactory.getClient(URI.create(filesInfo.get(userId).get(filename).getFileURL()), String.format("%s_%s", userId, filename), canRedirect).deleteFile(String.format("%s_%s", userId, filename), "");
    }
    
}
