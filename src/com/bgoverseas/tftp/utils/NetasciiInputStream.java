package com.bgoverseas.tftp.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;

/**
 * Created by stevenwang on 17/08/15.
 * create a netascii input stream
 */
public class NetasciiInputStream extends PushbackInputStream {
    public static final boolean IS_CONVERSION_NEEDED;
    public static final String LINE_SEPARATOR;

    static {
        LINE_SEPARATOR = System.getProperty("line.separator");
        IS_CONVERSION_NEEDED = !LINE_SEPARATOR.equals("\r\n");
    }

    private int length;

    public NetasciiInputStream(InputStream input){
        super(input, NetasciiInputStream.LINE_SEPARATOR.getBytes().length + 1);
    }

    public int read(byte buffer[], int offset, int length) throws IOException
    {
        if (NetasciiInputStream.IS_CONVERSION_NEEDED){
            if (length < 1)
                return 0;

            int ch, off;

            ch = available();

            this.length = (length > ch ? ch : length);

            if (this.length < 1)
                this.length = 1;


            if ((ch = this.readChar()) == -1)
                return -1;

            off = offset;

            do
            {
                buffer[offset++] = (byte)ch;
            }
            while (--this.length > 0 && (ch = this.readChar()) != -1);


            return (offset - off);
        }else {
            return super.read(buffer, offset, length);
        }
    }

    public int read() throws IOException
    {
        if (NetasciiInputStream.IS_CONVERSION_NEEDED){
            return this.readChar();
        }else {
            return super.read();
        }
    }

    public int read(byte data[]) throws IOException
    {
        return read(data, 0, data.length);
    }

    public int available() throws IOException
    {
        if (in == null) {
            throw new IOException("Stream closed");
        }
        return (buf.length - pos) + in.available();
    }

    private int readChar() throws IOException {
        int ch;

        ch = super.read();

        if (ch == '\r')
        {
            ch = super.read();
            if (ch == '\n')
            {
                unread(NetasciiInputStream.LINE_SEPARATOR.getBytes());
                ch = super.read();
                --this.length;
            }
            else
            {
                if (ch != -1)
                    unread(ch);
                return '\r';
            }
        }

        return ch;
    }
}
