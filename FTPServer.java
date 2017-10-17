
/*  Title: Project 1 - Server
 * 
 *  Authors: Nathan Lindenbaum
 *           Brendan Nahed
 *           Jacob Geers
 *
 *  Date: 10/15/2017
 *  Class: CIS457 Data Communications
 *   
 *  Notes: This is the server class and threads of the FTP file server. 
 *  It will connect to the server on a know port where it is listening. 
 *  Then it will stay connected on original port and pass data through
 *  temporary connections.
 *   
*/

import java.io.*;
import java.net.*;
import java.io.File;
import java.util.*;
import java.io.OutputStream;

/*
 * FTP server
 */
public class FTPServer {

  private static ServerSocket welcomeSocket;

  /*
   * Main method for initiating the server. This will continue to listen to port
   * 5081 for new connections. Once a connection has been established it will pass
   * it off to the ClientThread class where the rest of the commands will be
   * handled.
   */
  public static void main(String args[]) throws Exception {

    try {

      welcomeSocket = new ServerSocket(5081);

    } catch (IOException e) {

      System.out.println("Error Connecting...");
      System.exit(1);

    }
    while (true) {
      Socket socket = welcomeSocket.accept();
      ClientThread thread = new ClientThread(socket);
      thread.start();
    }

  }
}

/*
 * The ClientThread class extends Thread. This class will be used
 * to handle new connections made to the server. Once the connection
 * is made, it will come to this class where all the commands will be
 * parsed. Also, all the data will flow through here.
 */
class ClientThread extends Thread {

  private Socket clientConn;
  private DataOutputStream outToClient;
  private BufferedReader inFromClient;

  /*
   * Constructor. This method takes in a @param Socket connectionSocket.
   * This will then set clientConn. From here it will try to create new
   * bufferedreaders and dataoutputstreams. 
   */
  public ClientThread(Socket connectionSocket) {

    clientConn = connectionSocket;

    try {

      outToClient = new DataOutputStream(clientConn.getOutputStream());
      inFromClient = new BufferedReader(new InputStreamReader(clientConn.getInputStream()));

    } catch (IOException e) {
      System.out.println("Server line 85"); //test
      e.printStackTrace();
    }
  }
  
  /*
   * This method is used by calling .start() on a new thread. 
   * This will allow the multithreading of the server class. 
   * (Multiple client connections) This method services all the 
   * commands of the server and clients.   
   */
  public void run() {
    File[] listOfFiles = null;
    String nextFile = null;

    try {
      
      /*
       * Going through all the the possible commands. 
       */
      while (true) {

        String fromClient;
        String clientCommand;
        byte[] data;
        String frstln;
        int port;

        fromClient = inFromClient.readLine();
        StringTokenizer tokens = new StringTokenizer(fromClient);
        frstln = tokens.nextToken();
        port = Integer.parseInt(frstln);
        clientCommand = tokens.nextToken();

        /*
         * Case "list:"
         * 
         * This will be entered if the user sends the command to list
         * all the files in the server's directory. It will find all
         * the files in the directory that end with .txt. Then it will
         * close the dataSocket.
         */
        if (clientCommand.equals("list:")) {
          Socket dataSocket = new Socket(clientConn.getInetAddress(), port);
          DataOutputStream dataOutToClient = new DataOutputStream(dataSocket.getOutputStream());
          File folder = new File(System.getProperty("user.dir"));
          listOfFiles = folder.listFiles();
          for (int i = 0; i < listOfFiles.length; i++) {
            String temp = listOfFiles[i] + "";
            if (listOfFiles[i].isFile() && temp.endsWith(".txt")) {
              dataOutToClient.writeUTF("file " + listOfFiles[i].getName());
            } else if (listOfFiles.length == 0) {
              dataOutToClient.writeUTF("No Files Found"); //test
            }
          }

          dataOutToClient.close();
          dataSocket.close();
          System.out.println("Data Socket closed");
        }
        
        try {
          nextFile = tokens.nextToken();
        } catch (NullPointerException e) {
          System.out.println("Error: No File Specified");
        }

        /*
         * Case "retr:"
         * 
         * This method will be entered when the user wants to 
         * return/download a file from the server. It creates input and output
         * streams. Then it will search the directory for the file the client 
         * wants. If it has the file it will send it to them, if not an error
         * message will be sent.
         */
        if (clientCommand.equals("retr:")) {
          Socket dataSocket = new Socket(clientConn.getInetAddress(), port);
          DataOutputStream dataOutToClient = new DataOutputStream(dataSocket.getOutputStream());
          FileInputStream in = null;

          try {
            in = new FileInputStream(nextFile);
            dataOutToClient.writeUTF("200");
            sendFile(in, dataOutToClient);
            in.close();
          } catch (FileNotFoundException e) {
            dataOutToClient.writeUTF("550");
          }

          dataOutToClient.close();
          dataSocket.close();
          System.out.println("Data Socket closed");
        }
        
        /*
         * Case "stor:"
         * 
         * This method will be entered if the user wants to save a file
         * to the server. It will open input and output streams. Then it
         * will try to recieve the file. If the file is recieved a 
         * confirmation will appear. If not there will be an error message.
         */
        if (clientCommand.equals("stor:")) {
          Socket dataSocket = new Socket(clientConn.getInetAddress(), port);
          DataOutputStream dataOutToClient = new DataOutputStream(dataSocket.getOutputStream());
          DataInputStream inData = new DataInputStream(dataSocket.getInputStream());
          File f = new File(nextFile);
          FileOutputStream out = null;
          try {
            out = new FileOutputStream(f);
            System.out.println("Made it stor: ");
            recieveFile(inData, out);
            dataOutToClient.writeUTF("Server Report: File Recieved.");
            out.close();
          } catch (FileNotFoundException e) {
            dataOutToClient.writeUTF("Server error: File Not Recieved.");

          }

          dataOutToClient.close();
          dataSocket.close();
          System.out.println("Data Socket closed");

        }
      }
    } catch (NullPointerException e) {
      System.out.println("Connection with " + clientConn + " Ended");

    } catch (Exception e) {
      System.out.println(e);
    }

  }

  /*
   * This private method is used to send a file to the client. It takes as
   * parameters a FileInputStream fis and a DataOutStream os. While its trying
   * to send the file will update with a message indicating it is still working.
   */
  private static void sendFile(FileInputStream fis, DataOutputStream os) throws Exception {
    byte[] buffer = new byte[1024];
    int bytes = 0;

    while ((bytes = fis.read(buffer)) != -1) {
      System.out.println("Sending File...");
      os.write(buffer, 0, bytes);
    }
  }

  /*
   * This private method is used to recieve a file to the client. It takes as
   * parameters a DataOutStream os and a FileInputStream fis. While its trying
   * to recieve the file will update with a message indicating it is still working.
   */
  private static void recieveFile(DataInputStream dis, FileOutputStream os) throws Exception {
    byte[] buffer = new byte[1024];
    int bytes;

    while ((bytes = dis.read(buffer)) != -1) {
      System.out.println("Recieving File...");
      os.write(buffer, 0, bytes);
    }
  }
}