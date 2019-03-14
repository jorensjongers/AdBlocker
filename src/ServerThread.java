
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.Socket;


public class ServerThread implements Runnable{

    protected Socket clientSocket = null;
    protected String serverText   = null;

    public ServerThread(Socket clientSocket, String serverText) {
        this.clientSocket = clientSocket;
        this.serverText   = serverText;
    }
    
    //public void setWebPage() throws IOException {
    //	String records = "";
    //	try {
    //		BufferedReader reader = new BufferedReader(new FileReader("C:\\Users\\Marta\\git\\AdBlocker\\webpage\\index.html"));
    //	    String line;
    //	    while ((line = reader.readLine()) != null) {
    //	    	records += line;
    //	    }
    //	    reader.close();
    //	  } finally {
    //	  }
    //	this.serverText = records;
    	
    //	System.out.println(this.serverText);
  
    //}

    public void run() {
    	//try {
			//this.setWebPage();
		//} catch (IOException e1) {
			// TODO Auto-generated catch block
		//	e1.printStackTrace();
		//}
        try {
            InputStream input  = clientSocket.getInputStream();
            OutputStream output = clientSocket.getOutputStream();
            output.write(("HTTP/1.1 200 OK" +
            				this.serverText + " - " +
            				"").getBytes());
            
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            
            String message = "";
            String response = in.readLine();
            while (response != null) {
            	message += response;
                response = in.readLine();
            }
            	
            output.close();
            input.close();
            System.out.println("hallo");

            System.out.println(message);
        } catch (IOException e) {
            //report exception somewhere.
            e.printStackTrace();
        }
        
    
    }
    

}