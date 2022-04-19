package tp1.server.resources;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import jakarta.inject.Singleton;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response.Status;
import tp1.api.service.rest.RestFiles;

@Singleton
public class FilesResource implements RestFiles{

	private final Map<String, byte[]> files = new HashMap<>();

	private static Logger Log = Logger.getLogger(FilesResource.class.getName());
	
	public FilesResource() {
	}
		
	@Override
	public void writeFile(String fileId, byte[] data, String token){
		
		Log.info("Writing file: " + fileId);

		//Check if arguments are valid
		if(fileId == null || data == null){
			Log.info("fileId or data null.");
			throw new WebApplicationException(Status.BAD_REQUEST);
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


	@Override
	public void deleteFile(String fileId, String token) {
		Log.info("Deleting file : " + fileId);
		
		if(fileId == null){
			Log.info("fileId null.");
			throw new WebApplicationException(Status.BAD_REQUEST);
		}

		if(files.containsKey(fileId))
			files.remove(fileId);
		else{
			Log.info("File does not exist.");
			throw new WebApplicationException(Status.NOT_FOUND);
		}
	}


	@Override
	public byte[] getFile(String fileId, String token) {
		Log.info("Getting file: " + fileId);

		if(fileId == null){
			Log.info("fileId null.");
			throw new WebApplicationException(Status.BAD_REQUEST);
		}
		if(files.containsKey(fileId))
			return files.get(fileId);
		else{
			Log.info("File does not exist.");
			throw new WebApplicationException(Status.NOT_FOUND);
		}


	}

}