package tp1.server.rest_resources;

import java.util.List;
import jakarta.inject.Singleton;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response.Status;
import tp1.api.FileInfo;
import tp1.api.service.rest.RestDirectory;
import tp1.api.service.util.Directory;
import tp1.api.service.util.Result.ErrorCode;
import tp1.server.JavaDirectories;

@Singleton
public class DirResources implements RestDirectory{
    final Directory impl = new JavaDirectories(true);

    @Override
    public FileInfo writeFile(String filename, byte[] data, String userId, String password) {
        var result = impl.writeFile(filename, data, userId, password);
        if( result.isOK() )
            return result.value();
        else
            throw new WebApplicationException(getStatus(result.error())) ;
    }

    @Override
    public void deleteFile(String filename, String userId, String password) {
        var result = impl.deleteFile(filename, userId, password);
        if( !result.isOK() )
            throw new WebApplicationException(getStatus(result.error())) ;
        
    }

    @Override
    public void deleteUser(String userId) {
        var result = impl.deleteUserS(userId);
        if( !result.isOK() )
            throw new WebApplicationException(getStatus(result.error())) ;
        
    }

    @Override
    public void shareFile(String filename, String userId, String userIdShare, String password) {
        var result = impl.shareFile(filename, userId, userIdShare, password);
        if( !result.isOK() )
            throw new WebApplicationException(getStatus(result.error())) ;
        
    }

    @Override
    public void unshareFile(String filename, String userId, String userIdShare, String password) {
        var result = impl.unshareFile(filename, userId, userIdShare, password);
        if( !result.isOK() )
            throw new WebApplicationException(getStatus(result.error())) ;
        
    }

    @Override
    public byte[] getFile(String filename, String userId, String accUserId, String password) {
        var result = impl.getFile(filename, userId, accUserId, password);
        if( !result.isOK() )
            throw new WebApplicationException(getStatus(result.error())) ;
        return result.value();
    }

    @Override
    public List<FileInfo> lsFile(String userId, String password) {
        var result = impl.lsFile(userId, password);
        if( !result.isOK() )
            throw new WebApplicationException(getStatus(result.error())) ;
        return result.value();
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
