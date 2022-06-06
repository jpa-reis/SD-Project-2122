package tp2.impl.servers.rest;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.zookeeper.CreateMode;
import org.glassfish.jersey.server.ResourceConfig;

import tp2.api.service.java.Directory;
import tp2.impl.servers.common.InsecureHostnameVerifier;
import tp2.impl.servers.rest.util.GenericExceptionMapper;
import tp2.impl.servers.zookeeper.Zookeeper;
import util.Debug;
import util.Token;

import javax.net.ssl.HttpsURLConnection;

public class DirectoryRepRestServer extends AbstractRestServer {

    public static final int PORT = 4566;

    private static Logger Log = Logger.getLogger(DirectoryRepRestServer.class.getName());

    DirectoryRepRestServer() throws Exception {
        super(Log, Directory.SERVICE_NAME, PORT);
    }

    @Override
    void registerResources(ResourceConfig config) {
        config.register( DirectoryRepResources.class );
        config.register( GenericExceptionMapper.class );
    }

    public static void main(String[] args) throws Exception {

        Debug.setLogLevel( Level.INFO, Debug.TP1);

        Token.set( args.length > 0 ? args[0] : "");
        HttpsURLConnection.setDefaultHostnameVerifier(new InsecureHostnameVerifier());
        new DirectoryRepRestServer().start();

        var zk = new Zookeeper("kafka");

        String root = "/directory";

        zk.createNode(root, new byte[0], CreateMode.PERSISTENT);
        zk.createNode(root + root + "_", new byte[0], CreateMode.EPHEMERAL_SEQUENTIAL);
    }
}