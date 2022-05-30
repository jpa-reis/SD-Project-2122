package tp2.server.soap_webServices;

import jakarta.inject.Singleton;
import jakarta.jws.WebService;
import tp2.api.service.soap.FilesException;
import tp2.api.service.soap.SoapFiles;
import tp2.api.service.util.Files;
import tp2.api.service.util.Result.ErrorCode;
import tp2.server.JavaFiles;

@WebService(serviceName=SoapFiles.NAME, targetNamespace=SoapFiles.NAMESPACE, endpointInterface=SoapFiles.INTERFACE)
@Singleton
public class filesWebService implements SoapFiles{

    private static final String CONFLICT = "Conflict";
	private static final String BAD_REQUEST = "Bad Request";
	private static final String NOT_FOUND = "Not found";
	private static final String FORBIDDEN = "Wrong password";

    final Files impl = new JavaFiles();

    @Override
    public byte[] getFile(String fileId, String token) throws FilesException {
        var result = impl.getFile(fileId, token);
        if( result.isOK() )
            return result.value();
        else
            throw new FilesException(getStatus(result.error()));
    }

    @Override
    public void deleteFile(String fileId, String token) throws FilesException {
        var result = impl.deleteFile(fileId, token);
        if( !result.isOK() )
            throw new FilesException(getStatus(result.error()));
        
    }

    @Override
    public void writeFile(String fileId, byte[] data, String token) throws FilesException {
        var result = impl.writeFile(fileId, data, token);
        if( !result.isOK() )
            throw new FilesException(getStatus(result.error()));
        
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
