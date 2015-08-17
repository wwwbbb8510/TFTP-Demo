package com.bgoverseas.tftp.packet;

import com.bgoverseas.tftp.exception.TFTPPacketException;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.Arrays;

/**
 * Created by binwang on 15/08/15.
 * Request packet base class for RRQ/WRQ
 */
public abstract class TFTPRequestPacket extends TFTPBasePacket {
    public static final String[] ALLOWED_MODES = new String[]{"netascii", "octet"};
    /**
     * RRQ/WRQ filename
     */
    protected String filename;
    /**
     * RRQ/WRQ mode(netascii or octet)
     */
    protected String mode;

    /**
     * constructor without a datagram packet
     * @param opcode Opcode of the packet
     * @param tid tid of the packet which is also the port
     * @param address host address
     * @param filename RRQ/WRQ filename
     * @param mode RRQ/WRQ mode
     */
    protected TFTPRequestPacket(int opcode, int tid, InetAddress address, String filename, String mode){
        super(opcode, tid, address);
        this.filename = filename;
        this.mode = mode;
    }

    /**
     * constructor with a datagram packet
     * @param opcode
     * @param datagram
     * @throws TFTPPacketException
     */
    protected TFTPRequestPacket(int opcode, DatagramPacket datagram)
    throws TFTPPacketException{
        super(opcode, datagram.getPort(), datagram.getAddress());

        byte[] data = datagram.getData();

        if (this.getOpcode() != data[1])
            throw new TFTPPacketException("TFTP opcodes do not match the packet type");

        StringBuilder buffer = new StringBuilder();

        int index = 2;
        int length = datagram.getLength();

        while (index < length && data[index] != 0)
        {
            buffer.append((char)data[index]);
            ++index;
        }

        this.filename = buffer.toString();

        if (index >= length)
            throw new TFTPPacketException("TFTP mode was not transferred");

        buffer.setLength(0);
        ++index;
        while (index < length && data[index] != 0)
        {
            buffer.append((char)data[index]);
            ++index;
        }

        String modeFromPacket = buffer.toString().toLowerCase();
        if(Arrays.asList(TFTPRequestPacket.ALLOWED_MODES).contains(modeFromPacket)){
            this.mode = modeFromPacket;
        }else {
            throw new TFTPPacketException("TFTP mode is not recognized");
        }
    }

    /**
     * create a request packet
     * @return new DatagramPacket
     */
    public DatagramPacket createPacket(){
        int fileLength, modeLength;
        byte[] data;

        fileLength = this.filename.length();//get the filename length
        modeLength = this.mode.length();//get the mode length

        data = new byte[fileLength + modeLength + 4];//2 bytes of opcode + filename length + 1 byte of 0 + mode length + 1 byte of 0
        data[0] = 0;//first byte of opcode
        data[1] = (byte)this.getOpcode();//second byte of opcode
        System.arraycopy(this.filename.getBytes(), 0, data, 2, fileLength);//bytes of filename
        data[fileLength + 2] = 0;//1 byte of 0 as the delimiter of filename
        System.arraycopy(this.mode.getBytes(), 0, data, fileLength + 3,modeLength);//bytes of mode
        data[fileLength + modeLength + 3] = 0;//1 byte of 0 as the delimiter of mode

        return new DatagramPacket(data, data.length, this.address, this.getTid());
    }
    /**
     * get the filename of the RRQ/WRQ packet
     * @return
     */
    public String getFilename() {
        return filename;
    }

    /**
     * get the mode of the RRQ/WRQ packet
     * @return
     */
    public String getMode() {
        return mode;
    }
}
