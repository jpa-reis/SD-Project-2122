package tp1.server.factory_clients.directory_clients;

import tp1.api.service.soap.SoapDirectory;
import tp1.api.service.util.Directory;
import tp1.clients.SoapClient;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.List;

import javax.xml.namespace.QName;

import jakarta.xml.ws.Service;
import tp1.api.FileInfo;
import tp1.api.service.util.Result;

public class SoapDirectoryClient extends SoapClient implements Directory{

    private URI serverURI;

    protected SoapDirectoryClient(URI serverURI) {
        super();
        this.serverURI = serverURI;
    }

    @Override
    public Result<FileInfo> writeFile(String filename, byte[] data, String userId, String password) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Result<Void> deleteFile(String filename, String userId, String password) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Result<Void> shareFile(String filename, String userId, String userIdShare, String password) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Result<Void> unshareFile(String filename, String userId, String userIdShare, String password) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Result<byte[]> getFile(String filename, String userId, String accUserId, String password) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Result<List<FileInfo>> lsFile(String userId, String password) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Result<Void> deleteUserReferences(String userId) {
        reTry(() -> {
            QName qname = new QName(SoapDirectory.NAMESPACE, SoapDirectory.NAME);		
            Service service;
            try {
                service = Service.create( URI.create(serverURI+ "?wsdl").toURL(), qname);
            } catch (MalformedURLException e1) {
                e1.printStackTrace();
                return null;
            }	

            SoapDirectory directory = service.getPort(tp1.api.service.soap.SoapDirectory.class);
            directory.deleteUserS(userId);
            return null;
        });
        return Result.ok();
    }
    
}
