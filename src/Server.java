
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URISyntaxException;
import java.net.UnknownHostException;

/**
 * A class that creates a server and spawns a thread when connection is made.
 * 
 * 
 * @author Marta Bervoets & Joren 's Jongers
 *
 */
public class Server implements Runnable {

	// Sets the server port to the given variable port.
	public Server(int port){
	    this.serverPort = port;
	}

	// Main method to be run.
	public void run(){
		
		// Sets the running thread to the current thread.
	    this.runningThread = Thread.currentThread();
	    
	    // Creates a socket that the given port.
	    try {
            this.serverSocket = new ServerSocket(this.serverPort);
        } catch (IOException e) {
            throw new RuntimeException("Cannot open port " + this.serverPort, e);
        }
	    
	    // While the server is not stopped,
	    // try connecting to a client.
	    while(! isStopped()){
	        Socket clientSocket = null;
	        try {
	            clientSocket = this.serverSocket.accept();
	        } catch (IOException e) {
	            if(isStopped()) {
	                System.out.println("Server Stopped.") ;
	                return;
	            }
	            throw new RuntimeException(
	                "Error accepting client connection", e);
	        }
	        
	        // Spawns a new thread.
	        new Thread(new ServerThread(clientSocket, "Multithreaded Server")).start();
	    }
	    System.out.println("Server Stopped.") ;
	}

    private boolean isStopped() {
        return this.isStopped;
    }

    public void stop(){
        this.isStopped = true;
        try {
            this.serverSocket.close();
        } catch (IOException e) {
            throw new RuntimeException("Error closing server", e);
        }
    }


	public static void main(String[] args) throws URISyntaxException, UnknownHostException, IOException {
		
		// Creates a new server at port 9000.
		Server server = new Server(9000);
		
		// start calls the run method of this server
		new Thread(server).start();

		try {
		    Thread.sleep(20 * 1000);
		} catch (InterruptedException e) {
		    e.printStackTrace();
		}
		System.out.println("Stopping Server");
		server.stop();
			
	}
	
	/*
	 * Initializes the server port to 8080.
	 */
	int serverPort = 8080;
	
	/*
	 * Initializes the server socket to null.
	 */
	ServerSocket serverSocket = null;
	
	/*
	 * Initializes isStopped, whether the connection is stopped or not, at false.
	 */
	boolean isStopped = false;
	
	/**
	 * Initializes the running thread to null.
	 */
	Thread runningThread = null;

}



