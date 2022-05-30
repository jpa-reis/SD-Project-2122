package tp2.server.factory_clients.files_clients;

import java.net.URI;
import java.util.logging.Logger;

import jakarta.inject.Singleton;
import tp2.api.service.util.Files;
import tp2.server.JavaFiles;

@Singleton
public class FilesClientFactory {
  protected static Logger Log = Logger.getLogger(JavaFiles.class.getName());
    public FilesClientFactory(){

    }

    public Files getClient(URI serverURI, String fileId, boolean canRedirect){
      
        if( serverURI.toString().split("/files")[0].endsWith("rest")){
          return new RestFilesClient( serverURI , canRedirect);
        } 
        else
          return new SoapFilesClient( serverURI );

    }
}
