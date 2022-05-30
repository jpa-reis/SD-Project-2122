package tp2.server.soap_webServices;

import java.util.List;

import jakarta.inject.Singleton;
import jakarta.jws.WebService;
import tp2.api.FileInfo;
import tp2.api.service.soap.DirectoryException;
import tp2.api.service.soap.SoapDirectory;
import tp2.api.service.util.Directory;
import tp2.api.service.util.Result.ErrorCode;
import tp2.server.JavaDirectories;

@WebService(serviceName=SoapDirectory.NAME, targetNamespace=SoapDirectory.NAMESPACE, endpointInterface=SoapDirectory.INTERFACE)
@Singleton
public class dirWebService implements SoapDirectory{
    
    private static final String CONFLICT = "Conflict";
	private static final String BAD_REQUEST = "Bad Request";
	private static final String NOT_FOUND = "Not found";
	private static final String FORBIDDEN = "Wrong password";

    final Directory impl = new JavaDirectories(false);

    @Override
    public FileInfo writeFile(String filename, byte[] data, String userId, String password) throws DirectoryException {
        var result = impl.writeFile(filename, data, userId, password);
        if( result.isOK() )
            return result.value();
        else
            throw new DirectoryException(getStatus(result.error()));
    }

    @Override
    public void deleteFile(String filename, String userId, String password) throws DirectoryException {
        var result = impl.deleteFile(filename, userId, password);
        if( !result.isOK() )
            throw new DirectoryException(getStatus(result.error()));
        
    }

    @Override
    public void shareFile(String filename, String userId, String userIdShare, String password)
            throws DirectoryException {
        var result = impl.shareFile(filename, userId, userIdShare, password);
        if( !result.isOK() )
            throw new DirectoryException(getStatus(result.error()));
        
    }

    @Override
    public void unshareFile(String filename, String userId, String userIdShare, String password)
            throws DirectoryException {
        var result = impl.unshareFile(filename, userId, userIdShare, password);
        if( !result.isOK() )
            throw new DirectoryException(getStatus(result.error()));
        
    }

    @Override
    public byte[] getFile(String filename, String userId, String accUserId, String password) throws DirectoryException {
        var result = impl.getFile(filename, userId, accUserId, password);
        if( !result.isOK() )
            throw new DirectoryException(getStatus(result.error())) ;
        return result.value();
    }

    @Override
    public List<FileInfo> lsFile(String userId, String password) throws DirectoryException {
        var result = impl.lsFile(userId, password);
        if( !result.isOK() )
            throw new DirectoryException(getStatus(result.error()));
        return result.value();
    }

    @Override
    public void deleteUserS(String userId) {
        impl.deleteUserReferences(userId);
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
