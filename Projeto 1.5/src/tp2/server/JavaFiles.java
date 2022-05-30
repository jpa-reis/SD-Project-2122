package tp2.server;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import tp2.Discovery;
import tp2.api.service.util.Files;
import tp2.api.service.util.Result;
import tp2.api.service.util.Result.ErrorCode;

public class JavaFiles implements Files{
    protected static Logger Log = Logger.getLogger(JavaDirectories.class.getName());

    private final Map<String, byte[]> files = new ConcurrentHashMap<>();
    
    /*Discovery system variables and constants*/
    private final Discovery discoverySystem;
    static final String SERVICE = "files";
    public static final int PORT = 8080;
    /*----------------------------------------*/
    public JavaFiles(){
        super();
        discoverySystem = new Discovery(Discovery.DISCOVERY_ADDR, SERVICE, "");
        discoverySystem.listener(); 
    }

    @Override
    public Result<Void> writeFile(String fileId, byte[] data, String token) {
        //Check if arguments are valid
		if(fileId == null || data == null){
			return Result.error(ErrorCode.BAD_REQUEST);
		}
		
		//Check if fileId exists, overwrites the content
		if(files.containsKey(fileId)){
			files.replace(fileId, data);
		}
		else{
			//Add the file to the map of files
			files.put(fileId, data);
		}

        return Result.ok();
    }

    @Override
    public Result<Void> deleteFile(String fileId, String token) {
        if(fileId == null)
			return Result.error(ErrorCode.BAD_REQUEST);

		if(files.containsKey(fileId))
			files.remove(fileId);
		else
			return Result.error(ErrorCode.NOT_FOUND);

        return Result.ok();
    }

    @Override
    public Result<byte[]> getFile(String fileId, String token) {
        if(fileId == null)
			return Result.error(ErrorCode.BAD_REQUEST);
		
		if(files.containsKey(fileId))
			return Result.ok(files.get(fileId));
		else
			return Result.error(ErrorCode.NOT_FOUND);
    }
    
}
