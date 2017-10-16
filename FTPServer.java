import java.io.*; 
import java.net.*;
import java.io.File;
import java.util.*;


class FTPServer{	
    
            
    public static void main(String args[]) throws Exception {
      while(true){
      ServerSocket welcomeSocket = new ServerSocket(12000);

      Socket socket = welcomeSocket.accept();
      Thread(socket);
    }

    }

    public static void Thread(Socket connectionSocket){
    File[] listOfFiles=null;
    try{
     while(true)
      {    

        String fromClient;
        String clientCommand, nextFile;
        byte[] data;
        String frstln;
        int port;        
        System.out.println("start");
   
        DataOutputStream  outToClient = 
        new DataOutputStream(connectionSocket.getOutputStream());

        BufferedReader inFromClient = new BufferedReader(new
        InputStreamReader(connectionSocket.getInputStream()));
            
        fromClient = inFromClient.readLine();
        System.out.println(fromClient);
        StringTokenizer tokens = new StringTokenizer(fromClient);
            
        frstln = tokens.nextToken();
        port = Integer.parseInt(frstln);
        clientCommand = tokens.nextToken();
        try{
        nextFile = tokens.nextToken();
        System.out.println(nextFile);
      }catch(Exception e){}
                  
        System.out.println("frstln " + frstln);
        if(clientCommand.equals("list:"))
        {                   
          Socket dataSocket = new Socket(connectionSocket.getInetAddress(), port);
          DataOutputStream  dataOutToClient = 
          new DataOutputStream(dataSocket.getOutputStream());
          //needs work
          //listOfFiles = folder.listFiles();
            for (int i = 0; i < listOfFiles.length; i++) {
              if (listOfFiles[i].isFile()) {
                dataOutToClient.writeUTF("file " + listOfFiles[i].getName());
              }
            }
          
          dataOutToClient.close();
          dataSocket.close();
          System.out.println("Data Socket closed");
        }
			//needs work
      if(clientCommand.equals("retr:"))
      {
          Socket dataSocket = new Socket(connectionSocket.getInetAddress(), port);
          DataOutputStream dataOutToClient = new DataOutputStream(dataSocket.getOutputStream());

          System.out.println(dataOutToClient);

          dataOutToClient.writeUTF("-retr logic-");

          dataOutToClient.close();
          dataSocket.close();
          System.out.println("Data Socket closed");
	     }
       if (clientCommand.equals("stor:")) {

          Socket dataSocket = new Socket(connectionSocket.getInetAddress(), port);
          DataOutputStream dataOutToClient = new DataOutputStream(dataSocket.getOutputStream());

          System.out.println(dataOutToClient);

         dataOutToClient.writeUTF("-stor logic-");

          dataOutToClient.close();
          dataSocket.close();
          System.out.println("Data Socket closed");
        }
     }
   }catch(Exception e){
    System.out.println(e);
   }

  }
}    
