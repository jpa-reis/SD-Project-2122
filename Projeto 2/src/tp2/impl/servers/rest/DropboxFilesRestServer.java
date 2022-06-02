package tp2.impl.servers.rest;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.jersey.server.ResourceConfig;

import tp2.api.service.java.Files;
import tp2.impl.servers.common.InsecureHostnameVerifier;
import tp2.impl.servers.rest.util.GenericExceptionMapper;
import util.Debug;
import util.Token;

import javax.net.ssl.HttpsURLConnection;

public class DropboxFilesRestServer extends AbstractRestServer {
	public static final int PORT = 5678;
	
	private static Logger Log = Logger.getLogger(DropboxFilesRestServer.class.getName());

	
	DropboxFilesRestServer() {
		super(Log, Files.SERVICE_NAME, PORT);
	}
	
	@Override
	void registerResources(ResourceConfig config) {
		config.register( DropboxFilesResources.class );
		config.register( GenericExceptionMapper.class );
	}
	
	public static void main(String[] args) throws Exception {

		Debug.setLogLevel( Level.INFO, Debug.TP1);
		
		Token.set( args.length == 0 ? "" : args[0] );

		HttpsURLConnection.setDefaultHostnameVerifier(new InsecureHostnameVerifier());
		new DropboxFilesRestServer().start();
	}	
}