package tp2.server.rest_resources;

import java.util.List;

import jakarta.inject.Singleton;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response.Status;
import tp2.api.User;
import tp2.api.service.rest.RestUsers;
import tp2.api.service.util.Users;
import tp2.api.service.util.Result.ErrorCode;
import tp2.server.JavaUsers;

@Singleton
public class UsersResources implements RestUsers{

    final Users impl = new JavaUsers();


    @Override
    public String createUser(User user) {
        var result = impl.createUser( user );
        if( result.isOK() )
            return result.value();
        else
            throw new WebApplicationException(getStatus(result.error())) ;
    }

    @Override
    public User getUser(String userId, String password) {
        var result = impl.getUser( userId, password );
        if( result.isOK() )
            return result.value();
        else
            throw new WebApplicationException(getStatus(result.error())) ;
    }

    @Override
    public User updateUser(String userId, String password, User user) {
        var result = impl.updateUser( userId, password, user );
        if( result.isOK() )
            return result.value();
        else
            throw new WebApplicationException(getStatus(result.error())) ;
    }

    @Override
    public User deleteUser(String userId, String password) {
        var result = impl.deleteUser( userId, password);
        if( result.isOK() )
            return result.value();
        else
            throw new WebApplicationException(getStatus(result.error())) ;
    }

    @Override
    public List<User> searchUsers(String pattern) {
        var result = impl.searchUsers( pattern );
        if( result.isOK() )
            return result.value();
        else
            throw new WebApplicationException(getStatus(result.error())) ;
    }

    private Status getStatus(ErrorCode code){
        switch(code){
            case NOT_FOUND: 
                return Status.NOT_FOUND;
            case BAD_REQUEST:
                return Status.BAD_REQUEST;
            case FORBIDDEN:
                return Status.FORBIDDEN;
            case CONFLICT:
                return Status.CONFLICT;
            case INTERNAL_ERROR:
                return Status.INTERNAL_SERVER_ERROR;
            default:
                return Status.NOT_IMPLEMENTED;
        }
    }
     
}
