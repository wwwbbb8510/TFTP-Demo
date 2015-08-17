package com.bgoverseas.tftp;

import com.bgoverseas.tftp.TFTPBase;
import com.bgoverseas.tftp.exception.TFTPPacketException;
import com.bgoverseas.tftp.packet.*;
import com.bgoverseas.tftp.utils.NetasciiInputStream;
import com.bgoverseas.tftp.utils.NetasciiOutputStream;
import com.sun.org.apache.xalan.internal.xsltc.trax.TemplatesHandlerImpl;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.SocketException;

/**
 * Created by stevenwang on 17/08/15.
 * TFTP Client
 */
public class TFTPClient extends TFTPBase{

    public static final int DEFAULT_MAX_TIMEOUT_NUMBER = 3;

    private int maxTimeoutNumber;

    public TFTPClient(){
        this.maxTimeoutNumber = TFTPClient.DEFAULT_MAX_TIMEOUT_NUMBER;
    }

    public int receiveFile(String filename, String mode, OutputStream output,
                           InetAddress address, int port) throws IOException {
        int bytesRead = 0;
        int timeoutTimes = 0;
        int lastBlock = 0;
        int block = 1;
        int hostTid = 0;
        int dataLength = 0;
        TFTPBasePacket sentPacket, receivedPacket = null;
        TFTPErrorPacket error;
        TFTPDataPacket data;
        TFTPAckPacket ack = new TFTPAckPacket(port, address, 0);

        if (mode == TFTPBase.NETASCII_MODE)
            output = new NetasciiOutputStream(output);

        sentPacket =
                new TFTPReadRequestPacket(port, address, filename, mode);

        _sendPacket:
        do
        {
            this.send(sentPacket);

            _receivePacket:
            while (true)
            {
                timeoutTimes = 0;
                while (timeoutTimes < this.maxTimeoutNumber)
                {
                    try
                    {
                        receivedPacket = this.receive();
                        break;
                    }
                    catch (SocketException e)
                    {
                        if (++timeoutTimes >= this.maxTimeoutNumber)
                        {
                            throw new IOException("Connection timed out.");
                        }
                        continue;
                    }
                    catch (InterruptedIOException e)
                    {
                        if (++timeoutTimes >= this.maxTimeoutNumber)
                        {
                            throw new IOException("Connection timed out.");
                        }
                        continue;
                    }
                    catch (TFTPPacketException e)
                    {
                        throw new IOException("Bad packet: " + e.getMessage());
                    }
                }

                //get the host tid/port from the first received packet
                if (lastBlock == 0)
                {
                    hostTid = receivedPacket.getTid();
                    ack.setTid(hostTid);
                }

                if (address.equals(receivedPacket.getAddress()) &&
                        receivedPacket.getTid() == hostTid)
                {//TID and host address match the connection

                    switch (receivedPacket.getOpcode())
                    {
                        case TFTPBasePacket.OPCODE_ERROR_REQUEST:
                            error = (TFTPErrorPacket)receivedPacket;
                            throw new IOException("Error code " + error.getErrorCode() +
                                    " Error message: " + error.getErrorMessage());
                        case TFTPBasePacket.OPCODE_DATA_REQUEST:
                            data = (TFTPDataPacket)receivedPacket;
                            dataLength = data.getDataLength();

                            lastBlock = data.getBlockNumber();

                            if (lastBlock == block)
                            {
                                try
                                {
                                    output.write(data.getData(), data.getOffset(),
                                            dataLength);
                                }
                                catch (IOException e)
                                {
                                    error = new TFTPErrorPacket(hostTid, address,
                                            TFTPErrorPacket.ERROR_DISK_FULL,
                                            "File write failed.");
                                    this.send(error);
                                    throw e;
                                }
                                ++block;
                                if (block > 65535)
                                {
                                    block = 0;
                                }

                                break _receivePacket;
                            }
                            else
                            {
                                this.discard();

                                if (lastBlock == (block == 0 ? 65535 : (block - 1)))
                                    continue _sendPacket;  // Resend last acknowledgement.

                                continue _receivePacket; // Start fetching packets again.
                            }
                            //break;

                        default:
                            throw new IOException("Received unexpected Opcode.");
                    }
                }
                else
                {//TID doesn't match that from the connected server
                    error = new TFTPErrorPacket(receivedPacket.getTid(), receivedPacket.getAddress(), TFTPErrorPacket.ERROR_UNKNOWN_TRANSFER_ID, "Unexpected TID from the host.");
                    this.send(error);
                    continue _sendPacket;
                }

                // We should never get here, but this is a safety to avoid
                // infinite loop.  If only Java had the goto statement.
                //break;
            }

            ack.setBlockNumber(lastBlock);
            sentPacket = ack;
            bytesRead += dataLength;
        } // First data packet less than 512 bytes signals end of stream.

        while (dataLength == TFTPBasePacket.BLOCK_SIZE);

        this.send(sentPacket);

        return bytesRead;
    }

    public int receiveFile(String filename, String mode, OutputStream output,
                           InetAddress address) throws IOException {
        return this.receiveFile(filename, mode, output, address, TFTPBase.DEFAULT_SERVER_PORT);
    }

    public void sendFile(String filename, String mode, InputStream input,
                         InetAddress address, int port) throws IOException
    {
        int bytesRead = 0;
        int timeoutTimes = 0;
        int lastBlock = 0;
        int block = 0;
        int hostTid = 0;
        int dataLength = 0;
        int offset;
        int totalThisPacket = 0;
        boolean justStarted = true;
        boolean lastAckWait = false;
        TFTPBasePacket sentPacket, receivedPacket = null;
        TFTPErrorPacket error;
        TFTPDataPacket data = new TFTPDataPacket(port,address, 0, 0 , 4, new byte[TFTPBase.ACCEPTED_DEFAULT_PACKET_SIZE]);
        TFTPAckPacket ack;

        if (mode == TFTPBase.NETASCII_MODE)
            input = new NetasciiInputStream(input);

        sentPacket =
                new TFTPWriteRequestPacket(port, address, filename, mode);

        _sendPacket:
        do
        {
            // first time: block is 0, lastBlock is 0, send a request packet.
            // subsequent: block is integer starting at 1, send data packet.
            this.send(sentPacket);

            // this is trying to receive an ACK
            _receivePacket:
            while (true)
            {
                timeoutTimes = 0;
                while (timeoutTimes < this.maxTimeoutNumber)
                {
                    try
                    {
                        receivedPacket = this.receive();
                        break;
                    }
                    catch (SocketException e)
                    {
                        if (++timeoutTimes >= this.maxTimeoutNumber)
                        {
                            throw new IOException("Connection timed out.");
                        }
                        continue;
                    }
                    catch (InterruptedIOException e)
                    {
                        if (++timeoutTimes >= this.maxTimeoutNumber)
                        {
                            throw new IOException("Connection timed out.");
                        }
                        continue;
                    }
                    catch (TFTPPacketException e)
                    {
                        throw new IOException("Bad packet: " + e.getMessage());
                    }
                } // end of while loop over tries to receive

                // The first time we receive we get the port number and
                if (justStarted)
                {
                    justStarted = false;
                    hostTid = receivedPacket.getTid();
                    data.setTid(hostTid);
                }

                // Comply with RFC 783 indication that an error acknowledgement
                // should be sent to originator if unexpected TID or host.
                if (address.equals(receivedPacket.getAddress()) &&
                        receivedPacket.getTid() == hostTid)
                {

                    switch (receivedPacket.getOpcode())
                    {
                        case TFTPBasePacket.OPCODE_ERROR_REQUEST:
                            error = (TFTPErrorPacket)receivedPacket;
                            throw new IOException("Error code " + error.getErrorCode() +
                                    " Error message: " + error.getErrorMessage());
                        case TFTPBasePacket.OPCODE_ACK_REQUEST:
                            ack = (TFTPAckPacket)receivedPacket;

                            lastBlock = ack.getBlockNumber();

                            if (lastBlock == block)
                            {
                                ++block;
                                if (block > 65535)
                                {
                                    // wrap the block number
                                    block = 0;
                                }
                                if (lastAckWait) {

                                    break _sendPacket;
                                }
                                else {
                                    break _receivePacket;
                                }
                            }
                            else
                            {
                                this.discard();

                                if (lastBlock == (block == 0 ? 65535 : (block - 1)))
                                    continue _sendPacket;  // Resend last acknowledgement.

                                continue _receivePacket; // Start fetching packets again.
                            }
                            //break;

                        default:
                            throw new IOException("Received unexpected packet type.");
                    }
                }
                else
                {
                    error = new TFTPErrorPacket(receivedPacket.getTid(),receivedPacket.getAddress(),TFTPErrorPacket.ERROR_UNKNOWN_TRANSFER_ID,
                            "Unexpected TID from the server.");
                    this.send(error);
                    continue _sendPacket;
                }

                // We should never get here, but this is a safety to avoid
                // infinite loop.  If only Java had the goto statement.
                //break;
            }

            // OK, we have just gotten ACK about the last data we sent. Make another
            // and send it

            dataLength = TFTPBasePacket.BLOCK_SIZE;
            offset = 4;
            totalThisPacket = 0;
            while (dataLength > 0 &&
                    (bytesRead = input.read(new byte[TFTPBase.ACCEPTED_DEFAULT_PACKET_SIZE], offset, dataLength)) > 0)
            {
                offset += bytesRead;
                dataLength -= bytesRead;
                totalThisPacket += bytesRead;
            }

            if( totalThisPacket < TFTPBasePacket.BLOCK_SIZE) {
                /* this will be our last packet -- send, wait for ack, stop */
                lastAckWait = true;
            }
            data.setBlockNumber(block);
            data.setData(new byte[TFTPBase.ACCEPTED_DEFAULT_PACKET_SIZE], 4, totalThisPacket);
            sentPacket = data;
        }
        while ( totalThisPacket > 0 || lastAckWait );
        // Note: this was looping while dataLength == 0 || lastAckWait,
        // which was discarding the last packet if it was not full size
        // Should send the packet.
    }

    public void sendFile(String filename, String mode, InputStream input,
                         InetAddress address)
            throws IOException
    {
        sendFile(filename, mode, input, address, TFTPBase.DEFAULT_SERVER_PORT);
    }

    public int getMaxTimeoutNumber() {
        return maxTimeoutNumber;
    }

    public void setMaxTimeoutNumber(int maxTimeoutNumber) {
        this.maxTimeoutNumber = maxTimeoutNumber;
    }
}
