package com.atharion.commons.utils.function;

import java.io.DataInputStream;
import java.io.IOException;

public final class InputStreams {
    
    public InputStreams() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }

    public static short readShort(DataInputStream dis) throws IOException {
        int b1 = dis.readUnsignedByte();
        int b2 = dis.readUnsignedByte();

        return (short) (b1 + (b2 << 8));
    }

    /**
     * Read an integer value in little-endian (32bit)
     *
     * @param dis The data input stream
     * @return Read value in the stream
     * @throws IOException Throwed if the method cannot read an integer at the stream cursor
     */
    public static int readInt(DataInputStream dis) throws IOException {
        int b1 = dis.readUnsignedByte();
        int b2 = dis.readUnsignedByte();
        int b3 = dis.readUnsignedByte();
        int b4 = dis.readUnsignedByte();

        return b1 + (b2 << 8) + (b3 << 16) + (b4 << 24);
    }

    public static int[] readInt(DataInputStream dis, int amount) throws IOException {
        int[] values = new int[amount];
        for (int i = 0; i < amount; i++) {
            values[i] = readInt(dis);
        }
        return values;
    }

    /**
     * Read a string, byte by byte.
     *
     * @param dis The data input stream
     * @return Read value in the stream
     * @throws IOException Throwed if the method cannot read a string at the stream cursor
     */
    public static String readString(DataInputStream dis) throws IOException {
        int length = readInt(dis);

        StringBuilder sb = new StringBuilder();

        while (length > 0) {
            char c = (char) dis.readByte();
            sb.append(c);
            length--;
        }

        return sb.toString();
    }
}
