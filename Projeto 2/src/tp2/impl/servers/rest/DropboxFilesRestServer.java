package tp2.impl.servers.rest;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Verb;
import com.google.gson.Gson;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.glassfish.jersey.server.ResourceConfig;

import org.pac4j.scribe.builder.api.DropboxApi20;
import tp2.api.service.java.Files;
import tp2.impl.servers.common.InsecureHostnameVerifier;
import tp2.impl.servers.rest.records.DeleteFileArgs;
import tp2.impl.servers.rest.util.GenericExceptionMapper;
import tp2.impl.servers.zookeeper.Zookeeper;
import util.Debug;


import javax.net.ssl.HttpsURLConnection;

public class DropboxFilesRestServer extends AbstractRestServer {
	public static final int PORT = 5678;
	private static Logger Log = Logger.getLogger(DropboxFilesRestServer.class.getName());
	private static Zookeeper zk;
	
	DropboxFilesRestServer() throws Exception {
		super(Log, Files.SERVICE_NAME, PORT);
	}
	
	@Override
	void registerResources(ResourceConfig config) {
		config.register( DropboxFilesResources.class );
		config.register( GenericExceptionMapper.class );
	}
	
	public static void main(String[] args) throws Exception {

		Debug.setLogLevel( Level.INFO, Debug.TP1);
		
		boolean flag = Boolean.parseBoolean(args[0]);

		HttpsURLConnection.setDefaultHostnameVerifier(new InsecureHostnameVerifier());

		new DropboxFilesRestServer().start();
		zk = new Zookeeper("kafka", flag);
		Thread.sleep(Integer.MAX_VALUE);
	}	
}