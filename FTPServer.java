import java.io.*;
import java.net.*;
import java.io.File;
import java.util.*;
import java.io.OutputStream;



public class FTPServer
{

    // private static final int PORT = 12000;
    private static ServerSocket welcomeSocket;

    public static void main(String args[]) throws Exception
    {
        try{

            welcomeSocket = new ServerSocket(5081);

        } catch (IOException e){

            System.out.println("Error Connecting...");
            System.exit(1);

        }
        while(true) {
            Socket socket = welcomeSocket.accept();
            // welcomeSocket.close();
            // System.out.println("Closed welcome connection");
            ClientThread thread = new ClientThread(socket);
            thread.start();
            //new ClientThread(socket).start();
        }

    }
}

class ClientThread extends Thread
{

    private Socket clientConn;
    private DataOutputStream  outToClient;
    private BufferedReader inFromClient;

    public ClientThread(Socket connectionSocket)
    {

        clientConn = connectionSocket;

        try
        {

            outToClient = new DataOutputStream(clientConn.getOutputStream());
            inFromClient = new BufferedReader(new InputStreamReader(clientConn.getInputStream()));

        }
        catch (IOException e)
        {

            e.printStackTrace();

        }
    }

        public void run()
        {
            File[] listOfFiles = null;
            String nextFile = null;

            try
            {
                while(true)
                {

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
                    try
                    {
                        nextFile = tokens.nextToken();
                    }
                    catch(Exception e) {}

                    if(clientCommand.equals("list:"))
                    {
                        Socket dataSocket = new Socket(clientConn.getInetAddress(), port);
                        DataOutputStream  dataOutToClient = new DataOutputStream(dataSocket.getOutputStream());
                        File folder = new File(System.getProperty("user.dir"));
                        listOfFiles = folder.listFiles();
                        if(!(listOfFiles.length==0)){
                        for (int i = 0; i < listOfFiles.length; i++)
                        {
                            String temp = listOfFiles[i]+"";
                            if (listOfFiles[i].isFile() && temp.endsWith(".txt"))
                            {
                                dataOutToClient.writeUTF("file " + listOfFiles[i].getName());
                            }
                        }
                      }
                      else{
                        dataOutToClient.writeUTF("No files have been stored");

                      }

                        dataOutToClient.close();
                        dataSocket.close();
                        System.out.println("Data Socket closed");
                    }
                    //needs work
                    if(clientCommand.equals("retr:"))
                    {
                        Socket dataSocket = new Socket(clientConn.getInetAddress(), port);
                        DataOutputStream dataOutToClient = new DataOutputStream(dataSocket.getOutputStream());
                        FileInputStream in = null;

                        try
                        {
                            in = new FileInputStream(nextFile);
                            dataOutToClient.writeUTF("Server report: File found.");
                            sendFile(in, dataOutToClient);
                            in.close();
                        }
                        catch(FileNotFoundException e)
                        {
                            dataOutToClient.writeUTF("Server error: File Not found.");
                        }

                        dataOutToClient.close();
                        dataSocket.close();
                        System.out.println("Data Socket closed");
                    }
                    if (clientCommand.equals("stor:"))
                    {
                        Socket dataSocket = new Socket(clientConn.getInetAddress(), port);
                        DataOutputStream dataOutToClient = new DataOutputStream(dataSocket.getOutputStream());
                        DataInputStream inData = new DataInputStream(dataSocket.getInputStream());
                        File f = new File(nextFile);
                        FileOutputStream out = null;
                        try
                        {
                            out = new FileOutputStream(f);
                            recieveFile(inData, out);
                            dataOutToClient.writeUTF("Server Report: File Recieved.");
                            out.close();
                        }
                        catch(FileNotFoundException e)
                        {
                            dataOutToClient.writeUTF("Server error: File Not Recieved.");

                        }

                        dataOutToClient.close();
                        dataSocket.close();
                        System.out.println("Data Socket closed");

                    }
                }
            }
            catch(Exception e)
            {
                System.out.println(e);
            }

        }

    private static void sendFile(FileInputStream fis, DataOutputStream os) throws Exception
    {
        byte[] buffer = new byte[1024];
        int bytes = 0;

        while ((bytes = fis.read(buffer)) != -1)
        {
            System.out.println("Sending File...");
            os.write(buffer, 0, bytes);
        }
    }

    private static void recieveFile(DataInputStream dis, FileOutputStream os) throws Exception
    {
        byte[] buffer = new byte [1024];
        int bytes;

        while ((bytes = dis.read(buffer)) != -1)
        {
            System.out.println("Recieving File...");
            os.write(buffer, 0, bytes);
        }
    }
}