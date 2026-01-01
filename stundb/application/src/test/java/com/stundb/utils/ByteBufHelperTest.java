package com.stundb.utils;

import static org.junit.jupiter.api.Assertions.*;

import io.netty.buffer.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.stream.Stream;

@ExtendWith(MockitoExtension.class)
public class ByteBufHelperTest {

    private static Stream<Arguments> of_shouldReturnAnEmptyByteBuf_whenInvalidInputArguments() {
        return Stream.of(
                Arguments.of((Object) new byte[] {}),
                Arguments.of((Object) null),
                Arguments.of("string"));
    }

    private static Stream<Arguments> from_shouldReturnNull_whenByteBufIsInvalidArguments() {
        return Stream.of(
                Arguments.of((Object) null),
                Arguments.of(UnpooledByteBufAllocator.DEFAULT.directBuffer()));
    }

    @Test
    void of_shouldReturnAnUnpooledHeapByteBuf_whenByteArrayLengthIsSmallerThanThreshold() {
        var buffer = ByteBufHelper.of(new byte[] {0, 1, 2, 3});

        assertEquals(4, buffer.readableBytes());
        assertInstanceOf(UnpooledHeapByteBuf.class, buffer);
    }

    @ParameterizedTest
    @MethodSource("of_shouldReturnAnEmptyByteBuf_whenInvalidInputArguments")
    void of_shouldReturnAnEmptyByteBuf_whenInvalidInput(Object input) {
        var buffer = ByteBufHelper.of(input);

        assertEquals(0, buffer.readableBytes());
        assertInstanceOf(EmptyByteBuf.class, buffer);
    }

    @Test
    void of_shouldReturnAPooledByteBuf_whenByteArrayLengthIsBiggerThanThreshold()
            throws ClassNotFoundException {
        var buffer = ByteBufHelper.of(new byte[128_000]);

        assertEquals(128_000, buffer.readableBytes());
        assertInstanceOf(Class.forName("io.netty.buffer.PooledUnsafeDirectByteBuf"), buffer);
    }

    @Test
    void from_shouldReturnAByteArray_whenByteBufIsValid() {
        var bytes = ByteBufHelper.from(Unpooled.wrappedBuffer(new byte[] {0, 1}));

        assertEquals(2, bytes.length);
    }

    @ParameterizedTest
    @MethodSource("from_shouldReturnNull_whenByteBufIsInvalidArguments")
    void from_shouldReturnNull_whenByteBufIsInvalid(ByteBuf buffer) throws ClassNotFoundException {
        var bytes = ByteBufHelper.from(buffer);

        if (buffer == null) {
            assertNull(bytes);
            return;
        }
        assertInstanceOf(
                Class.forName(
                        "io.netty.buffer.UnpooledByteBufAllocator$InstrumentedUnpooledUnsafeDirectByteBuf"),
                buffer);
    }

    @Test
    void release_shouldBeSuccessful_whenByteBufIsValid() {
        var bytes = new byte[65_000];
        var buffer = Unpooled.wrappedBuffer(bytes);

        assertEquals(1, buffer.refCnt());
        ByteBufHelper.release(buffer);
        assertEquals(0, buffer.refCnt());
    }

    @Test
    void release_shouldMaintainRefCnt_whenByteBufIsInvalid() {
        // just checks no exception is thrown
        ByteBufHelper.release(null);

        var buffer = Unpooled.EMPTY_BUFFER;

        ByteBufHelper.release(buffer);

        assertEquals(1, buffer.refCnt());
        ByteBufHelper.release(buffer);
        assertEquals(1, buffer.refCnt());
    }
}
