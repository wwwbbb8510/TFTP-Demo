package com.bgoverseas.tftp;

import com.bgoverseas.tftp.exception.TFTPPacketException;
import com.bgoverseas.tftp.packet.*;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

/**
 * Created by binwang on 16/08/15.
 * TFTP base class which is that parent of both TFTP Client and TFTP Server
 */
public class TFTPBase {
    /**
     * the string of netascii mode
     */
    public static final String NETASCII_MODE = "netascii";
    /**
     * the string of octet mode
     */
    public static final String OCTET_MODE = "octet";
    /**
     * default timeout by millisecond
     */
    public static final int DEFAULT_TIMEOUT = 10000;
    /**
     * default server port
     */
    public static final int DEFAULT_SERVER_PORT = 6900;
    /**
     * the accepted packet size used for receiving the packet
     */
    public static final int ACCEPTED_DEFAULT_PACKET_SIZE = TFTPBasePacket.BLOCK_SIZE + 4;//use the data packet size as the receiving data size
    /**
     * the minimum packet size which all of the packets must bigger than
     */
    public static final int ACCEPTED_MIN_PACKET_SIZE = 4;

    /**
     * timeout of the socket
     */
    protected int timeout;
    /**
     * UDP socket
     */
    protected DatagramSocket socket;

    protected boolean isOpen;

    /**
     * constructor
     */
    public TFTPBase(){
        this.timeout = TFTPBase.DEFAULT_TIMEOUT;
        this.isOpen = false;
    }

    /**
     * open a new UDP socket
     * @throws SocketException
     */
    public void openSocket() throws SocketException {
        this.socket = new DatagramSocket();
        this.isOpen = true;
    }

    /**
     * open a new UDP socket with a port number
     * @param port
     * @throws SocketException
     */
    public void openSocket(int port) throws SocketException
    {
        this.socket = new DatagramSocket(port);
        this.isOpen = true;
    }

    /**
     * send a TFTP packet
     * @param packet
     * @throws IOException
     */
    public void send(TFTPBasePacket packet) throws IOException {
        this.socket.send(packet.createPacket());
    }

    /**
     * receive a TFTP packet
     * @return
     * @throws IOException
     * @throws TFTPPacketException
     */
    public TFTPBasePacket receive() throws IOException, TFTPPacketException {
        DatagramPacket packet = new DatagramPacket(new byte[TFTPBase.ACCEPTED_DEFAULT_PACKET_SIZE], TFTPBase.ACCEPTED_DEFAULT_PACKET_SIZE);
        socket.receive(packet);
        return this.parseDatagram(packet);
    }

    public void discard() throws IOException {
        int to;
        DatagramPacket datagram;

        datagram = new DatagramPacket(new byte[TFTPBase.ACCEPTED_DEFAULT_PACKET_SIZE], TFTPBase.ACCEPTED_DEFAULT_PACKET_SIZE);

        to = this.socket.getSoTimeout();
        this.socket.setSoTimeout(1);

        try
        {
            while (true)
                this.socket.receive(datagram);
        }
        catch (SocketException e)
        {

        }
        catch (InterruptedIOException e)
        {

        }

        this.socket.setSoTimeout(to);
    }

    /**
     * parse a UDP datagram to a TFTP packet
     * @param datagram
     * @return
     * @throws TFTPPacketException
     */
    protected TFTPBasePacket parseDatagram(DatagramPacket datagram) throws TFTPPacketException {
        byte[] data;
        TFTPBasePacket packet = null;

        if (datagram.getLength() < TFTPBase.ACCEPTED_MIN_PACKET_SIZE)
            throw new TFTPPacketException("Datagram data length is too short");

        data = datagram.getData();

        switch (data[1])
        {
            case TFTPBasePacket.OPCODE_READ_REQUEST:
                packet = new TFTPReadRequestPacket(datagram);
                break;
            case TFTPBasePacket.OPCODE_WRITE_REQUEST:
                packet = new TFTPWriteRequestPacket(datagram);
                break;
            case TFTPBasePacket.OPCODE_DATA_REQUEST:
                packet = new TFTPDataPacket(datagram);
                break;
            case TFTPBasePacket.OPCODE_ACK_REQUEST:
                packet = new TFTPAckPacket(datagram);
                break;
            case TFTPBasePacket.OPCODE_ERROR_REQUEST:
                packet = new TFTPErrorPacket(datagram);
                break;
            default:
                throw new TFTPPacketException(
                        "Bad packet.  Invalid TFTP operator code.");
        }

        return packet;
    }

    public void close(){
        if (this.socket != null) {
            this.socket.close();
        }
        this.socket = null;
        this.isOpen = false;
    }

    /**
     * get current timeout
     * @return
     */
    public int getTimeout() {
        return timeout;
    }

    /**
     * set timeout
     * @param timeout
     */
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    /**
     * get UDP socket
     * @return
     */
    public DatagramSocket getSocket() {
        return socket;
    }

    /**
     * set UDP socket
     * @param socket
     */
    public void setSocket(DatagramSocket socket) {
        this.socket = socket;
    }

    /**
     * get isOpen
     * @return
     */
    public boolean isOpen() {
        return isOpen;
    }
}
