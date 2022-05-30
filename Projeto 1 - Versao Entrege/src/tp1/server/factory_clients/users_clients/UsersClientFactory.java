package tp1.server.factory_clients.users_clients;

import java.net.URI;

import tp1.api.service.util.Users;
import tp1.server.JavaDirectories;

import java.util.logging.Logger;

import jakarta.inject.Singleton;

@Singleton
public class UsersClientFactory {
    protected static Logger Log = Logger.getLogger(JavaDirectories.class.getName());
    public UsersClientFactory(){

    }

    public Users getClient(URI serverURI){
        if( serverURI.toString().endsWith("rest")){
          return new RestUsersClient( serverURI );
        } 
        else
          return new SoapUsersClient( serverURI );

    }
}
