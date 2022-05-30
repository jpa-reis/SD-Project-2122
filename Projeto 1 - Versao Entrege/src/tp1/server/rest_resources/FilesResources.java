package tp1.server.rest_resources;
import jakarta.inject.Singleton;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response.Status;
import tp1.api.service.rest.RestFiles;
import tp1.api.service.util.Files;
import tp1.api.service.util.Result.ErrorCode;
import tp1.server.JavaFiles;

@Singleton
public class FilesResources implements RestFiles{

    final Files impl = new JavaFiles();

    @Override
    public void writeFile(String fileId, byte[] data, String token) {
        var result = impl.writeFile(fileId, data, token);
        if( !result.isOK() )
            throw new WebApplicationException(getStatus(result.error())) ;
        
    }

    @Override
    public void deleteFile(String fileId, String token) {
        var result = impl.deleteFile(fileId, token);
        if( !result.isOK() )
            throw new WebApplicationException(getStatus(result.error())) ;
        
    }

    @Override
    public byte[] getFile(String fileId, String token) {
        var result = impl.getFile(fileId, token);
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
