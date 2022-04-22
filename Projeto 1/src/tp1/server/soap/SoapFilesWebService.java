package tp1.server.soap;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import jakarta.jws.WebService;
import tp1.api.service.soap.FilesException;
import tp1.api.service.soap.SoapFiles;

@WebService(serviceName=SoapFiles.NAME, targetNamespace=SoapFiles.NAMESPACE, endpointInterface=SoapFiles.INTERFACE)

public class SoapFilesWebService  implements SoapFiles{

	private static final String BAD_REQUEST = "Bad Request";
	private static final String NOT_FOUND = "Not found";

    private final Map<String, byte[]> files = new HashMap<>();
	static Logger Log = Logger.getLogger(SoapFilesWebService.class.getName());

    public SoapFilesWebService(){

    }

    @Override
    public byte[] getFile(String fileId, String token) throws FilesException {
        Log.info("Getting file: " + fileId);
		if(fileId == null){
			Log.info("fileId null.");
			throw new FilesException(BAD_REQUEST);
		}
		if(files.containsKey(fileId))
			return files.get(fileId);
		else{
			Log.info("File does not exist.");
			throw new FilesException(NOT_FOUND);
		}
    }

    @Override
    public void deleteFile(String fileId, String token) throws FilesException {
        Log.info("Deleting file : " + fileId);
		
		if(fileId == null){
			Log.info("fileId null.");
			throw new FilesException(BAD_REQUEST);
		}

		if(files.containsKey(fileId))
			files.remove(fileId);
		else{
			Log.info("File does not exist.");
			throw new FilesException(NOT_FOUND);
		}
        
    }

    @Override
    public void writeFile(String fileId, byte[] data, String token) throws FilesException {
        Log.info("Writing file: " + fileId);

		//Check if arguments are valid
		if(fileId == null || data == null){
			Log.info("fileId or data null.");
			throw new FilesException(BAD_REQUEST);
		}
		
		//Check if fileId exists, overwrites the content
		if(files.containsKey(fileId)){
			files.replace(fileId, data);
		}
		else{
			//Add the file to the map of files
			files.put(fileId, data);
		}	
        
    }

}
