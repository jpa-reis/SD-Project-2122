package tp2.impl.servers.rest;

import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.glassfish.jersey.server.ResourceConfig;

import tp2.api.service.java.Directory;
import tp2.api.service.rest.RestDirectory;
import tp2.impl.clients.rest.RestDirectoryClient;
import tp2.impl.servers.common.InsecureHostnameVerifier;
import tp2.impl.servers.rest.util.GenericExceptionMapper;
import tp2.impl.zookeeper.Zookeeper;
import util.Debug;
import util.IP;
import util.Token;

import javax.net.ssl.HttpsURLConnection;

public class DirectoryRepRestServer extends AbstractRestServer {

    /*
    * criação do servidor -> mandar thread com classe
    * se o servidor morre -> a classe manda o pedido para outro servidor?
    * */
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

        String root = "/directory";
        new DirectoryRepRestServer().start();

        var zk = new Zookeeper("kafka");
        zk.createNode(root, new byte[0], CreateMode.PERSISTENT);
        Charset charset = StandardCharsets.US_ASCII;
        String path = zk.createNode(root + root + "_",
                        charset.encode(serverURI).array(),
                        CreateMode.EPHEMERAL_SEQUENTIAL);

            zk.client().exists("/directory/directory_0000000000", watchedEvent -> {
                if(watchedEvent.getType() == Watcher.Event.EventType.NodeDataChanged){

                }
            });
            //url/___________
            //pedido rest a url
    }
}