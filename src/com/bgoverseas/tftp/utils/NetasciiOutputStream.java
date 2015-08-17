package com.bgoverseas.tftp.utils;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by stevenwang on 17/08/15.
 * create a netascii output stream
 */
public class NetasciiOutputStream  extends FilterOutputStream {
    private boolean isCrLast;

    public NetasciiOutputStream(OutputStream output){
        super(output);
        this.isCrLast = false;
    }

    public synchronized void write(int ch) throws IOException {
        if (NetasciiInputStream.IS_CONVERSION_NEEDED)
        {
            this.writeChar(ch);
        }else{
            out.write(ch);
            return ;
        }
    }

    public synchronized void write(byte data[])
            throws IOException
    {
        write(data, 0, data.length);
    }

    public synchronized void write(byte data[], int offset, int length)
            throws IOException
    {
        if (NetasciiInputStream.IS_CONVERSION_NEEDED){
            while (length-- > 0)
                this.writeChar(data[offset++]);
        }else{
            out.write(data, offset, length);
            return ;
        }
    }

    public synchronized void close()
            throws IOException
    {
        if (NetasciiInputStream.IS_CONVERSION_NEEDED){
            if (this.isCrLast)
                out.write('\r');
            super.close();
        }else {
            super.close();
            return ;
        }
    }

    private void writeChar(int ch) throws IOException {
        switch (ch)
        {
            case '\r':
                this.isCrLast = true;
                break;
            case '\n':
                if (this.isCrLast)
                {
                    out.write(NetasciiInputStream.LINE_SEPARATOR.getBytes());
                    this.isCrLast = false;
                    break;
                }else {
                    this.isCrLast = false;
                    out.write('\n');
                    break;
                }
            default:
                if (this.isCrLast)
                {
                    out.write('\r');
                    this.isCrLast = false;
                }
                out.write(ch);
                break;
        }
    }
}
