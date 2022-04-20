package tp1.server.soap;

import java.net.InetAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.xml.ws.Endpoint;

public class SOAPDirServer {
    public static final int PORT = 8080;
	public static final String SERVICE_NAME = "directory";
	public static String SERVER_BASE_URI = "http://%s:%s/soap";

	private static Logger Log = Logger.getLogger(SOAPDirServer.class.getName());

	public static void main(String[] args) throws Exception {
		
		System.setProperty("com.sun.xml.ws.transport.http.client.HttpTransportPipe.dump", "true");
		System.setProperty("com.sun.xml.internal.ws.transport.http.client.HttpTransportPipe.dump", "true");
		System.setProperty("com.sun.xml.ws.transport.http.HttpAdapter.dump", "true");
		System.setProperty("com.sun.xml.internal.ws.transport.http.HttpAdapter.dump", "true");

		Log.setLevel(Level.INFO);

		String ip = InetAddress.getLocalHost().getHostAddress();
		String serverURI = String.format(SERVER_BASE_URI, ip, PORT);

		Endpoint.publish(serverURI, new SoapDirWebService());

		Log.info(String.format("%s Soap Server ready @ %s\n", SERVICE_NAME, serverURI));
	}
}
