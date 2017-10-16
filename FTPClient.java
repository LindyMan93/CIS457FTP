import java.io.*; 
import java.net.*;
import java.util.*;
import java.text.*;
import java.lang.*;
import javax.swing.*;
import java.io.OutputStream;

class FTPClient { 

    public static void main(String argv[]) throws Exception 
    { 
        String sentence; 
        boolean isOpen = true;
        int number=1;
        boolean notEnd = true;
	    String statusCode;
	    boolean clientgo = true;
        int port, port1;
        Socket ControlSocket;
	    
	
	    BufferedReader inFromUser = 
        new BufferedReader(new InputStreamReader(System.in)); 
        sentence = inFromUser.readLine();
        StringTokenizer tokens = new StringTokenizer(sentence);


	   if(sentence.startsWith("connect")){
	   String serverName = tokens.nextToken(); // pass the connect command
	   serverName = tokens.nextToken();
	   port1 = Integer.parseInt(tokens.nextToken());
       port = port1+2;
       System.out.println("Connecting to " + serverName + " through port "+ port1);
       try{
       	Socket ControlSocket = new Socket(serverName, port1);
       	System.out.println("Connected");
        System.out.println(ControlSocket);
       } catch (Exception ConnectException) {
       	System.out.println("Incorrect server name or port");
       }	
        
	while(isOpen && clientgo)
        {      

          DataOutputStream outToServer = new DataOutputStream(ControlSocket.getOutputStream()); 
          
	  DataInputStream inFromServer = new DataInputStream(new BufferedInputStream(ControlSocket.getInputStream()));
          

    	  String command = inFromUser.readLine();
          StringTokenizer token = new StringTokenizer(command);
          sentence = token.nextToken();
          String fileName = token.nextToken();
        System.out.println(fileName);
        if(sentence.equals("list:"))
        {
	    ServerSocket welcomeData = new ServerSocket(port);
    	    outToServer.writeBytes (port + " " + sentence + " " + '\n');

	    Socket dataSocket = welcomeData.accept(); 
 	    DataInputStream inData = new DataInputStream(new BufferedInputStream(dataSocket.getInputStream()));
            while(notEnd) 
            {
                try{
                String modifiedSentence = inData.readUTF();
                System.out.println(modifiedSentence);
            }
            catch (Exception e){
                notEnd = false;
            }
          // Need work
            }
	

	 welcomeData.close();
	 dataSocket.close();
	 System.out.println("\nWhat would you like to do next: \n retr: file.txt || stor: file.txt  || close");

        }
         else if(sentence.startsWith("retr:"))
        {
        ServerSocket welcomeData = new ServerSocket(port);
        outToServer.writeBytes (port + " " + sentence + " " + '\n');

        Socket dataSocket = welcomeData.accept(); 
        DataInputStream inData = new DataInputStream(new BufferedInputStream(dataSocket.getInputStream()));
        String modifiedSentence = inData.readUTF();
        System.out.println(modifiedSentence);

        welcomeData.close();
        dataSocket.close();
        System.out.println("\nWhat would you like to do next: \n retr: file.txt || stor: file.txt  || close");
        }
        else if(sentence.startsWith("stor:")){
        ServerSocket welcomeData = new ServerSocket(port);
        outToServer.writeBytes (port + " " + sentence + " " + '\n');
        FileOutputStream out = new FileOutputStream(fileName);

        Socket dataSocket = welcomeData.accept(); 
        DataInputStream inData = new DataInputStream(new BufferedInputStream(dataSocket.getInputStream()));

        String modifiedSentence = inData.readUTF();
        System.out.println(modifiedSentence);

        welcomeData.close();
        dataSocket.close();
        System.out.println("\nWhat would you like to do next: \n retr: file.txt || stor: file.txt  || close");
        }
        else if(sentence.startsWith("close")){
            System.exit(1);
        }
    }
}
}

}
