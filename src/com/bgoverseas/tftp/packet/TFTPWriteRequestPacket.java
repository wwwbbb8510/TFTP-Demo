package com.bgoverseas.tftp.packet;

import com.bgoverseas.tftp.exception.TFTPPacketException;

import java.net.DatagramPacket;
import java.net.InetAddress;

/**
 * Created by binwang on 15/08/15.
 * WRQ packet
 */
public class TFTPWriteRequestPacket extends TFTPRequestPacket {
    /**
     * constructor without a datagram
     * @param tid
     * @param address
     * @param filename
     * @param mode
     */
    public TFTPWriteRequestPacket(int tid, InetAddress address, String filename, String mode){
        super(TFTPBasePacket.OPCODE_WRITE_REQUEST, tid, address, filename, mode);
    }

    /**
     * constructor with a datagram
     * @param datagram
     * @throws TFTPPacketException
     */
    public TFTPWriteRequestPacket(DatagramPacket datagram)
            throws TFTPPacketException {
        super(TFTPBasePacket.OPCODE_WRITE_REQUEST, datagram);
    }
}
