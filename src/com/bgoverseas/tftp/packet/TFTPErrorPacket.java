package com.bgoverseas.tftp.packet;

import com.bgoverseas.tftp.exception.TFTPPacketException;

import java.net.DatagramPacket;
import java.net.InetAddress;

/**
 * Created by binwang on 15/08/15.
 * Error packet
 */
public class TFTPErrorPacket extends TFTPBasePacket {
    /**
     * code for error not defined
     */
    public static final int ERROR_NOT_DEFINED = 0;
    /**
     * code for error of file not found
     */
    public static final int ERROR_FILE_NOT_FOUND = 1;
    /**
     * code for access violation
     */
    public static final int ERROR_ACCESS_VIOLATION = 2;
    /**
     * code for no storage space
     */
    public static final int ERROR_DISK_FULL = 3;
    /**
     * code for illegal operation
     */
    public static final int ERROR_ILLEGAL_OPERATION = 4;
    /**
     * code for unknown transfer ID
     */
    public static final int ERROR_UNKNOWN_TRANSFER_ID = 5;
    /**
     * code for file already existed
     */
    public static final int ERROR_FILE_ALREADY_EXIST = 6;
    /**
     * code for user not found
     */
    public static final int ERROR_NO_SUCH_USER = 7;
    /**
     * error code of the packet
     */
    private int errorCode;
    /**
     * error message
     */
    private String errorMessage;

    /**
     * constructor without a datagram
     * @param tid
     * @param address
     * @param errorCode
     * @param errorMessage
     */
    public TFTPErrorPacket(int tid, InetAddress address, int errorCode, String errorMessage){
        super(TFTPBasePacket.OPCODE_ERROR_REQUEST, tid, address);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    /**
     * constructor with a datagram
     * @param datagram
     * @throws TFTPPacketException
     */
    public TFTPErrorPacket(DatagramPacket datagram) throws TFTPPacketException{
        super(TFTPBasePacket.OPCODE_ERROR_REQUEST, datagram.getPort(), datagram.getAddress());
        int index, length;
        byte[] data;
        StringBuilder buffer;

        data = datagram.getData();
        length = datagram.getLength();

        if (this.getOpcode() != data[1])
            throw new TFTPPacketException("TFTP Opcode does not match the packet.");

        this.errorCode = (((data[2] & 0xff) << 8) | (data[3] & 0xff));

        if (length < 5)
            throw new TFTPPacketException("Error message is null");

        index = 4;
        buffer = new StringBuilder();

        while (index < length && data[index] != 0)
        {
            buffer.append((char)data[index]);
            ++index;
        }

        this.errorMessage = buffer.toString();
    }

    /**
     * create a new packet
     * @return
     */
    public DatagramPacket createPacket(){
        byte[] data;
        int length;

        length = this.errorMessage.length();

        data = new byte[length + 5];//two bytes for opcode, two bytes for error code, length of error string and 1 byte of 0 used for ending the packet
        data[0] = 0;
        data[1] = (byte)this.getOpcode();
        data[2] = (byte)((this.errorCode & 0xffff) >> 8);
        data[3] = (byte)(this.errorCode & 0xff);

        System.arraycopy(this.errorMessage.getBytes(), 0, data, 4, length);

        data[length + 4] = 0;

        return new DatagramPacket(data, data.length, this.address, this.tid);
    }
}
