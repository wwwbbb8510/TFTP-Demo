package com.bgoverseas.tftp.packet;

import java.net.DatagramPacket;
import java.net.InetAddress;

/**
 * Created by binwang on 15/08/15.
 * This is the base packet class for all kind of packets
 */
public abstract class TFTPBasePacket {
    /**
     * OPCode of Read Request Packet
     */
    public static final int OPCODE_READ_REQUEST = 1;
    /**
     * OPCode of Write Request Packet
     */
    public static final int OPCODE_WRITE_REQUEST = 2;
    /**
     * OPCode of Data Request Packet
     */
    public static final int OPCODE_DATA_REQUEST = 3;
    /**
     * OPCode of Acknowledgement Request Packet
     */
    public static final int OPCODE_ACK_REQUEST = 4;
    /**
     * OPCode of Error Request Packet
     */
    public static final int OPCODE_ERROR_REQUEST = 5;
    /**
     * The size of each block
     */
    public static final int BLOCK_SIZE = 512;
    /**
     * packet opcode used to store sub packet class type
     */
    int opcode;
    /**
     * TID of the packet which is also used as the connection port
     */
    int tid;
    /**
     * the host address mainly carrying the IP address
     */
    InetAddress address;

    /**
     * constructor
     * @param opcode packet type
     * @param tid packet TID
     * @param address host address
     */
    protected TFTPBasePacket(int opcode, int tid, InetAddress address){
        this.opcode = opcode;
        this.tid = tid;
        this.address = address;
    }

    /**
     * abstract method of creating a datagram packet
     * @return
     */
    public abstract DatagramPacket createPacket();

    /**
     * get the current packet type
     * @return
     */
    public int getOpcode() {
        return opcode;
    }

    /**
     * get the packet tid
     * @return
     */
    public int getTid() {
        return tid;
    }

    /**
     * set the packet tid
     * @param tid
     */
    public void setTid(int tid) {
        this.tid = tid;
    }

    /**
     * get the packet address
     * @return
     */
    public InetAddress getAddress() {
        return address;
    }

    /**
     * set the packet address
     * @param address
     */
    public void setAddress(InetAddress address) {
        this.address = address;
    }
}
