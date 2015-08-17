package com.bgoverseas.tftp.packet;

import com.bgoverseas.tftp.exception.TFTPPacketException;

import java.net.DatagramPacket;
import java.net.InetAddress;

/**
 * Created by binwang on 15/08/15.
 * RRQ packet
 */
public class TFTPReadRequestPacket extends TFTPRequestPacket {
    /**
     * constructor without a datagram packet
     * @param tid
     * @param address
     * @param filename
     * @param mode
     */
    public TFTPReadRequestPacket(int tid, InetAddress address, String filename, String mode){
        super(TFTPBasePacket.OPCODE_READ_REQUEST, tid, address, filename, mode);
    }

    /**
     * constructor with a datagram packet
     * @param datagram
     * @throws TFTPPacketException
     */
    public TFTPReadRequestPacket(DatagramPacket datagram)
            throws TFTPPacketException{
        super(TFTPBasePacket.OPCODE_READ_REQUEST, datagram);
    }
}
