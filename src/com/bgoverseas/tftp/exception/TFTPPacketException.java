package com.bgoverseas.tftp.exception;

/**
 * Created by binwang on 15/08/15.
 * TFTP packet exception
 */
public class TFTPPacketException extends TFTPBaseException {
    /**
     * the exception message used to attach for every TFTPPacket Exception
     */
    public static final String APPENDED_PACKET_EXCEPTION_MESSAGE = "PACKET EXCEPTION";

    /**
     * call parent constructor with the default exception message
     */
    public TFTPPacketException(){
        super(TFTPPacketException.APPENDED_PACKET_EXCEPTION_MESSAGE);
    }

    /**
     * call parent constructor combining the input message and the default message
     * @param message
     */
    public TFTPPacketException(String message){
        super(message + TFTPPacketException.APPENDED_PACKET_EXCEPTION_MESSAGE);
    }
}
