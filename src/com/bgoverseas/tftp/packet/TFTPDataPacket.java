package com.bgoverseas.tftp.packet;

import com.bgoverseas.tftp.exception.TFTPPacketException;

import java.net.DatagramPacket;
import java.net.InetAddress;

/**
 * Created by binwang on 15/08/15.
 * data packet
 */
public class TFTPDataPacket extends TFTPBasePacket {
    /**
     * block number of the packet
     */
    private int blockNumber;
    /**
     * the length of data which should be 512 if it's not the last one
     */
    private int dataLength;
    /**
     * the offset into the data
     */
    private int offset;
    /**
     * data of the packet
     */
    private byte[] data;

    /**
     * constructor without a datagram packet
     * @param tid
     * @param address
     * @param blockNumber
     * @param dataLength
     * @param offset
     * @param data
     */
    public TFTPDataPacket(int tid, InetAddress address, int blockNumber, int dataLength, int offset, byte[] data){
        super(TFTPBasePacket.OPCODE_DATA_REQUEST, tid, address);
        this.blockNumber = blockNumber;
        this.dataLength = dataLength;
        this.offset = offset;
        this.data = data;
    }

    /**
     * constructor with a datagram packet
     * @param datagram
     * @throws TFTPPacketException
     */
    public TFTPDataPacket(DatagramPacket datagram) throws TFTPPacketException{
        super(TFTPBasePacket.OPCODE_DATA_REQUEST, datagram.getPort(), datagram.getAddress());
        this.data = datagram.getData();
        this.offset = 4;//2 bytes for opcode and 2 bytes for block number

        if (this.getOpcode() != data[1])
            throw new TFTPPacketException("TFTP code does not match the packet.");

        blockNumber = (((data[2] & 0xff) << 8) | (data[3] & 0xff));

        dataLength = datagram.getLength() - 4;
    }

    /**
     * create a data packet
     * @return new DatagramPacket
     */
    public DatagramPacket createPacket(){
        byte[] data;

        data = new byte[this.dataLength + 4];
        data[0] = 0;
        data[1] = (byte)this.opcode;
        data[2] = (byte)((this.blockNumber & 0xffff) >> 8);
        data[3] = (byte)(this.blockNumber & 0xff);

        System.arraycopy(this.data, this.offset, data, 4, this.dataLength);

        return new DatagramPacket(data, this.dataLength + 4, this.address, this.tid);
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

    /**
     * get data length
     * @return
     */
    public int getDataLength() {
        return dataLength;
    }

    /**
     * set data length
     * @param dataLength
     */
    public void setDataLength(int dataLength) {
        this.dataLength = dataLength;
    }

    /**
     * get offset
     * @return
     */
    public int getOffset() {
        return offset;
    }

    /**
     * set offset
     * @param offset
     */
    public void setOffset(int offset) {
        this.offset = offset;
    }

    /**
     * get data
     * @return
     */
    public byte[] getData() {
        return data;
    }

    /**
     * set data
     * @param data
     */
    public void setData(byte[] data) {
        this.data = data;
    }
}
