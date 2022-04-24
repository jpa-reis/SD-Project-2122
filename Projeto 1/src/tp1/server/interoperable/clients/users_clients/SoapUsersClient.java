package tp1.server.interoperable.clients.users_clients;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.List;

import javax.xml.namespace.QName;
import tp1.api.service.soap.SoapUsers;
import tp1.api.service.soap.UsersException;
import tp1.api.User;
import tp1.api.service.util.Result;
import tp1.api.service.util.Users;
import tp1.clients.SoapClient;
import jakarta.xml.ws.Service;
import tp1.api.service.util.Result.ErrorCode;


public class SoapUsersClient extends SoapClient implements Users{

    private static final String CONFLICT = "Conflict";
	private static final String BAD_REQUEST = "Bad Request";
	private static final String NOT_FOUND = "Not found";
	private static final String FORBIDDEN = "Wrong password";

    private URI serverURI;

    public SoapUsersClient(URI serverURI){
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
            QName qname = new QName(SoapUsers.NAMESPACE, SoapUsers.NAME);		
            Service service;
            try {
                service = Service.create( URI.create(serverURI+ "?wsdl").toURL(), qname);
            } catch (MalformedURLException e1) {
                e1.printStackTrace();
                return null;
            }	

            SoapUsers users = service.getPort(tp1.api.service.soap.SoapUsers.class);
            try {
                return Result.ok(users.getUser(userId, password));
            } catch (UsersException e1) {
                return Result.error(getErrorCode(e1.getMessage()));
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

    private ErrorCode getErrorCode(String code){
        switch(code){
            case NOT_FOUND: 
                return ErrorCode.NOT_FOUND;
            case BAD_REQUEST:
                return ErrorCode.BAD_REQUEST;
            case FORBIDDEN:
                return ErrorCode.FORBIDDEN;
            default:
                return ErrorCode.CONFLICT;
        }
        
    }
    
}
