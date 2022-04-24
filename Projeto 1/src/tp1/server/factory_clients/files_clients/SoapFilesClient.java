package tp1.server.factory_clients.files_clients;

import java.net.MalformedURLException;
import java.net.URI;

import javax.xml.namespace.QName;

import tp1.api.service.soap.FilesException;
import tp1.api.service.soap.SoapFiles;
import tp1.api.service.util.Files;
import tp1.api.service.util.Result;
import tp1.clients.SoapClient;
import jakarta.xml.ws.Service;
import tp1.api.service.util.Result.ErrorCode;

public class SoapFilesClient extends SoapClient implements Files{
    
    private static final String CONFLICT = "Conflict";
	private static final String BAD_REQUEST = "Bad Request";
	private static final String NOT_FOUND = "Not found";
	private static final String FORBIDDEN = "Wrong password";

    private URI serverURI;

    public SoapFilesClient(URI serverURI){
        super();
        this.serverURI = serverURI;
    }

	@Override
	public Result<Void> writeFile(String fileId, byte[] data, String token) {
		
        return reTry(() -> {
            QName qname = new QName(SoapFiles.NAMESPACE, SoapFiles.NAME);		
            Service service;
            try {
                service = Service.create( URI.create(serverURI + "?wsdl").toURL(), qname);
            } catch (MalformedURLException e1) {
                e1.printStackTrace();
                return null;
            }		
            SoapFiles files = service.getPort(tp1.api.service.soap.SoapFiles.class);
            try {
                files.writeFile(fileId, data, "");
                return Result.ok();
            } catch (FilesException e) {
                return Result.error(getErrorCode(e.getMessage()));
            }
        });


        
	}

	@Override
	public Result<Void> deleteFile(String fileId, String token) {
        return reTry(() -> {
            QName qname = new QName(SoapFiles.NAMESPACE, SoapFiles.NAME);		
            Service service;
            try {
                service = Service.create( URI.create(serverURI+ "?wsdl").toURL(), qname);
            } catch (MalformedURLException e1) {
                e1.printStackTrace();
                return null;
            }		
            SoapFiles files = service.getPort(tp1.api.service.soap.SoapFiles.class);
            
            try {
                files.deleteFile(fileId, "");
                return Result.ok();
            } catch (FilesException e) {
                return Result.error(getErrorCode(e.getMessage()));
            }
		});
	}

	@Override
	public Result<byte[]> getFile(String fileId, String token) {
		return reTry( () -> {
            QName qname = new QName(SoapFiles.NAMESPACE, SoapFiles.NAME);		
            Service service;
            try {
                service = Service.create( URI.create(serverURI + "?wsdl").toURL(), qname);
            } catch (MalformedURLException e1) {
                e1.printStackTrace();
                return null;
            }	

            SoapFiles files = service.getPort(tp1.api.service.soap.SoapFiles.class);

            try {
                return Result.ok(files.getFile(fileId, ""));
            } catch (FilesException e1) {
                return Result.error(getErrorCode(e1.getMessage()));
            }
        });
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
