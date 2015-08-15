package com.bgoverseas.tftp.exception;

import com.bgoverseas.tftp.packet.TFTPBasePacket;

/**
 * Created by binwang on 15/08/15.
 * TFTP base exception
 */
public abstract class TFTPBaseException extends Exception {
    /**
     * call the constructor of super class
     */
    public TFTPBaseException(){
        super();
    }

    /**
     * call the constructor of super class passing the exception message
     * @param message
     */
    public TFTPBaseException(String message){
        super(message);
    }
}
