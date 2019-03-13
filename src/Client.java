
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;

import javax.imageio.ImageWriter;

import org.jsoup.Jsoup;
import org.jsoup.select.Elements;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;


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
	
	public void getHtmlFile(String relativeURI) throws UnknownHostException, IOException {
		
		String host = uri.getHost();
		String path = relativeURI;
		Socket socket = new Socket(host, port);
		DataOutputStream out = new DataOutputStream(socket.getOutputStream());
		BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		
		
		
		// Send command over socket
		String command = this.command + " " + path + " " + "HTTP/1.1" +"\n";
		command += "Host: " + host + "\n" + "\n";
		
		out.writeBytes(command + "\n");
		
		// Receive header from socket
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
				body += readChunk(in, contentLength-3);
			
		} else {
			int chunkLength = Integer.parseInt(in.readLine(), 16);
			while (chunkLength != 0) {
				body += readChunk(in, chunkLength);
				chunkLength = Integer.parseInt(in.readLine(), 16);
			}
			chunked = false;
		}

		
		if (body.substring(1,4).equals("PNG")) {
			System.out.println(body);
			PrintWriter writer = new PrintWriter("afbeelding.png");
			writer.write(body);
			writer.close();
		} else {
		this.html = body;
		PrintWriter writer = new PrintWriter("test.html", "UTF-8");
		writer.write(body);
		writer.close();
		}
		
		socket.close();	
	}
	
	public void getHtmlFile() throws UnknownHostException, IOException {
		getHtmlFile(this.uri.getPath());
		
	}
	
	
	public void getImages(String pathOfHtmlFile) throws UnknownHostException, IOException {
	
		Document doc = Jsoup.parse(this.html, "http://" + this.uri.getHost());
		Elements pngs = doc.select("img[src$=.png]");
		
		for (Element element : pngs) {
			String relativeURI = "";
			relativeURI = element.attr("src");
			getHtmlFile(relativeURI);
		}
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
	
		client.getHtmlFile("/");
		client.getImages("/test.html");
				
	}
	
	private URI uri;
	private String command;
	private int port;
	private Boolean chunked = false;
	private String html;
}