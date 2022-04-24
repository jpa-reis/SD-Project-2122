package tp1.server.interoperable.clients.files_clients;

import java.net.URI;

import tp1.api.service.util.Files;
import tp1.server.interoperable.JavaFiles;

import java.util.logging.Logger;

import jakarta.inject.Singleton;

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
