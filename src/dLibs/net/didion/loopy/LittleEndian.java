// Decompiled by DJ v3.10.10.93 Copyright 2007 Atanas Neshkov  Date: 29/11/2017 23:43:44
// Home Page: http://members.fortunecity.com/neshkov/dj.html http://www.neshkov.com/dj.html - Check often for new version!
// Decompiler options: packimports(3) deadcode 
// Source File Name:   LittleEndian.java
package dLibs.net.didion.loopy;

public class LittleEndian {

    public LittleEndian()
    {
    }

    public static int getUInt8(byte src[], int offset)
    {
        return src[offset] & 0xff;
    }

    public static int getInt8(byte src[], int offset)
    {
        return src[offset];
    }

    public static int getUInt16(byte src[], int offset)
    {
        int v0 = src[offset] & 0xff;
        int v1 = src[offset + 1] & 0xff;
        return v1 << 8 | v0;
    }

    public static long getUInt32(byte src[], int offset)
    {
        long v0 = src[offset] & 0xff;
        long v1 = src[offset + 1] & 0xff;
        long v2 = src[offset + 2] & 0xff;
        long v3 = src[offset + 3] & 0xff;
        return v3 << 24 | v2 << 16 | v1 << 8 | v0;
    }
}