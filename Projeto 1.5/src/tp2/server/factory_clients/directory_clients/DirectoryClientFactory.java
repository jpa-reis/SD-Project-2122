package tp2.server.factory_clients.directory_clients;

import java.net.URI;

import jakarta.inject.Singleton;
import tp2.api.service.util.Directory;

@Singleton
public class DirectoryClientFactory {

   
    public DirectoryClientFactory(){

    }

    public Directory getClient(URI serverURI){
        if( serverURI.toString().endsWith("rest")){
          return new RestDirectoryClient( serverURI );
        } 
        else
          return new SoapDirectoryClient( serverURI );

    }
    
}
