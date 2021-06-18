package org.rain.vertx.app.base.crypto.utils;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NullCipher;

/**
 * Drop-in replacement for {@link CipherOutputStream} with some enhancements/differences:
 *
 * <ul>
 * <li>Control whether the underlying output stream should also be closed or remain open when
 * {@link #close()} is called.</li>
 * <li>{@link BadPaddingException} and {@link IllegalBlockSizeException} is wrapped inside
 * {@link CipherException} and re-thrown.</li>
 * <li>{@link IOException} is not swallowed.</li>
 * </ul>
 *
 * @author JustIn <cnrainbing@gmail.com>
 * @since 1.1.0
 * @see CipherOutputStream
 */
public class RainCipherOutputStream extends FilterOutputStream {
    private byte[] ibuffer = new byte[1];
    private Cipher cipher;
    private OutputStream output;
    private boolean closed = false;
    private boolean closeOutput = false;

    /**
     * Construct a new {@link CipherOutputStream} from an {@link OutputStream} and a {@link Cipher}.
     *
     * @param output
     * @param cipher
     * @param closeOutput
     *            if {@code true} calling {@link #close()} will also close the underlying output
     *            stream, if {@code false} the underlying output stream remains open
     */
    public RainCipherOutputStream(OutputStream output, Cipher cipher, boolean closeOutput) {
        super(output);
        this.output = output;
        this.cipher = cipher;
        this.closeOutput = closeOutput;
    }

    /**
     * Construct a new {@link RainCipherOutputStream} from an {@link OutputStream}, using
     * {@link NullCipher}.
     *
     * @param output
     * @param closeOutput
     *            if {@code true} calling {@link #close()} will also close the underlying output
     *            stream, if {@code false} the underlying output stream remains open
     */
    protected RainCipherOutputStream(OutputStream output, boolean closeOutput) {
        this(output, new NullCipher(), closeOutput);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(int value) throws IOException {
        ibuffer[0] = (byte) value;
        byte[] buffer = cipher.update(ibuffer, 0, 1);
        if (buffer != null) {
            output.write(buffer);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(byte[] value) throws IOException {
        write(value, 0, value.length);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(byte[] value, int offset, int length) throws IOException {
        byte[] buffer = cipher.update(value, offset, length);
        if (buffer != null) {
            output.write(buffer);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException {
        if (!closed) {
            closed = true;
            try {
                byte[] buffer = cipher.doFinal();
                if (buffer != null) {
                    output.write(buffer);
                }
            } catch (IllegalBlockSizeException | BadPaddingException e) {
                throw new CipherException(e);
            } finally {
                if (closeOutput) {
                    super.close();
                } else {
                    flush();
                }
            }
        }
    }
}
