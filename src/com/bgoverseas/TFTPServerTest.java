package com.bgoverseas;

import com.bgoverseas.tftp.TFTPServer;

import java.io.File;
import java.io.InputStreamReader;

/**
 * Created by binwang on 17/08/15.
 */
public class TFTPServerTest {
    public static void main(String[] args) throws Exception
    {
        if (args.length != 1)
        {
        System.out
        .println("You must provide 1 argument - the base path for the server to serve from.");
        System.exit(1);
        }

        TFTPServer ts = new TFTPServer(new File(args[0]), new File(args[0]), TFTPServer.ServerMode.R);
        ts.setSocketTimeout(2000);


        System.out.println("TFTP Server running.  Press enter to stop.");
        new InputStreamReader(System.in).read();

        ts.shutdown();
        System.out.println("Server shut down.");
        System.exit(0);
    }
}
