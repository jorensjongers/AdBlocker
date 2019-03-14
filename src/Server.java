
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URISyntaxException;
import java.net.UnknownHostException;

public class Server implements Runnable {

	public Server(int port){
	    this.serverPort = port;
	}

	public void run(){
	    this.runningThread = Thread.currentThread();
	    try {
            this.serverSocket = new ServerSocket(this.serverPort);
        } catch (IOException e) {
            throw new RuntimeException("Cannot open port " + this.serverPort, e);
        }
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
	        
	        new Thread(new ServerThread(clientSocket, "Multithreaded Server")).start();
	    }
	    System.out.println("Server Stopped.") ;
	}

    private synchronized boolean isStopped() {
        return this.isStopped;
    }

    public synchronized void stop(){
        this.isStopped = true;
        try {
            this.serverSocket.close();
        } catch (IOException e) {
            throw new RuntimeException("Error closing server", e);
        }
    }


	public static void main(String[] args) throws URISyntaxException, UnknownHostException, IOException {
	
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
	
	int serverPort = 8080;
	ServerSocket serverSocket = null;
	boolean isStopped = false;
	Thread runningThread = null;

}



