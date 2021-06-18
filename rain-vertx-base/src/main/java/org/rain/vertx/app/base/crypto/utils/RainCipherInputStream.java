package org.rain.vertx.app.base.crypto.utils;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NullCipher;

/**
 * Drop-in replacement for {@link CipherInputStream} with some enhancements/differences:
 *
 * <ul>
 * <li>Control whether the underlying input stream should also be closed or remain open when
 * {@link #close()} is called.</li>
 * <li>{@link BadPaddingException} and {@link IllegalBlockSizeException} is wrapped inside
 * {@link CipherException} and re-thrown.</li>
 * <li>{@link IOException} is not swallowed.</li>
 * </ul>
 *
 * @author JustIn <cnrainbing@gmail.com>
 * @since 1.1.0
 * @see CipherInputStream
 */
public class RainCipherInputStream extends FilterInputStream {
    private Cipher cipher;
    private InputStream input;
    private boolean noMoreInput = false;
    private byte[] obuffer;
    private int ostart = 0;
    private int ofinish = 0;
    private boolean closed = false;
    private boolean closeInput = false;

    /**
     * Construct a new {@link RainCipherInputStream} from an {@link InputStream} and a
     * {@link Cipher}.
     *
     * @param input
     * @param cipher
     * @param closeInput
     *            if {@code true} calling {@link #close()} will also close the underlying input
     *            stream, if {@code false} the underlying input stream remains open
     */
    public RainCipherInputStream(InputStream input, Cipher cipher, boolean closeInput) {
        super(input);
        this.input = input;
        this.cipher = cipher;
        this.closeInput = closeInput;
    }

    /**
     * Construct a new {@link RainCipherInputStream} from an {@link InputStream}, using
     * {@link NullCipher}.
     *
     * @param input
     * @param closeInput
     *            if {@code true} calling {@link #close()} will also close the underlying input
     *            stream, if {@code false} the underlying input stream remains open
     */
    protected RainCipherInputStream(InputStream input, boolean closeInput) {
        this(input, new NullCipher(), closeInput);
    }

    /**
     * Load data to internal buffer, return the number of bytes read, or {@code -1} if no more data
     * to read.
     *
     * @return
     * @throws IOException
     */
    private int getMoreData() throws IOException {
        if (noMoreInput) {
            return -1;
        } else {
            byte[] buffer = new byte[1024];
            int bytesRead = input.read(buffer);
            if (bytesRead == -1) {
                noMoreInput = true;
                try {
                    obuffer = cipher.doFinal();
                } catch (IllegalBlockSizeException | BadPaddingException e) {
                    throw new CipherException(e);
                }
                if (obuffer == null) {
                    return -1;
                } else {
                    ostart = 0;
                    return ofinish = obuffer.length;
                }
            } else {
                obuffer = cipher.update(buffer, 0, bytesRead);
                ostart = 0;
                return ofinish = (obuffer == null ? 0 : obuffer.length);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int read() throws IOException {
        if (ostart >= ofinish) {
            int temp;
            for (temp = 0; temp == 0; temp = getMoreData())
                ;
            if (temp == -1) {
                return -1;
            }
        }
        return obuffer[ostart++] & 255;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int read(byte[] b, int startOffset, int length) throws IOException {
        int temp;
        if (ostart >= ofinish) {
            for (temp = 0; temp == 0; temp = getMoreData())
                ;
            if (temp == -1) {
                return -1;
            }
        }

        if (length <= 0) {
            return 0;
        } else {
            temp = ofinish - ostart;
            if (length < temp) {
                temp = length;
            }
            System.arraycopy(obuffer, ostart, b, startOffset, temp);
            ostart += temp;
            return temp;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long skip(long n) throws IOException {
        int temp = ofinish - ostart;
        if (n > temp) {
            n = temp;
        }
        if (n < 0) {
            return 0;
        } else {
            ostart = (int) (ostart + n);
            return n;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int available() throws IOException {
        return ofinish - ostart;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException {
        if (!closed) {
            closed = true;
            if (closeInput) {
                super.close();
            }
            if (!noMoreInput) {
                try {
                    cipher.doFinal();
                } catch (IllegalBlockSizeException | BadPaddingException e) {
                    throw new CipherException(e);
                }
            }
            ostart = ofinish = 0;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean markSupported() {
        return false;
    }
}