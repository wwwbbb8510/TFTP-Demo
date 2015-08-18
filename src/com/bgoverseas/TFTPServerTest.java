package com.bgoverseas;

import com.bgoverseas.tftp.TFTPServer;

import java.io.File;
import java.io.InputStreamReader;

/**
 * Created by binwang on 17/08/15.
 * TFTP server used for testing
 */
public class TFTPServerTest {
    /**
     * entry of server test
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception
    {
        if (args.length != 1)
        {
            System.out
            .println("One parameter is required which is the directory for the test server");
            System.exit(1);
        }

        TFTPServer ts = new TFTPServer(new File(args[0]), new File(args[0]), TFTPServer.ServerMode.W);
        ts.setSocketTimeout(2000);

        System.out.println("TFTP Server running. Press enter to stop.");
        new InputStreamReader(System.in).read();
        ts.shutdown();
        System.out.println("Server shutting down.");
        System.exit(0);
    }
}
