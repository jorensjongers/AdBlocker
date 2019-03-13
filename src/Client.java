import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;

/**
 * 
 * @author Marta Bervoets & Joren 's Jongers
 *
 */

public class Client {

	public Client(String HTTPCommand, String URI, int port) throws URISyntaxException {
		
		this.uri = new URI(URI);
		this.port = port;
		this.command = HTTPCommand;
		
	}
	
	public Client(String HTTPCommand, String URI) throws URISyntaxException {
		
		this(HTTPCommand, URI, 80);
		
	}
	
	public void processCommand() throws UnknownHostException, IOException {
		
		String host = uri.getHost();
		String path = uri.getPath();
		Socket socket = new Socket(host, port);
		DataOutputStream out = new DataOutputStream(socket.getOutputStream());
		BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		PrintWriter writer = new PrintWriter("test.html", "UTF-8");
		
		
		
		// Send command over socket
		String command = this.command + " " + path + " " + "HTTP/1.1" +"\n";
		command += "Host: " + host + "\n" + "\n";
		
		out.writeBytes(command + "\n");
		
		// Receive response from socket
		while (true) {
			String response = in.readLine();
			System.out.println(response);
			if (response.length() == 0) 
			     break;
		}
		
		@SuppressWarnings("unused")
		String resp = in.readLine();
		
		
		//int chunk_length = Integer.parseInt(in.readLine(), 16);
		//System.out.println(chunk_length);
		

		writer.close();
		socket.close();
		
		
	}
	
	public static void main(String[] args) throws URISyntaxException, UnknownHostException, IOException {
		
		Client client;
		
		if (args.length == 2) {
			client = new Client(args[0], args[1]);
		} else {
			client = new Client(args[0], args[1], Integer.parseInt(args[2]));
		}
	
		client.processCommand();
				
	}
	
	private URI uri;
	private String command;
	private int port;
	
	
}