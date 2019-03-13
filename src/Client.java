import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;

import org.jsoup.Jsoup;
import org.jsoup.select.Elements;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;


/**
 * A class of HTTP/1.1 clients that can load a .html file and embedded images.
 * 
 * @author Marta Bervoets & Joren 's Jongers
 * @version 1.0
 */

public class Client {

	public static void main(String[] args) throws URISyntaxException, UnknownHostException, IOException {
		
		Client client;
		
		if (args.length == 2) {
			client = new Client(args[0], args[1]);
		} else {
			client = new Client(args[0], args[1], Integer.parseInt(args[2]));
		}
	
		client.getFile();
		client.getImages("test.html");
				
	}


	/**
	 * Initializes a client object with an HTTP command, a URI, and a port number.
	 *  
	 * @param HTTPCommand
	 * 		  Either HEAD, GET, PUT or POST. 
	 * @param URI
	 * 		  
	 * @param port
	 * 
	 * @throws URISyntaxException
	 * 		   If URI is formatted incorrectly.
	 * @throws IOException 
	 * @throws UnknownHostException 
	 */
	public Client(String HTTPCommand, String URI, int port) throws URISyntaxException, UnknownHostException, IOException {
		
		setURI(new URI(URI));
		setPort(port);
		setCommand(HTTPCommand);
		
	}
	
		
	/**
	 * Initializes a client object with an HTTP command, a URI, and a port number 80.
	 * 
	 * @param HTTPCommand
	 * 		  Either HEAD, GET, PUT or POST. 
	 * @param URI
	 * 
	 * @throws URISyntaxException
	 * 		   If URI is formatted incorrectly.  
	 * @throws IOException 
	 * @throws UnknownHostException 
	 */
	public Client(String HTTPCommand, String URI) throws URISyntaxException, UnknownHostException, IOException {
		
		this(HTTPCommand, URI, 80);
		
	}
	
	
	public void getFile() throws UnknownHostException, IOException {
		getHtmlFile(this.uri.getPath());
		
	}


	/**
	 * Prints the response of a GET HTTP command to the standard output stream 
	 * and saves a file containing the body of the response.
	 * 
	 * @param relativeURI
	 * 		  Path to the required file on the host server.
	 * 
	 * @throws UnknownHostException
	 *         If the host is not reachable.
	 *         
	 * @throws IOException
	 */
	public void getHtmlFile(String relativeURI) throws UnknownHostException, IOException {
		
		// Initialize socket and configure I/O.
		Socket socket = new Socket(getURI().getHost(), getPort());
		DataOutputStream out = new DataOutputStream(socket.getOutputStream());
		BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		
		// Build HTTP/1.1 command
		String httpCommand = buildCommand(relativeURI);
		
		// Send command over socket
		out.writeBytes(httpCommand + "\n");
		
		// Receive header from socket
		String header = "";
		String headerLine = in.readLine();
		while (headerLine.length() != 0) {
			System.out.println(headerLine);
			if (headerLine.contains("Content-Length: "))
				setContentLength(Integer.parseInt(headerLine.replaceAll("Content-Length: ", "")));
			header += headerLine;
			headerLine = in.readLine();
		}
		
		// Print header to standard output stream.
		System.out.println("\n");
		
		// Check whether or not the response is chunked.
		if (header.contains("Transfer-Encoding: chunked")) {
			setChunked();
		} else {
			setNotChunked();
		}
		
		// Check the content type of the response.
		if (header.contains("Content-Type: text/html"))
			processHtml(in);

		socket.close();
	}
	
	/**
	 * Prints the response of a GET HTTP command to the standard output stream 
	 * and saves a file containing the body of the response.
	 * 
	 * @param relativeURI
	 * 		  Path to the required file on the host server.
	 * 
	 * @throws UnknownHostException
	 *         If the host is not reachable.
	 *         
	 * @throws IOException
	 */
	public void getImgFile(String relativeURI) throws UnknownHostException, IOException {
		
		// Initialize socket and configure I/O.
		Socket socket = new Socket(getURI().getHost(), getPort());
		DataOutputStream out = new DataOutputStream(socket.getOutputStream());
		DataInputStream in = new DataInputStream(socket.getInputStream());
		
		// Build HTTP/1.1 command
		String httpCommand = buildCommand(relativeURI);
		
		// Send command over socket
		out.writeBytes(httpCommand + "\n");
		
		// Receive header from socket
		String header = "";
		String headerLine = readLine(in);
		while (headerLine.length() != 0) {
			System.out.println(headerLine);
			if (headerLine.contains("Content-Length: "))
				setContentLength(Integer.parseInt(headerLine.replaceAll("Content-Length: ", "")));
			header += headerLine;
			headerLine = readLine(in);
		}
		
		// Print header to standard output stream.
		System.out.println("\n");
		
		// Check whether or not the response is chunked.
		if (header.contains("Transfer-Encoding: chunked")) {
			setChunked();
		} else {
			setNotChunked();
		}
		
		// Check the content type of the response.
		if (header.contains("Content-Type: image") ) {
			String name = relativeURI.replace("/", "");
			processImg(in, name);
		}


		socket.close();
	}

	private void processImg(DataInputStream in, String name) throws IOException {
		
		File file = new File(name);
		file.createNewFile();
		FileOutputStream out = new FileOutputStream(name);
		byte[] buffer = new byte[contentLength];
		out.write(buffer, 0, in.read(buffer));
		in.close();
		out.close();	
	}


	/**
	 * Reads a single line from a DataInputStream.
	 * @throws IOException 
	 */
	private String readLine(DataInputStream in) throws IOException {
		
		char current = (char) in.readByte();
		String res = "";
		while (current != '\r') {
			res += current;
			current = (char) in.readByte();
		}
		in.readByte();
		return res;
		
	}
	
	
	/**
	 * Prints the body of an HTTP/1.1 response to the standard output stream and writes it to a text file.
	 * @param in
	 * 		  The BufferedReader connected to the socket.
	 * @throws IOException
	 */
	private void processHtml(BufferedReader in) throws IOException {
		
		String body = "";
		if (!isChunked()) {
			body += readChunk(in, getContentLength()-1);	
		} else {
			int chunkLength = Integer.parseInt(in.readLine(), 16);
			while (chunkLength != 0) {
				body += readChunk(in, chunkLength+2);
				chunkLength = Integer.parseInt(in.readLine(), 16);
			}
			setNotChunked();
		}
		// Print body to standard output stream.
		System.out.println(body); 
		
		// Write body of response to a file that is named after its relative URI.
		PrintWriter writer = new PrintWriter("test.html", "UTF-8");
		writer.write(body);
		writer.close();	
	}

	
	/**
	 * Builds a correctly formatted HTTP/1.1 command to be send over a socket.
	 *  
	 * @param relativeURI
	 *        the relative URI of the required resource.
	 *        
	 * @return String
	 * 		   A correctly formatted HTTP/1.1 command.
	 */
	private String buildCommand(String relativeURI) {
		String command = this.command + " " + relativeURI + " " + "HTTP/1.1" +"\n";
		command += "Host: " + getURI().getHost() + "\n" + "\n";
		
		return command;
	}

	public void getImages(String nameOfHtmlFile) throws UnknownHostException, IOException {
	
		// Get all PNG files from html file.
		File input = new File(nameOfHtmlFile);
		Document doc = Jsoup.parse(input, "UTF-8", "http://" + getURI().getHost() + "/");
		Elements imgs = doc.select("img[src$=.png]");
		imgs.addAll(doc.select("img[src$=.jpg]"));
		imgs.addAll(doc.select("img[src$=.jpeg]"));
		for (Element element : imgs) {
			String relativeURI = element.attr("src");
			getImgFile(relativeURI);
		}	
	}
	
	/**
	 * Reads a chunk of characters of the given length from a BufferedReader.
	 * 
	 * @param in
	 * @param length
	 * @return
	 * @throws IOException
	 */
	private String readChunk(BufferedReader in, int length) throws IOException {
		String chunk = "";
		for (int i = 0; i<length; i++) {
			chunk += (char) in.read();
		}
		return chunk;
		
	}
	
	/**
	 * A Field containing the URI of this Client.
	 */
	private URI uri;
	
	private void setURI(URI uri) {
		this.uri = uri;
	}
	
	public URI getURI() {
		return this.uri;
	}
	
	
	/**
	 * A field containing the command that this client has to process.
	 */
	private String command;
	
	private void setCommand(String command) {
		this.command = command;
	}
	
	public String getCommand() {
		return this.command;
	}
	
	
	/**
	 * A field containing the port at which this client accesses the host.
	 */
	private int port;
	
	private void setPort(int port) {
		this.port = port;
	}
	
	public int getPort() {
		return this.port;
	}
	
	
	/**
	 * A boolean that denotes whether or not the response of a 
	 * 'GET' HTTP command is chunked or not.
	 */
	private Boolean chunked = false;
	
	private void setChunked() {
		this.chunked = true;
	}
	
	private void setNotChunked() {
		this.chunked = false;
	}
	
	public boolean isChunked() {
		return this.chunked;
	}

	/**
	 * An integer storing the content length of a non chunked HTTP response.
	 */
	private int contentLength = 0;


	private int getContentLength() {
		return this.contentLength;
	}
	
	private void setContentLength(int length) {
		this.contentLength = length;
	}

}