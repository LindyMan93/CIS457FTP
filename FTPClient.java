import java.io.*;
import java.net.*;
import java.util.*;
import java.text.*;
import java.lang.*;
import javax.swing.*;
import java.io.OutputStream;

class FTPClient {

    public static String options = "\nWhat would you like to do: \n list: to list files \n retr: file.txt || stor: file.txt  || close";

    public static void main(String argv[]) throws Exception {
        String sentence;
        String fileName = null;
        boolean isOpen = true;
        int number = 1;
        boolean notEnd = true;
        String statusCode;
        boolean clientgo = true;
        int port, port1;


        BufferedReader inFromUser =
        new BufferedReader(new InputStreamReader(System.in));

        sentence = inFromUser.readLine();
        StringTokenizer tokens = new StringTokenizer(sentence);

        if(sentence.startsWith("connect")) {

            String serverName = tokens.nextToken(); // pass the connect command
            serverName = tokens.nextToken();
            System.out.println(serverName);
            port1 = Integer.parseInt(tokens.nextToken());
            port = port1+2;
            System.out.println("Connecting to " + serverName + " through port "+ port1);
            Socket ControlSocket = new Socket(serverName, port1);
            System.out.println("Connected");
            System.out.println(options);

            while(isOpen && clientgo) {

                DataOutputStream outToServer = new DataOutputStream(ControlSocket.getOutputStream());
                DataInputStream inFromServer = new DataInputStream(new BufferedInputStream(ControlSocket.getInputStream()));
                ServerSocket welcomeData = new ServerSocket(port);

                String command = inFromUser.readLine();
                StringTokenizer token = new StringTokenizer(command);
                sentence = token.nextToken();

                try {
                    fileName = token.nextToken();
                }

                catch(Exception e) { }

                if(sentence.equals("list:")) {

                    outToServer.writeBytes (port + " " + sentence + " " + '\n');

                    Socket dataSocket = welcomeData.accept();
                    DataInputStream inData = new DataInputStream(new BufferedInputStream(dataSocket.getInputStream()));
                    while(notEnd) {

                        try {

                            String modifiedSentence = inData.readUTF();
                            System.out.println(modifiedSentence);

                        }

                        catch (Exception e) {
                            notEnd = false;
                        }
                        // Need work
                    }

                    // welcomeData.close();
                    dataSocket.close();
                    System.out.println(options);

                }

                else if(sentence.startsWith("retr:")) {
                    // ServerSocket welcomeData = new ServerSocket(port);
                    outToServer.writeBytes (port + " " + sentence + " " + fileName+'\n');
                    Socket dataSocket = welcomeData.accept();
                    DataInputStream inData = new DataInputStream(new BufferedInputStream(dataSocket.getInputStream()));
                    FileOutputStream out = null;
                    boolean fileExists = true;
                    try {
                        out = new FileOutputStream(fileName);
                    }
                    catch(FileNotFoundException e) {
                        System.out.println("Client error: File Not Recieved.");
                        fileExists = false;
                    }

                    if(fileExists) {
                        recieveFile(inData, out);

                        //String modifiedSentence = inData.readLine();
                        System.out.println("File Recieved");
                    }
                    out.close();
                    // welcomeData.close();
                    dataSocket.close();
                    System.out.println(options);

                }

                else if(sentence.startsWith("stor:")) {

                    // ServerSocket welcomeData = new ServerSocket(port);
                    outToServer.writeBytes (port + " " + sentence + " " +fileName+ '\n');
                    boolean fileExists = true;

                    Socket dataSocket = welcomeData.accept();
                    DataInputStream inData = new DataInputStream(new BufferedInputStream(dataSocket.getInputStream()));
                    DataOutputStream outData = new DataOutputStream(dataSocket.getOutputStream());

                    FileInputStream in = null;
                    try {
                        in = new FileInputStream(fileName);
                        System.out.println("Client report: File found.");
                    }

                    catch(FileNotFoundException e) {
                        System.out.println("Client error: File Not found.");
                        fileExists = false;
                    }

                    if(fileExists) {
                        sendFile(in, outData);
                        // String modifiedSentence = inData.readUTF();
                        // System.out.println(modifiedSentence);
                    }

                    in.close();
                    // welcomeData.close();
                    dataSocket.close();
                    System.out.println(options);
                }

                else if(sentence.startsWith("close")) {
                    System.exit(1);
                }
            }
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

    private static void recieveFile(DataInputStream dis, FileOutputStream os) throws Exception {
        byte[] buffer = new byte [1024];
        int bytes;

        while ((bytes = dis.read(buffer)) != -1) {
            System.out.println("Recieving File...");
            os.write(buffer, 0, bytes);
        }
    }

}
