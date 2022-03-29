package tp1.server.files;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;

import tp1.server.Discovery;

public class GetFile {
    
    public static final int PORT = 8080;
	public static final String SERVICE = "FilesService";
	private static final String SERVER_URI_FMT = "http://%s:%s/rest";
	public static void main(String[] args) throws IOException {

		if( args.length != 2) {
			System.err.println( "Use: java tp1.server.files.GetFile fileId token");
			return;
		}
		
		String fileId = args[0];
		String token = args[1];
		
		String ip = InetAddress.getLocalHost().getHostAddress();
		String fileURI = String.format(SERVER_URI_FMT, ip, PORT);
		Discovery listener = new Discovery(Discovery.DISCOVERY_ADDR, SERVICE, fileURI);
		listener.listener();

		while(listener.knownUrisOf(SERVICE).length == 0){
				try {
					System.out.println("Searching for servers...");
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

		}

		URI[] fileServiceURIS = listener.knownUrisOf(SERVICE);

		System.out.println("Sending request to server.");
		
        new RestFileClient(fileServiceURIS[0]).getFile(fileId, token);
	}
}