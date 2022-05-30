package tp1.server.factory_clients.users_clients;

import java.net.URI;
import java.util.List;

import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import tp1.api.User;
import tp1.api.service.rest.RestUsers;
import tp1.api.service.util.Result;
import tp1.api.service.util.Users;
import tp1.clients.RestClient;
import tp1.server.JavaDirectories;
import tp1.api.service.util.Result.ErrorCode;

import java.util.logging.Logger;

public class RestUsersClient extends RestClient implements Users{
    protected static Logger Log = Logger.getLogger(JavaDirectories.class.getName());
    private URI serverURI;

    public RestUsersClient(URI serverURI){
        super();
        this.serverURI = serverURI;
    }

    @Override
    public Result<String> createUser(User user) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Result<User> getUser(String userId, String password) {
        return reTry(() -> {
            WebTarget target = client.target(serverURI).path(RestUsers.PATH);
            Response r = target.path(userId)
                    .queryParam(RestUsers.PASSWORD, password).request()
                    .accept(MediaType.APPLICATION_JSON)
                    .get();

            if(r.getStatus() == Status.OK.getStatusCode()){
                return Result.ok();   
            }
            else{
                return Result.error(getErrorCode(r.getStatus()));
            }
        });
    }

    @Override
    public Result<User> updateUser(String userId, String password, User user) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Result<User> deleteUser(String userId, String password) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Result<List<User>> searchUsers(String pattern) {
        // TODO Auto-generated method stub
        return null;
    }

    private ErrorCode getErrorCode(int code){
        switch(code){
            case 404: 
                return ErrorCode.NOT_FOUND;
            case 400:
                return ErrorCode.BAD_REQUEST;
            case 403:
                return ErrorCode.FORBIDDEN;
            default:
                return ErrorCode.CONFLICT;
        }
    }
    
}
