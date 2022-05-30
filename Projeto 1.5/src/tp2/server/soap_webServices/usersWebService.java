package tp2.server.soap_webServices;

import java.util.List;

import jakarta.inject.Singleton;
import jakarta.jws.WebService;
import tp2.api.User;
import tp2.api.service.soap.SoapUsers;
import tp2.api.service.soap.UsersException;
import tp2.api.service.util.Users;
import tp2.api.service.util.Result.ErrorCode;
import tp2.server.JavaUsers;


@WebService(serviceName=SoapUsers.NAME, targetNamespace=SoapUsers.NAMESPACE, endpointInterface=SoapUsers.INTERFACE)
@Singleton
public class usersWebService implements SoapUsers{

    private static final String CONFLICT = "Conflict";
	private static final String BAD_REQUEST = "Bad Request";
	private static final String NOT_FOUND = "Not found";
	private static final String FORBIDDEN = "Wrong password";
    
    final Users impl = new JavaUsers();

	@Override
	public String createUser(User user) throws UsersException {
		var result = impl.createUser( user );
        if( result.isOK() )
            return result.value();
        else
            throw new UsersException(getStatus(result.error()));
    
	}

	@Override
	public User getUser(String userId, String password) throws UsersException {
		 var result = impl.getUser( userId, password );
        if( result.isOK() )
            return result.value();
        else
            throw new UsersException(getStatus(result.error()));
    
	}

	@Override
	public User updateUser(String userId, String password, User user) throws UsersException {
		var result = impl.updateUser( userId, password, user );
        if( result.isOK() )
            return result.value();
        else
            throw new UsersException(getStatus(result.error()));
	}

	@Override
	public User deleteUser(String userId, String password) throws UsersException {
		var result = impl.deleteUser( userId, password);
        if( result.isOK() )
            return result.value();
        else
            throw new UsersException(getStatus(result.error()));
    
	}

	@Override
	public List<User> searchUsers(String pattern) throws UsersException {
		var result = impl.searchUsers( pattern );
        if( result.isOK() )
            return result.value();
        else
           throw new UsersException(getStatus(result.error()));
	}

    private String getStatus(ErrorCode code){
        switch(code){
            case NOT_FOUND: 
                return NOT_FOUND;
            case BAD_REQUEST:
                return BAD_REQUEST;
            case FORBIDDEN:
                return FORBIDDEN;
            default: return CONFLICT;
        }
    }

}
