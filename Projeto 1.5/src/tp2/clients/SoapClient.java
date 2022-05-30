package tp2.clients;


import java.util.function.Supplier;
import java.util.logging.Logger;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.xml.ws.WebServiceException;

public class SoapClient {
	protected static Logger Log = Logger.getLogger(SoapClient.class.getName());

	protected static final int READ_TIMEOUT = 10000;
	protected static final int CONNECT_TIMEOUT = 10000;

	protected static final int RETRY_SLEEP = 1000;
	protected static final int MAX_RETRIES = 3;


	protected final Client client;
	final ClientConfig config;

	protected SoapClient() {
		this.config = new ClientConfig();

		config.property(ClientProperties.READ_TIMEOUT, READ_TIMEOUT);
		config.property( ClientProperties.CONNECT_TIMEOUT, CONNECT_TIMEOUT);
		
		this.client = ClientBuilder.newClient(config);
	}

	protected <T> T reTry(Supplier<T> func) {
		
		for (int i = 0; i < MAX_RETRIES; i++){
			try {
				return func.get();
			} catch (WebServiceException x) {
				Log.fine("WebServiceException: " + x.getMessage());
				sleep(RETRY_SLEEP);
			} catch (Exception x) {
				Log.fine("Exception: " + x.getMessage());
				x.printStackTrace();
				break;
			}
		}
		
		
		return null;
	}

	private void sleep(int ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException x) { // nothing to do...
		}
	}
}
