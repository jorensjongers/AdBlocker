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
		String response = in.readLine();
		int contentLength = 0;
		while (response.length() != 0) {
			System.out.println(response);
			response = in.readLine();
			if (response.contains("Transfer-Encoding: chunked")) 
				this.chunked = true;	
			// Save content-length.
			if (response.contains("Content-Length: "))
				contentLength = Integer.parseInt(response.replaceAll("Content-Length: ", ""));
		}
	
		// print empty line between header and body
		System.out.println("\n");
		
		String body = "";
		if (!chunked) {
			for (int i = 0; i<contentLength; i++) {
				body += readChunk(in, contentLength);
			}
			
		} else {
			int chunkLength = Integer.parseInt(in.readLine(), 16);
			System.out.println(chunkLength);
			while (chunkLength != 0) {
				body += readChunk(in, chunkLength);
				chunkLength = Integer.parseInt(in.readLine(), 16);	
			}
		}
		
		// GIT TEST // TEST TEST // TEST TEST TEST
		writer.write(body);
		writer.close();
		socket.close();
		
		
	}
	
	private String readChunk(BufferedReader in, int length) throws IOException {
		String chunk = "";
		for (int i = 0; i<length+2; i++) {
			chunk += (char) in.read();
		}
		return chunk;
		
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
	private Boolean chunked = false;
	
}