
/*  Title: Project 1 - Client
 * 
 *  Authors: Nathan Lindenbaum
 *           Brendan Nahed
 *           Jacob Geers
 *
 *  Date: 10/15/2017
 *  Class: CIS457 Data Communications
 *   
 *  Notes: This is the client class of the FTP file server. It will
 *  connect to the server on a know port where it is listening. Then 
 *  it will stay connected on original port and pass data through
 *  temporary connections.
 *   
*/
import java.io.*;
import java.net.*;
import java.util.*;
import java.text.*;
import java.lang.*;
import javax.swing.*;
import java.io.OutputStream;

/*
 * This class will handle all commands and requests directly from the
 * client.
 */
class FTPClient {

    public static String options = "\nWhat would you like to do: \n list: to list files \n retr: file.txt || stor: file.txt  || close";

    /*
     * This main method will create buffered readers and tokenize the 
     * clients input so that it can send the information correctly to the
     * server.
     */
    public static void main(String argv[]) throws Exception {
        String sentence;
        String fileName = null;
        boolean isOpen = true;
        int number = 1;
        String statusCode;
        boolean clientgo = true;
        int port, port1;
        String splashScreen1 = "\n-----Simple FTP-----\n \nActions-------------\nlist:(Lists server directory)";
        String splashScreen2 = "\nstor:(Saves file on directory)\nretr: (Downloads file from server \nquit: (Quit)";
        String splashScreen3 = "\n-------------------- \n";
        System.out.print(splashScreen1 + splashScreen2 + splashScreen3);

        BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));

        sentence = inFromUser.readLine();
        StringTokenizer tokens = new StringTokenizer(sentence);

        /*
         * This if statement is required to connect to the server. It
         * ensures that user started their command with "connect", then
         * it will take the next pieces of the argument and try to connect
         * to a server with either that name or ip address at the specified
         * port.
         */
        if (sentence.startsWith("connect")) {

            String serverName = tokens.nextToken(); // pass the connect command
            serverName = tokens.nextToken();
            port1 = Integer.parseInt(tokens.nextToken());
            System.out.println("Connecting to " + serverName + " through port " + port1);
            Socket ControlSocket = new Socket(serverName, port1);
            System.out.println("Connected");
            System.out.println(options);

            /*
             * Running while the connection is open and client is ready.
             */
            while (isOpen && clientgo) {

                DataOutputStream outToServer = new DataOutputStream(ControlSocket.getOutputStream());
                DataInputStream inFromServer = new DataInputStream(
                        new BufferedInputStream(ControlSocket.getInputStream()));

                String command = inFromUser.readLine();
                StringTokenizer token = new StringTokenizer(command);
                sentence = token.nextToken();

             /*   try {
                    fileName = token.nextToken();

                } catch (NoSuchElementException e) {
                    System.out.println("Invalid Argument"); //test
                    fileName = "";
                    // should restart while loop
                }*/

                /*
                 * Case "list:"
                 * 
                 * This will be entered if the user wants to see what is currently
                 * in the servers directory. It will connect to initial port + 2. 
                 * Then it will send the server what port it is listening on and what
                 * it is asking for back. This is where the Case "list:" in the server
                 * class will run. It will then read the data from the buffer and
                 * display it to the client.
                 */
                if (sentence.equals("list:")) {

                    boolean notEnd = true;
                    port = port1 + 2;
                    ServerSocket welcomeData = new ServerSocket(port);
                    outToServer.writeBytes(port + " " + sentence + " " + '\n');

                    Socket dataSocket = welcomeData.accept();
                    DataInputStream inData = new DataInputStream(new BufferedInputStream(dataSocket.getInputStream()));
                    while (notEnd) {

                        try {

                            String modifiedSentence = inData.readUTF();
                            System.out.println(modifiedSentence);

                        }

                        catch (Exception e) {
                            notEnd = false;
                        }
                        // Need work
                    }
                    inData.close();
                    welcomeData.close();
                    dataSocket.close();
                    System.out.println(options);

                }
                if (!(sentence.equals("list:"))){
                    try {
                        fileName = token.nextToken();

                    } catch (NoSuchElementException e) {
                        System.out.println("Invalid Argument"); //test
                    } 
                }

                /*
                 * Case "retr:"
                 * 
                 * This will download a file from the server. It will 
                 * send the server which file it wants to download and
                 * listen to see if the server is sending anything back.
                 */
                if (sentence.startsWith("retr:")) {
                    port = port1 + 2;
                    ServerSocket welcomeData = new ServerSocket(port);
                    outToServer.writeBytes(port + " " + sentence + " " + fileName + '\n');
                    Socket dataSocket = welcomeData.accept();
                    DataInputStream inData = new DataInputStream(new BufferedInputStream(dataSocket.getInputStream()));
                    FileOutputStream out = null;
                    boolean fileExists = true;
                    try {
                        out = new FileOutputStream(fileName);

                    } catch (FileNotFoundException e) {
                        System.out.println("Client error: File Not Recieved.");
                        fileExists = false;

                    } catch (NullPointerException e) {
                        System.out.println("No File Specified"); //test
                        //not neccesary if line 90 can restart whle loop
                    }

                    if (fileExists) {
                        recieveFile(inData, out);

                        // String modifiedSentence = inData.readLine();
                        System.out.println("File Recieved");
                    }
                    out.close();
                    welcomeData.close();
                    dataSocket.close();
                    System.out.println(options);

                }

                /*
                 * This will be ran when the user wants to save a file on the
                 * server. It will create input and output streams where the file 
                 * will be sent through. If the file does not exist in the Client's
                 * directory it will spit out an error message.
                 */
                else if (sentence.startsWith("stor:")) {
                    port = port1 + 2;
                    ServerSocket welcomeData = new ServerSocket(port);
                    outToServer.writeBytes(port + " " + sentence + " " + fileName + '\n');
                    boolean fileExists = true;

                    Socket dataSocket = welcomeData.accept();
                    DataInputStream inData = new DataInputStream(new BufferedInputStream(dataSocket.getInputStream()));
                    DataOutputStream outData = new DataOutputStream(dataSocket.getOutputStream());

                    FileInputStream in = null;
                    try {
                        in = new FileInputStream(fileName);
                        System.out.println("Client report: File found.");
                    }

                    catch (FileNotFoundException e) {
                        System.out.println("Client error: File Not found.");
                        fileExists = false;
                    }

                    if (fileExists) {
                        sendFile(in, outData);
                        // String modifiedSentence = inData.readUTF();
                        // System.out.println(modifiedSentence);
                    }

                    in.close();
                    welcomeData.close();
                    dataSocket.close();
                    System.out.println(options);
                }

                /*
                 * Case "close"
                 * 
                 * This will close the connection to the server and stop
                 * running the FTPClient program.
                 */
                else if (sentence.startsWith("close")) {
                    System.exit(1);
                }
            }
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