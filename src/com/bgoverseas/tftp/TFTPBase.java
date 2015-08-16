package com.bgoverseas.tftp;

import com.bgoverseas.tftp.exception.TFTPPacketException;
import com.bgoverseas.tftp.packet.*;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

/**
 * Created by binwang on 16/08/15.
 */
public class TFTPBase {
    public static final String NETASCII_MODE = "netascii";
    public static final String OCTET_MODE = "octet";
    public static final int DEFAULT_TIMEOUT = 10000;
    public static final int DEFAULT_SERVER_PORT = 69;
    public static final int ACCEPTED_DEFAULT_PACKET_SIZE = TFTPBasePacket.BLOCK_SIZE + 4;//use the data packet size as the receiving data size
    public static final int ACCEPTED_MIN_PACKET_SIZE = 4;

    protected int timeout;
    protected DatagramSocket socket;

    public TFTPBase(){
        this.timeout = TFTPBase.DEFAULT_TIMEOUT;
    }

    public void openSocket() throws SocketException {
        this.socket = new DatagramSocket();
    }

    public void send(TFTPBasePacket packet) throws IOException {
        this.socket.send(packet.createPacket());
    }

    public TFTPBasePacket receive() throws IOException, TFTPPacketException {
        DatagramPacket packet = new DatagramPacket(new byte[TFTPBase.ACCEPTED_DEFAULT_PACKET_SIZE], TFTPBase.ACCEPTED_DEFAULT_PACKET_SIZE);
        socket.receive(packet);
        return this.parseDatagram(packet);
    }

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
}
