package com.bgoverseas;

import com.bgoverseas.tftp.TFTPBase;
import com.bgoverseas.tftp.TFTPClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * Created by binwang on 17/08/15.
 * TFTP client used for test
 */
public class TFTPClientTest {
    /**
     * help text for users
     */
    public static final String HELP_TEXT =
            "Usage: TFTPClientTest [options] hostname local_file remote_file\n\r" +
                    "hostname   - The name of the server\n" +
                    "local_file  - The client file name\n" +
                    "remote_file - The server file name\n" +
                    "options: (The default is to assume -r -b)\n" +
                    "\t-s Send a local file\n" +
                    "\t-r Receive a remote file\n" +
                    "\t-a Use Netascii transfer mode\n" +
                    "\t-b Use Octet transfer mode\n";

    /**
     * entry of client test
     * @param args
     */
    public static void main(String[] args) {
        boolean receiveFile = true;//receive a remote file as default
        String mode = TFTPBase.OCTET_MODE;//octet as the default mode
        boolean closed;
        int argCounter;
        String arg, hostname, localFilename, remoteFilename;
        TFTPClient tftpClient;

        // Parse options
        for (argCounter = 0; argCounter < args.length; argCounter++) {
            arg = args[argCounter];
            if (arg.startsWith("-"))
            {
                if (arg.equals("-r")) {
                    receiveFile = true;
                } else if (arg.equals("-s")) {
                    receiveFile = false;
                } else if (arg.equals("-a")) {
                    mode = TFTPBase.NETASCII_MODE;
                } else if (arg.equals("-b")) {
                    mode = TFTPBase.OCTET_MODE;
                } else {
                    System.err.println("Error: wrong option.");
                    System.err.print(TFTPClientTest.HELP_TEXT);
                    System.exit(1);
                }
            } else {
                break;
            }
        }

        //Parse the hostname, local_file, remote_file
        if (args.length - argCounter != 3) {
            System.err.println("Error: There must be three arguments after the options");
            System.err.print(TFTPClientTest.HELP_TEXT);
            System.exit(1);
        }
        hostname = args[argCounter];
        localFilename = args[argCounter + 1];
        remoteFilename = args[argCounter + 2];

        //run the TFTP client to send or request a file
        tftpClient = new TFTPClient();// Create a TFTPClient instance to handle the file transfer.
        tftpClient.setTimeout(60000);// Set the timeout to 1 minute
        try {
            tftpClient.openSocket();
        }
        catch (SocketException e) {
            System.err.println("Error: could not open local UDP socket.");
            System.err.println(e.getMessage());
            System.exit(1);
        }
        closed = false;//set the closed status of the local file to false which means it's open
        if (receiveFile) {//receive a file
            FileOutputStream output = null;
            File file;

            file = new File(localFilename);

            // If file exists, don't overwrite it.
            if (file.exists()) {
                System.err.println("Error: " + localFilename + " already exists.");
                System.exit(1);
            }

            // Try to open local file for writing
            try {
                output = new FileOutputStream(file);
            }
            catch (IOException e) {
                tftpClient.close();
                System.err.println("Error: could not open local file for writing.");
                System.err.println(e.getMessage());
                System.exit(1);
            }

            // Try to receive remote file via TFTP
            try {
                tftpClient.receiveFile(remoteFilename, mode, output, InetAddress.getByName(hostname));
            }
            catch (UnknownHostException e) {
                System.err.println("Error: could not resolve hostname.");
                System.err.println(e.getMessage());
                System.exit(1);
            }
            catch (IOException e) {
                System.err.println(
                        "Error: I/O exception occurred while receiving file.");
                System.err.println(e.getMessage());
                System.exit(1);
            }
            finally {
                // Close local socket and output file
                tftpClient.close();
                try {
                    if (output != null) {
                        output.close();
                    }
                    closed = true;
                }
                catch (IOException e) {
                    closed = false;
                    System.err.println("Error: error closing file.");
                    System.err.println(e.getMessage());
                }
            }

            if (!closed) {
                System.exit(1);
            }

        } else {//send a file
            FileInputStream input = null;

            // Try to open local file for reading
            try {
                input = new FileInputStream(localFilename);
            }
            catch (IOException e) {
                tftpClient.close();
                System.err.println("Error: could not open local file for reading.");
                System.err.println(e.getMessage());
                System.exit(1);
            }

            // Try to send local file via TFTP
            try {
                tftpClient.sendFile(remoteFilename, mode, input, InetAddress.getByName(hostname));
            }
            catch (UnknownHostException e) {
                System.err.println("Error: could not resolve hostname.");
                System.err.println(e.getMessage());
                System.exit(1);
            }
            catch (IOException e) {
                System.err.println(
                        "Error: I/O exception occurred while sending file.");
                System.err.println(e.getMessage());
                System.exit(1);
            }
            finally {
                // Close local socket and input file
                tftpClient.close();
                try {
                    if (input != null) {
                        input.close();
                    }
                    closed = true;
                }
                catch (IOException e) {
                    closed = false;
                    System.err.println("Error: error closing file.");
                    System.err.println(e.getMessage());
                }
            }

            if (!closed) {
                System.exit(1);
            }

        }

    }
}
