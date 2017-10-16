import java.io.*; 
import java.net.*;
import java.io.File;
import java.util.*;
import java.io.OutputStream;



class FTPServer{	
    
            
    public static void main(String args[]) throws Exception {
      while(true){
      ServerSocket welcomeSocket = new ServerSocket(12000);

      Socket socket = welcomeSocket.accept();
      Thread(socket);
      welcomeSocket.close();
    }

    }

    public static void Thread(Socket connectionSocket){
    File[] listOfFiles=null;
    String nextFile = null;
    try{
     while(true)
      {    

        String fromClient;
        String clientCommand;
        byte[] data;
        String frstln;
        int port;        
   
        DataOutputStream  outToClient = 
        new DataOutputStream(connectionSocket.getOutputStream());

        BufferedReader inFromClient = new BufferedReader(new
        InputStreamReader(connectionSocket.getInputStream()));
            
        fromClient = inFromClient.readLine();
        StringTokenizer tokens = new StringTokenizer(fromClient);
        System.out.println(fromClient);
        frstln = tokens.nextToken();
        port = Integer.parseInt(frstln);
        clientCommand = tokens.nextToken();
        try{
        nextFile = tokens.nextToken();
        System.out.println(nextFile);

      }catch(Exception e){}        
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
          FileInputStream in = null;

          try{
          in = new FileInputStream(nextFile);
          dataOutToClient.writeUTF("Server report: File found.");
          sendFile(in, dataOutToClient);
          in.close();
          }
          catch(FileNotFoundException e){
          dataOutToClient.writeUTF("Server error: File Not found.");
          }
          
          dataOutToClient.close();
          dataSocket.close();
          System.out.println("Data Socket closed");
	     }
       if (clientCommand.equals("stor:")) {
          Socket dataSocket = new Socket(connectionSocket.getInetAddress(), port);
          DataOutputStream dataOutToClient = new DataOutputStream(dataSocket.getOutputStream());
          DataInputStream inData = new DataInputStream(dataSocket.getInputStream());
          File f = new File(nextFile);
          FileOutputStream out = null;
          try{
          out = new FileOutputStream(f);
          System.out.println("Made it stor: ");
          recieveFile(inData, out);
          dataOutToClient.writeUTF("Server Report: File Recieved.");
          out.close();
        }
        catch(FileNotFoundException e){
          dataOutToClient.writeUTF("Server error: File Not Recieved.");

        }
        
          dataOutToClient.close();
          dataSocket.close();
          System.out.println("Data Socket closed");
        
      }
     }
   }catch(Exception e){
    System.out.println(e);
   }

  }

  private static void sendFile(FileInputStream fis, DataOutputStream os) throws Exception {
    byte[] buffer = new byte[1024];
    int bytes = 0;
    
    while ((bytes = fis.read(buffer)) != -1) {
      System.out.println("Sending File...");
      os.write(buffer, 0, bytes);
    }
  }

  private static void recieveFile(DataInputStream dis, FileOutputStream os) throws Exception{
    byte[] buffer = new byte [1024];
    int bytes;
  
    while ((bytes = dis.read(buffer)) != -1) {
      System.out.println("Recieving File...");
      os.write(buffer, 0, bytes);
    }
  }
}    
