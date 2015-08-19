package com.bgoverseas.tftp;

import com.bgoverseas.tftp.TFTPBase;
import com.bgoverseas.tftp.exception.TFTPPacketException;
import com.bgoverseas.tftp.packet.*;
import com.bgoverseas.tftp.utils.NetasciiInputStream;
import com.bgoverseas.tftp.utils.NetasciiOutputStream;

import java.io.*;
import java.net.SocketTimeoutException;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Created by stevenwang on 17/08/15.
 * TFTP Server
 */
public class TFTPServer implements Runnable{

    /**
     * default server port
     */
    private static final int DEFAULT_TFTP_PORT = 6900;

    /**
     * server mode(R: read, W: read and write)
     */
    public static enum ServerMode { R, W; }

    /**
     * a set of file transfer handler used for multi-threads
     */
    private HashSet<TFTPServerHandler> handlers = new HashSet<TFTPServerHandler>();
    /**
     * server mode
     */
    private ServerMode mode;
    /**
     * maximum trying timeout times
     */
    private int maxTimeoutTimes = 3;
    /**
     * server port
     */
    private int serverPort;
    /**
     * server read file directory
     */
    private File serverReadDirectory;
    /**
     * server write file directory
     */
    private File serverWriteDirectory;
    /**
     * whether to shutdown the server or not
     */
    private volatile boolean shutdownServer = false;
    /**
     * TFTP instance
     */
    private TFTPBase serverTftp;
    /**
     * server thread
     */
    private Thread serverThread;
    /**
     * socket timeout time
     */
    private int socketTimeout;
    /**
     * server execption
     */
    private Exception serverException;

    /**
     * constructor
     * @param serverReadDirectory
     * @param serverWriteDirectory
     * @param port
     * @param mode
     * @throws IOException
     */
    public TFTPServer(File serverReadDirectory, File serverWriteDirectory, int port, ServerMode mode) throws IOException {
        this.serverPort = port;
        this.mode = mode;
        launch(serverReadDirectory, serverWriteDirectory);
    }

    /**
     * constructor using default server port
     * @param serverReadDirectory
     * @param serverWriteDirector
     * @param mode
     * @throws IOException
     */
    public TFTPServer(File serverReadDirectory, File serverWriteDirector, ServerMode mode) throws IOException {
        this(serverReadDirectory, serverWriteDirector, TFTPServer.DEFAULT_TFTP_PORT, mode);
    }

    /**
     * the actual method for the thread
     */
    public void run()
    {
        try
        {
            while (!this.shutdownServer)
            {
                TFTPBasePacket tftpPacket;

                tftpPacket = this.serverTftp.receive();

                TFTPServerHandler serverHandler = new TFTPServerHandler(tftpPacket);
                synchronized(handlers)
                {
                    handlers.add(serverHandler);
                }

                Thread thread = new Thread(serverHandler);
                thread.setDaemon(true);
                thread.start();
            }
        }
        catch (Exception e)
        {
            if (!this.shutdownServer)
            {
                serverException = e;
                System.out.println("Unexpected Error in TFTP Server - Server shut down! + " + e);
            }
        }
        finally
        {
            this.shutdownServer = true; // set this to true, so the launching thread can check to see if it started.
            if (serverTftp != null && serverTftp.isOpen())
            {
                serverTftp.close();
            }
        }

    }

    /**
     * launch the server
     * @param serverReadDirectory
     * @param serverWriteDirectory
     * @throws IOException
     */
    private void launch(File serverReadDirectory, File serverWriteDirectory) throws IOException
    {
        System.out.println("Starting TFTP Server on port " + this.serverPort + ".  Read directory: "
                + serverReadDirectory + " Write directory: " + serverWriteDirectory
                + " Server Mode is " + this.mode);

        this.serverReadDirectory = serverReadDirectory.getCanonicalFile();
        if (!this.serverReadDirectory.exists() || !serverReadDirectory.isDirectory())
        {
            throw new IOException("The server read directory " + this.serverReadDirectory
                    + " does not exist");
        }

        this.serverWriteDirectory = serverWriteDirectory.getCanonicalFile();
        if (!this.serverWriteDirectory.exists() || !serverWriteDirectory.isDirectory())
        {
            throw new IOException("The server write directory " + this.serverWriteDirectory
                    + " does not exist");
        }

        this.serverTftp = new TFTPBase();

        // This is the value used in response to each client.
        this.socketTimeout = this.serverTftp.getTimeout();

        // we want the server thread to listen forever.
        this.serverTftp.setTimeout(0);

        this.serverTftp.openSocket(this.serverPort);

        this.serverThread = new Thread(this);
        this.serverThread.setDaemon(true);
        this.serverThread.start();
    }

    /**
     * finalization method
     * @throws Throwable
     */
    protected void finalize() throws Throwable
    {
        shutdown();
    }

    /**
     * shutdown the server
     */
    public void shutdown()
    {
        shutdownServer = true;

        synchronized(this.handlers)
        {
            Iterator<TFTPServerHandler> it = this.handlers.iterator();
            while (it.hasNext())
            {
                it.next().shutdown();
            }
        }

        try
        {
            this.serverTftp.close();
        }
        catch (RuntimeException e)
        {
            // noop
        }

        try {
            serverThread.join();
        } catch (InterruptedException e) {
            // we've done the best we could, return
        }
    }

    /**
     * set socket timeout
     * @param socketTimeout
     */
    public void setSocketTimeout(int socketTimeout) {
        this.socketTimeout = socketTimeout;
    }

    /**
     * private class of file transfer handler
     */
    private class TFTPServerHandler implements Runnable{
        /**
         * tftp packet which will be sent
         */
        private TFTPBasePacket tftpPacket;
        /**
         * shutdown status
         */
        private boolean shutdownHandler = false;
        /**
         * tftp processor
         */
        TFTPBase tftp = null;
        /**
         * socket timeout
         */
        protected int socketTimeout;

        /**
         * constructor
         * @param tftpPacket
         */
        public TFTPServerHandler(TFTPBasePacket tftpPacket){
            this.tftpPacket = tftpPacket;
            this.socketTimeout = TFTPBase.DEFAULT_TIMEOUT;
        }

        /**
         * show down the handler
         */
        public void shutdown(){
            shutdownHandler = true;
            try
            {
                this.tftp.close();
            }
            catch (RuntimeException e)
            {
            }
        }

        /**
         * the actual method of the handler thread
         */
        public void run()
        {
            try
            {
                this.tftp = new TFTPBase();

                this.tftp.setTimeout(this.socketTimeout);

                this.tftp.openSocket();

                if (this.tftpPacket instanceof TFTPReadRequestPacket)
                {
                    handleRead(((TFTPReadRequestPacket) this.tftpPacket));
                }
                else if (this.tftpPacket instanceof TFTPWriteRequestPacket)
                {
                    handleWrite((TFTPWriteRequestPacket) this.tftpPacket);
                }
                else
                {
                    System.out.println("Unsupported TFTP request (" + this.tftpPacket + ") - ignored.");
                }
            }
            catch (Exception e)
            {
                if (!this.shutdownHandler)
                {
                    System.out.println("Unexpected Error in during TFTP file transfer.  Transfer aborted. " + e);
                }
            }
            finally
            {
                try
                {
                    if (this.tftp != null && this.tftp.isOpen())
                    {
                        this.tftp.close();
                    }
                }
                catch (Exception e)
                {
                    // noop
                }
                synchronized(TFTPServer.this.handlers)
                {
                    TFTPServer.this.handlers.remove(this);
                }
            }
        }

        /**
         * process the read request
         * @param rrqPacket
         * @throws IOException
         * @throws TFTPPacketException
         */
        private void handleRead(TFTPReadRequestPacket rrqPacket) throws IOException, TFTPPacketException
        {
            InputStream is = null;
            try
            {
                try
                {
                    is = new BufferedInputStream(new FileInputStream(buildSafeFile(
                            TFTPServer.this.serverReadDirectory, rrqPacket.getFilename(), false)));
                }
                catch (FileNotFoundException e)
                {
                    this.tftp.send(new TFTPErrorPacket(rrqPacket.getTid(), rrqPacket.getAddress(), TFTPErrorPacket.ERROR_FILE_NOT_FOUND, e.getMessage()));
                    return;
                }
                catch (Exception e)
                {
                    this.tftp.send(new TFTPErrorPacket(rrqPacket.getTid(), rrqPacket.getAddress(), TFTPErrorPacket.ERROR_NOT_DEFINED, e.getMessage()));
                    return;
                }

                if (rrqPacket.getMode() == TFTPBase.NETASCII_MODE)
                {
                    is = new NetasciiInputStream(is);
                }

                byte[] temp = new byte[TFTPBasePacket.BLOCK_SIZE];

                TFTPBasePacket answer;

                int block = 1;
                boolean sendNext = true;

                int readLength = TFTPBasePacket.BLOCK_SIZE;

                TFTPDataPacket lastSentData = null;

                // We are reading a file, so when we read less than the
                // requested bytes, we know that we are at the end of the file.
                while (readLength == TFTPBasePacket.BLOCK_SIZE && !this.shutdownHandler)
                {
                    if (sendNext)
                    {
                        readLength = is.read(temp);
                        if (readLength == -1)
                        {
                            readLength = 0;
                        }
                        lastSentData = new TFTPDataPacket(rrqPacket.getTid(), rrqPacket.getAddress(), block,readLength, 0, temp);
                        this.tftp.send(lastSentData);
                    }

                    answer = null;

                    int timeoutCount = 0;

                    while (!this.shutdownHandler
                            && (answer == null || !answer.getAddress().equals(rrqPacket.getAddress()) || answer
                            .getTid() != rrqPacket.getTid()))
                    {
                        // listen for an answer.
                        if (answer != null)
                        {
                            // The answer that we got didn't come from the
                            // expected source, fire back an error, and continue
                            // listening.
                            System.out.println("TFTP Server ignoring message from unexpected source.");
                            this.tftp.send(new TFTPErrorPacket(answer.getTid(), answer.getAddress(), TFTPErrorPacket.ERROR_UNKNOWN_TRANSFER_ID, "Unexpected Host or Port"));
                        }
                        try
                        {
                            answer = this.tftp.receive();
                        }
                        catch (SocketTimeoutException e)
                        {
                            if (timeoutCount >= TFTPServer.this.maxTimeoutTimes)
                            {
                                throw e;
                            }
                            // didn't get an ack for this data. need to resend
                            // it.
                            timeoutCount++;
                            this.tftp.send(lastSentData);
                            continue;
                        }
                    }

                    if (answer == null || !(answer instanceof TFTPAckPacket))
                    {
                        if (!this.shutdownHandler)
                        {
                            System.out.println("Unexpected response from tftp client during transfer (" + answer + ").  Transfer aborted.");
                        }
                        break;
                    }
                    else
                    {
                        // once we get here, we know we have an answer packet
                        // from the correct host.
                        TFTPAckPacket ack = (TFTPAckPacket) answer;
                        if (ack.getBlockNumber() != block)
                        {
                            /*
                             * The origional tftp spec would have called on us to resend the
                             * previous data here, however, that causes the SAS Syndrome.
                             * http://www.faqs.org/rfcs/rfc1123.html section 4.2.3.1 The modified
                             * spec says that we ignore a duplicate ack. If the packet was really
                             * lost, we will time out on receive, and resend the previous data at
                             * that point.
                             */
                            sendNext = false;
                        }
                        else
                        {
                            // send the next block
                            block++;
                            if (block > 65535)
                            {
                                // wrap the block number
                                block = 0;
                            }
                            sendNext = true;
                        }
                    }
                }
            }
            finally
            {
                try
                {
                    if (is != null)
                    {
                        is.close();
                    }
                }
                catch (IOException e)
                {
                    // noop
                }
            }
        }

        /**
         * process the write request
         * @param wrqPacket
         * @throws IOException
         * @throws TFTPPacketException
         */
        private void handleWrite(TFTPWriteRequestPacket wrqPacket) throws IOException,
                TFTPPacketException
        {
            OutputStream bos = null;
            try
            {
                if (TFTPServer.this.mode == ServerMode.R)
                {
                    this.tftp.send(new TFTPErrorPacket(wrqPacket.getTid(), wrqPacket.getAddress(), TFTPErrorPacket.ERROR_ILLEGAL_OPERATION, "Write not allowed by server."));
                    return;
                }

                int lastBlock = 0;
                String fileName = wrqPacket.getFilename();

                try
                {
                    File temp = buildSafeFile(TFTPServer.this.serverWriteDirectory, fileName, true);
                    if (temp.exists())
                    {
                        this.tftp.send(new TFTPErrorPacket(wrqPacket.getTid(), wrqPacket.getAddress(), TFTPErrorPacket.ERROR_FILE_NOT_FOUND, "File already exists"));
                        return;
                    }
                    bos = new BufferedOutputStream(new FileOutputStream(temp));

                    if (wrqPacket.getMode() == TFTPBase.NETASCII_MODE)
                    {
                        bos = new NetasciiOutputStream(bos);
                    }
                }
                catch (Exception e)
                {
                    this.tftp.send(new TFTPErrorPacket(wrqPacket.getTid(), wrqPacket.getAddress(), TFTPErrorPacket.ERROR_NOT_DEFINED, e.getMessage()));
                    return;
                }

                TFTPAckPacket lastSentAck = new TFTPAckPacket(wrqPacket.getTid(), wrqPacket.getAddress(), 0);
                this.tftp.send(lastSentAck);

                while (true)
                {
                    // get the response - ensure it is from the right place.
                    TFTPBasePacket dataPacket = null;

                    int timeoutCount = 0;

                    while (!this.shutdownHandler
                            && (dataPacket == null
                            || !dataPacket.getAddress().equals(wrqPacket.getAddress()) || dataPacket
                            .getTid() != wrqPacket.getTid()))
                    {
                        // listen for an answer.
                        if (dataPacket != null)
                        {
                            // The data that we got didn't come from the
                            // expected source, fire back an error, and continue
                            // listening.
                            System.out.println("TFTP Server ignoring message from unexpected source.");
                            this.tftp.send(new TFTPErrorPacket(dataPacket.getTid(), dataPacket.getAddress(), TFTPErrorPacket.ERROR_UNKNOWN_TRANSFER_ID, "Unexpected Host or Port"));
                        }

                        try
                        {
                            dataPacket = this.tftp.receive();
                        }
                        catch (SocketTimeoutException e)
                        {
                            if (timeoutCount >= TFTPServer.this.maxTimeoutTimes)
                            {
                                throw e;
                            }
                            // It didn't get our ack. Resend it.
                            this.tftp.send(lastSentAck);
                            timeoutCount++;
                            continue;
                        }
                    }

                    if (dataPacket != null && dataPacket instanceof TFTPWriteRequestPacket)
                    {
                        // it must have missed our initial ack. Send another.
                        lastSentAck = new TFTPAckPacket(wrqPacket.getTid(), wrqPacket.getAddress(), 0);
                        this.tftp.send(lastSentAck);
                    }
                    else if (dataPacket == null || !(dataPacket instanceof TFTPDataPacket))
                    {
                        if (!this.shutdownHandler)
                        {
                            System.out.println("Unexpected response from tftp client during transfer (" + dataPacket + ").  Transfer aborted.");
                        }
                        break;
                    }
                    else
                    {
                        int block = ((TFTPDataPacket) dataPacket).getBlockNumber();
                        byte[] data = ((TFTPDataPacket) dataPacket).getData();
                        int dataLength = ((TFTPDataPacket) dataPacket).getDataLength();
                        int dataOffset = ((TFTPDataPacket) dataPacket).getOffset();

                        if (block > lastBlock || (lastBlock == 65535 && block == 0))
                        {
                            // it might resend a data block if it missed our ack
                            // - don't rewrite the block.
                            bos.write(data, dataOffset, dataLength);
                            lastBlock = block;
                        }

                        lastSentAck = new TFTPAckPacket(wrqPacket.getTid(), wrqPacket.getAddress(), block);
                        this.tftp.send(lastSentAck);
                        if (dataLength < TFTPBasePacket.BLOCK_SIZE)
                        {
                            // end of stream signal - The tranfer is complete.
                            bos.close();

                            // But my ack may be lost - so listen to see if I
                            // need to resend the ack.
                            for (int i = 0; i < TFTPServer.this.maxTimeoutTimes; i++)
                            {
                                try
                                {
                                    dataPacket = this.tftp.receive();
                                }
                                catch (SocketTimeoutException e)
                                {
                                    // this is the expected route - the client
                                    // shouldn't be sending any more packets.
                                    break;
                                }

                                if (dataPacket != null
                                        && (!dataPacket.getAddress().equals(wrqPacket.getAddress()) || dataPacket
                                        .getTid() != wrqPacket.getTid()))
                                {
                                    // make sure it was from the right client...
                                    this.tftp.send(new TFTPErrorPacket(dataPacket.getTid(), dataPacket.getAddress(),TFTPErrorPacket.ERROR_UNKNOWN_TRANSFER_ID, "Unexpected Host or Port"));
                                }
                                else
                                {
                                    // This means they sent us the last
                                    // datapacket again, must have missed our
                                    // ack. resend it.
                                    this.tftp.send(lastSentAck);
                                }
                            }

                            // all done.
                            break;
                        }
                    }
                }
            }
            finally
            {
                if (bos != null)
                {
                    bos.close();
                }
            }
        }

        /**
         * build a file
         * @param serverDirectory
         * @param fileName
         * @param createSubDirs
         * @return
         * @throws IOException
         */
        private File buildSafeFile(File serverDirectory, String fileName, boolean createSubDirs)
                throws IOException
        {
            File temp = new File(serverDirectory, fileName);
            temp = temp.getCanonicalFile();

            if (!isSubdirectoryOf(serverDirectory, temp))
            {
                throw new IOException("Cannot access files outside of tftp server root.");
            }

            // ensure directory exists (if requested)
            if (createSubDirs)
            {
                createDirectory(temp.getParentFile());
            }

            return temp;
        }

        /**
         * create the directory
         * @param file
         * @throws IOException
         */
        private void createDirectory(File file) throws IOException
        {
            File parent = file.getParentFile();
            if (parent == null)
            {
                throw new IOException("Unexpected error creating requested directory");
            }
            if (!parent.exists())
            {
                // recurse...
                createDirectory(parent);
            }

            if (parent.isDirectory())
            {
                if (file.isDirectory())
                {
                    return;
                }
                boolean result = file.mkdir();
                if (!result)
                {
                    throw new IOException("Couldn't create requested directory");
                }
            }
            else
            {
                throw new IOException(
                        "Invalid directory path - file in the way of requested folder");
            }
        }

        /**
         * check the sub directory
         * @param parent
         * @param child
         * @return
         */
        private boolean isSubdirectoryOf(File parent, File child)
        {
            File childsParent = child.getParentFile();
            if (childsParent == null)
            {
                return false;
            }
            if (childsParent.equals(parent))
            {
                return true;
            }
            else
            {
                return isSubdirectoryOf(parent, childsParent);
            }
        }
    }
}
