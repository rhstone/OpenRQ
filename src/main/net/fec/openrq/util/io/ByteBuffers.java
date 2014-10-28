/*
 * Copyright 2014 OpenRQ Team
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.fec.openrq.util.io;


import java.nio.Buffer;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.Objects;


/**
 * Class containing utility methods for {@link ByteBuffer} objects.
 */
public final class ByteBuffers {

    /**
     * Allocates a new buffer.
     * 
     * @param capacity
     *            The capacity of the new buffer
     * @param isDirect
     *            Whether the new buffer will be {@linkplain Buffer#isDirect() direct}
     * @return a new allocated buffer
     */
    public static ByteBuffer allocate(int capacity, boolean isDirect) {

        return isDirect ? ByteBuffer.allocateDirect(capacity) : ByteBuffer.allocate(capacity);
    }

    /**
     * Copies the contents of a buffer to a newly created one.
     * <p>
     * Calling this method has the same effect as calling {@link #copyBuffer(ByteBuffer, int, BufferOperation)
     * copyBuffer(buf, buf.remaining(), BufferOperation.ADVANCE_POSITION)}.
     * 
     * @param buf
     *            The buffer to be copied
     * @return a copy of a buffer
     */
    public static ByteBuffer copyBuffer(ByteBuffer buf) {

        return copyBuffer(buf, buf.remaining(), BufferOperation.ADVANCE_POSITION);
    }

    /**
     * Copies the contents of a buffer to a newly created one.
     * <p>
     * Calling this method has the same effect as calling {@link #copyBuffer(ByteBuffer, int, BufferOperation)
     * copyBuffer(buf, copySize, BufferOperation.ADVANCE_POSITION)}.
     * 
     * @param buf
     *            The buffer to be copied
     * @param copySize
     *            The number of bytes to be copied from the source buffer
     * @return a copy of a buffer
     * @exception IllegalArgumentException
     *                If the number of bytes to be copied is negative
     * @exception BufferUnderflowException
     *                If the number of bytes to be copied exceeds the number of {@linkplain Buffer#remaining()
     *                available} bytes in the source buffer
     */
    public static ByteBuffer copyBuffer(ByteBuffer buf, int copySize) {

        return copyBuffer(buf, copySize, BufferOperation.ADVANCE_POSITION);
    }

    /**
     * Copies the contents of a buffer to a newly created one.
     * <p>
     * Calling this method has the same effect as calling {@link #copyBuffer(ByteBuffer, int, BufferOperation)
     * copyBuffer(buf, buf.remaining(), op)}.
     * 
     * @param buf
     *            The buffer to be copied
     * @param op
     *            The operation to apply to the source buffer after being copied
     * @return a copy of a buffer
     */
    public static ByteBuffer copyBuffer(ByteBuffer buf, BufferOperation op) {

        return copyBuffer(buf, buf.remaining(), op);
    }

    /**
     * Copies the contents of a buffer to a newly created one. The buffer copy will have the same
     * {@linkplain ByteBuffer#order() byte order} and will only be {@linkplain ByteBuffer#isDirect() direct} if the
     * source buffer also is.
     * <p>
     * The number of bytes to be copied is specified as a parameter. At the end of the copy, the position of the source
     * buffer will have advanced the specified number of bytes (before the provided buffer operation is applied to the
     * source buffer).
     * 
     * @param buf
     *            The buffer to be copied
     * @param copySize
     *            The number of bytes to be copied from the source buffer
     * @param op
     *            The operation to apply to the source buffer after being copied
     * @return a copy of a buffer
     * @exception IllegalArgumentException
     *                If the number of bytes to be copied is negative
     * @exception BufferUnderflowException
     *                If the number of bytes to be copied exceeds the number of {@linkplain Buffer#remaining()
     *                available} bytes in the source buffer
     */
    public static ByteBuffer copyBuffer(ByteBuffer buf, int copySize, BufferOperation op) {

        final int srcPos = buf.position();
        final int srcLim = buf.limit();
        final int remaining = srcLim - srcPos;

        if (copySize < 0) throw new IllegalArgumentException("number of bytes to copy is negative");
        if (remaining < copySize) throw new BufferUnderflowException();
        Objects.requireNonNull(op);

        ByteBuffer copy = allocate(copySize, buf.isDirect()).order(buf.order());
        try {
            buf.limit(srcPos + copySize);
            copy.put(buf);
            copy.rewind();
        }
        finally {
            buf.limit(srcLim); // always restore the original limit
        }

        // only apply the operation if no exception was previously thrown
        op.apply(buf, srcPos, buf.position());
        return copy;
    }

    /**
     * Copies the contents of a source buffer into a destination buffer. The number of bytes to copy is the number of
     * {@linkplain Buffer#remaining() available} bytes in the source buffer.
     * <p>
     * Calling this method has the same effect as calling
     * {@link #copyBuffer(ByteBuffer, BufferOperation, ByteBuffer, BufferOperation, int)
     * copyBuffer(src, BufferOperation.ADVANCE_POSITION, dst, BufferOperation.ADVANCE_POSITION, src.remaining())}.
     * 
     * @param src
     *            The source buffer
     * @param dst
     *            The destination buffer
     * @exception BufferOverflowException
     *                If the number of {@linkplain Buffer#remaining() available} bytes in the source buffer exceeds the
     *                number of available bytes in the destination buffer
     */
    public static void copyBuffer(ByteBuffer src, ByteBuffer dst) {

        copyBuffer(src, BufferOperation.ADVANCE_POSITION, dst, BufferOperation.ADVANCE_POSITION, src.remaining());
    }

    /**
     * Copies the contents of a source buffer into a destination buffer. The number of bytes to copy is the number of
     * {@linkplain Buffer#remaining() available} bytes in the source buffer.
     * <p>
     * Calling this method has the same effect as calling
     * {@link #copyBuffer(ByteBuffer, BufferOperation, ByteBuffer, BufferOperation, int)
     * copyBuffer(src, srcOp, dst, dstOp, src.remaining())}.
     * 
     * @param src
     *            The source buffer
     * @param srcOp
     *            The operation to apply to the source buffer after the copy
     * @param dst
     *            The destination buffer
     * @param dstOp
     *            The operation to apply to the destination buffer after the copy
     * @exception BufferOverflowException
     *                If the number of {@linkplain Buffer#remaining() available} bytes in the source buffer exceeds the
     *                number of available bytes in the destination buffer
     */
    public static void copyBuffer(ByteBuffer src, BufferOperation srcOp, ByteBuffer dst, BufferOperation dstOp) {

        copyBuffer(src, srcOp, dst, dstOp, src.remaining());
    }

    /**
     * Copies the contents of a source buffer into a destination buffer.
     * <p>
     * Calling this method has the same effect as calling
     * {@link #copyBuffer(ByteBuffer, BufferOperation, ByteBuffer, BufferOperation, int)
     * copyBuffer(src, BufferOperation.ADVANCE_POSITION, dst, BufferOperation.ADVANCE_POSITION, copySize)}.
     * 
     * @param src
     *            The source buffer
     * @param dst
     *            The destination buffer
     * @param copySize
     *            The number of bytes to copy
     * @exception IllegalArgumentException
     *                If the number of bytes to copy is negative
     * @exception BufferUnderflowException
     *                If the number of bytes to copy exceeds the number of {@linkplain Buffer#remaining()
     *                available} bytes in the source buffer
     * @exception BufferOverflowException
     *                If the number of bytes to copy exceeds the number of {@linkplain Buffer#remaining()
     *                available} bytes in the destination buffer
     */
    public static void copyBuffer(ByteBuffer src, ByteBuffer dst, int copySize) {

        copyBuffer(src, BufferOperation.ADVANCE_POSITION, dst, BufferOperation.ADVANCE_POSITION, copySize);
    }

    /**
     * Copies the contents of a source buffer into a destination buffer.
     * <p>
     * The number of bytes to be copied is specified as a parameter. At the end of the copy, the positions of the source
     * and destination buffers will have advanced the specified number of bytes (before the provided buffer operations
     * are applied to each buffer).
     * 
     * @param src
     *            The source buffer
     * @param srcOp
     *            The operation to apply to the source buffer after the copy
     * @param dst
     *            The destination buffer
     * @param dstOp
     *            The operation to apply to the destination buffer after the copy
     * @param copySize
     *            The number of bytes to copy
     * @exception IllegalArgumentException
     *                If the number of bytes to copy is negative
     * @exception BufferUnderflowException
     *                If the number of bytes to copy exceeds the number of {@linkplain Buffer#remaining()
     *                available} bytes in the source buffer
     * @exception BufferOverflowException
     *                If the number of bytes to copy exceeds the number of {@linkplain Buffer#remaining()
     *                available} bytes in the destination buffer
     */
    public static void copyBuffer(
        ByteBuffer src,
        BufferOperation srcOp,
        ByteBuffer dst,
        BufferOperation dstOp,
        int copySize)
    {

        final int srcPos = src.position();
        final int srcLim = src.limit();
        final int srcRemaining = srcLim - srcPos;

        final int dstPos = dst.position();
        final int dstLim = dst.limit();
        final int dstRemaining = dstLim - dstPos;

        if (copySize < 0) throw new IllegalArgumentException("number of bytes to copy is negative");
        if (srcRemaining < copySize) throw new BufferUnderflowException();
        if (dstRemaining < copySize) throw new BufferOverflowException();
        Objects.requireNonNull(srcOp);
        Objects.requireNonNull(dstOp);

        try {
            src.limit(srcPos + copySize);
            dst.limit(dstPos + copySize);
            dst.put(src);
        }
        finally {
            // always restore the original limits
            src.limit(srcLim);
            dst.limit(dstLim);
        }

        // only apply the operations if no exception was previously thrown
        srcOp.apply(src, srcPos, src.position());
        dstOp.apply(dst, dstPos, dst.position());
    }

    /**
     * Puts zeros in the provided buffer, starting at the current buffer position.
     * <p>
     * Calling this method has the same effect as calling {@link #putZeros(ByteBuffer, int, BufferOperation)
     * putZeros(dst, dst.remaining(), BufferOperation.ADVANCE_POSITION)}.
     * 
     * @param dst
     *            The buffer to put zeros into
     */
    public static void putZeros(ByteBuffer dst) {

        putZeros(dst, dst.remaining(), BufferOperation.ADVANCE_POSITION);
    }

    /**
     * Puts zeros in the provided buffer, starting at the current buffer position.
     * <p>
     * Calling this method has the same effect as calling {@link #putZeros(ByteBuffer, int, BufferOperation)
     * putZeros(dst, numZeros, BufferOperation.ADVANCE_POSITION)}.
     * 
     * @param dst
     *            The buffer to put zeros into
     * @param numZeros
     *            The number of zeros to put
     * @exception IllegalArgumentException
     *                If the number of zeros is negative
     * @exception BufferOverflowException
     *                If the number of zeros exceeds the number of {@linkplain Buffer#remaining() available} bytes in
     *                the buffer
     */
    public static void putZeros(ByteBuffer dst, int numZeros) {

        putZeros(dst, numZeros, BufferOperation.ADVANCE_POSITION);
    }

    /**
     * Puts zeros in the provided buffer, starting at the current buffer position.
     * <p>
     * Calling this method has the same effect as calling {@link #putZeros(ByteBuffer, int, BufferOperation)
     * putZeros(dst, dst.remaining(), op)}.
     * 
     * @param dst
     *            The buffer to put zeros into
     * @param op
     *            The operation to apply to the buffer after the put
     */
    public static void putZeros(ByteBuffer dst, BufferOperation op) {

        putZeros(dst, dst.remaining(), op);
    }

    /**
     * Puts zeros in the provided buffer, starting at the current buffer position.
     * <p>
     * At the end of the put, the position of the buffer will have advanced the specified number of zeros (before the
     * provided buffer operation is applied to the buffer).
     * 
     * @param dst
     *            The buffer to put zeros into
     * @param numZeros
     *            The number of zeros to put
     * @param op
     *            The operation to apply to the buffer after the put
     * @exception IllegalArgumentException
     *                If the number of zeros is negative
     * @exception BufferOverflowException
     *                If the number of zeros exceeds the number of {@linkplain Buffer#remaining() available} bytes in
     *                the buffer
     */
    public static void putZeros(ByteBuffer dst, final int numZeros, BufferOperation op) {

        final int pos = dst.position();
        final int lim = dst.limit();
        final int remaining = lim - pos;

        if (numZeros < 0) throw new IllegalArgumentException("number of zeros is negative");
        if (remaining < numZeros) throw new BufferOverflowException();
        Objects.requireNonNull(op);

        int remZeros = numZeros;
        while (remZeros > 0) {
            final int amount = Math.min(remZeros, ZERO_BUFFER_CAPACITY);
            dst.put(zeroBuffer(amount, dst.isDirect()));
            remZeros -= amount;
        }

        op.apply(dst, pos, dst.position());
    }


    private static final int ZERO_BUFFER_CAPACITY = 4096;
    private static final ThreadLocal<ByteBuffer> ZERO_BUFFER = new ThreadLocal<ByteBuffer>() {

        @Override
        protected ByteBuffer initialValue() {

            return ByteBuffer.allocate(ZERO_BUFFER_CAPACITY);
        }
    };
    private static final ThreadLocal<ByteBuffer> ZERO_DIR_BUFFER = new ThreadLocal<ByteBuffer>() {

        @Override
        protected ByteBuffer initialValue() {

            return ByteBuffer.allocateDirect(ZERO_BUFFER_CAPACITY);
        }
    };


    /*
     * Requires size <= ZERO_BUFFER_CAPACITY
     */
    private static ByteBuffer zeroBuffer(int size, boolean direct) {

        ByteBuffer zeroBuf = direct ? ZERO_DIR_BUFFER.get() : ZERO_BUFFER.get();
        zeroBuf.clear();
        zeroBuf.limit(size);
        return zeroBuf;
    }

    private ByteBuffers() {

        // not instantiable
    }
}
