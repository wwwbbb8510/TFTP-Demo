package com.bgoverseas.tftp.packet;

import com.bgoverseas.tftp.exception.TFTPPacketException;

import javax.xml.crypto.Data;
import java.net.DatagramPacket;
import java.net.InetAddress;

/**
 * Created by binwang on 15/08/15.
 * Acknowledge packet
 */
public class TFTPAckPacket extends TFTPBasePacket{
    /**
     * block number of the packet
     */
    private int blockNumber;

    /**
     * constructor without a datagram
     * @param tid
     * @param address
     * @param blockNumber
     */
    public TFTPAckPacket(int tid, InetAddress address, int blockNumber){
        super(TFTPBasePacket.OPCODE_ACK_REQUEST, tid, address);
        this.blockNumber = blockNumber;
    }

    /**
     * constructor with a datagram
     * @param datagram
     * @throws TFTPPacketException
     */
    public TFTPAckPacket(DatagramPacket datagram) throws TFTPPacketException{
        super(TFTPBasePacket.OPCODE_ACK_REQUEST, datagram.getPort(), datagram.getAddress());
        byte[] data;

        data = datagram.getData();

        if (this.getOpcode() != data[1])
            throw new TFTPPacketException("TFTP Opcode doesn't match the packet.");

        this.blockNumber = (((data[2] & 0xff) << 8) | (data[3] & 0xff));
    }

    /**
     * create a new packet
     * @return
     */
    public DatagramPacket createPacket(){
        byte[] data;

        data = new byte[4];//two bytes for Opcode and two bytes for block number
        data[0] = 0;
        data[1] = (byte)this.getOpcode();
        data[2] = (byte)((this.blockNumber & 0xffff) >> 8);
        data[3] = (byte)(this.blockNumber & 0xff);

        return new DatagramPacket(data, data.length, this.address, this.tid);
    }

    /**
     * get block number
     * @return
     */
    public int getBlockNumber() {
        return blockNumber;
    }

    /**
     * set block number
     * @param blockNumber
     */
    public void setBlockNumber(int blockNumber) {
        this.blockNumber = blockNumber;
    }
}
