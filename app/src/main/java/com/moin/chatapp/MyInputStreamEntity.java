package com.moin.chatapp;


import org.apache.http.entity.InputStreamEntity;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by macpro on 9/16/16.
 */
public class MyInputStreamEntity extends InputStreamEntity {

    private final ProgressListener listener;

    public MyInputStreamEntity(InputStream instream, long length,final ProgressListener listener) {
        super(instream, length);
        this.listener = listener;
    }


    @Override
    public void writeTo(OutputStream outstream) throws IOException {
        super.writeTo(new CountingOutputStream(outstream, this.listener));
    }

    public static class CountingOutputStream extends FilterOutputStream
    {

        private final ProgressListener listener;
        private long transferred;

        public CountingOutputStream(final OutputStream out, final ProgressListener listener)
        {
            super(out);
            this.listener = listener;
            this.transferred = 0;
        }

        public void write(byte[] b, int off, int len) throws IOException
        {
            out.write(b, off, len);
            this.transferred += len;
            this.listener.transferred(this.transferred);
        }

        public void write(int b) throws IOException
        {
            out.write(b);
            this.transferred++;
            this.listener.transferred(this.transferred);
        }
    }

    public static interface ProgressListener
    {
        void transferred(long num);
    }
}
