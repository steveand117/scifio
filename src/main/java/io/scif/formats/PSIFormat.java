/*
 * #%L
 * SCIFIO library for reading and converting scientific file formats.
 * %%
 * Copyright (C) 2011 - 2020 SCIFIO developers.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package io.scif.formats;

import io.scif.AbstractChecker;
import io.scif.AbstractFormat;
import io.scif.AbstractMetadata;
import io.scif.AbstractParser;
import io.scif.ByteArrayPlane;
import io.scif.ByteArrayReader;
import io.scif.Format;
import io.scif.FormatException;
import io.scif.HasColorTable;
import io.scif.ImageMetadata;
import io.scif.MetaTable;
import io.scif.MetadataLevel;
import io.scif.UnsupportedCompressionException;
import io.scif.codec.BitBuffer;
import io.scif.config.SCIFIOConfig;
import io.scif.util.FormatTools;
import io.scif.util.ImageTools;

import java.io.IOException;

import net.imagej.axis.Axes;
import net.imglib2.Interval;
import net.imglib2.display.ColorTable;
import net.imglib2.display.ColorTable8;

import org.scijava.io.handle.DataHandle;
import org.scijava.io.handle.DataHandle.ByteOrder;
import org.scijava.io.location.Location;
import org.scijava.plugin.Plugin;

/**
 * BMPReader is the file format reader for
 * <a href="https://en.wikipedia.org/wiki/BMP_file_format">Microsoft Bitmap
 * (BMP)</a> files.
 *
 * @author Mark Hiner
 */
@Plugin(type = Format.class, name = "Windows Bitmap")
public class PSIFormat extends AbstractFormat {

    // -- Constants --

    public static final String BMP_MAGIC_STRING = "BM";

    // -- Compression types --

    private static final int RAW = 0;

    private static final int RLE_8 = 1;

    private static final int RLE_4 = 2;

    private static final int RGB_MASK = 3;

    // -- AbstractFormat Methods --

    @Override
    protected String[] makeSuffixArray() {
        return new String[] { "psi" };
    }

    // -- Nested Classes --

    public static class Metadata extends AbstractMetadata implements
            HasColorTable
    {

        // -- Fields --

        /** The palette for indexed color images. */
        private ColorTable8 palette;

        /** Compression type */
        private int compression;

        /** Offset to image data. */
        private long global;

        private boolean invertY = false;

        // -- Getters and Setters --

        public int getCompression() {
            return compression;
        }

        public void setCompression(final int compression) {
            this.compression = compression;
        }

        public long getGlobal() {
            return global;
        }

        public void setGlobal(final long global) {
            this.global = global;
        }

        public boolean isInvertY() {
            return invertY;
        }

        public void setInvertY(final boolean invertY) {
            this.invertY = invertY;
        }

        // -- Metadata API Methods --

        @Override
        public void populateImageMetadata() {
            // Create an ImageMetadata for each image in the dataset
            createImageMetadata(2);

            // Get the ImageMetadata of the first image, for modification
            final ImageMetadata iMeta = get(0);
            final ImageMetadata iMeta2 = get(0);

            // Add X and Y axes.
            // There are many ways to populate the axes.
            // See the ImageMetadata API for more details
//            iMeta.addAxis(Axes.X, 25);
//            iMeta.addAxis(Axes.Y, 25);
//            iMeta2.addAxis(Axes.X, 25);
//            iMeta2.addAxis(Axes.Y, 25);
        }

        // -- HasSource API Methods --

        @Override
        public void close(final boolean fileOnly) throws IOException {
            super.close(fileOnly);

            if (!fileOnly) {
                compression = 0;
                global = 0;
                palette = null;
                invertY = false;
            }
        }

        // -- HasColorTable API Methods --

        @Override
        public ColorTable getColorTable(final int imageIndex,
                                        final long planeIndex)
        {
            return palette;
        }
    }

//    public static class Checker extends AbstractChecker {
//
////        @Override
////        public boolean isFormat(final DataHandle<Location> stream)
////                throws IOException
////        {
////            final int blockLen = 2;
////            if (!FormatTools.validStream(stream, blockLen, false)) return false;
////            return stream.readString(blockLen).startsWith(BMP_MAGIC_STRING);
////        }
//    }

    public static class Parser extends AbstractParser<Metadata> {

        @Override
        protected void typedParse(final DataHandle<Location> stream,
                                  final Metadata meta, final SCIFIOConfig config) throws IOException,
                FormatException
        {
            meta.createImageMetadata(1);

            final ImageMetadata iMeta = meta.get(0);
            final MetaTable globalTable = meta.getTable();

            stream.setOrder(ByteOrder.BIG_ENDIAN);
//            System.out.println("First 4 byte string:" + getSource().readString(100));
            System.out.println("First 4 byte string:" + getSource().readString(4));
            System.out.println("Second 8 byte string:" + getSource().readString(8));
            System.out.println(" 2 byte string:" + getSource().readString(2));
            System.out.println(getSource().offset());
            System.out.println("12 byte string:" + getSource().readString(12));
            System.out.println("heading in degrees:" + getSource().readFloat());
            System.out.println("Lane index:" + getSource().readByte());
            System.out.println("Serial Number:" + getSource().readInt());
            System.out.println("GPS Longitutde:" + getSource().readDouble());
            System.out.println("GPS Latitude:" + getSource().readDouble());
            System.out.println(getSource().offset());
            getSource().seek(69);
            System.out.println("2D Pixel Storage Order(uint8):" + getSource().readByte());
            System.out.println("2D Codec(uint8):" + getSource().readByte());
            System.out.println("2D Longitudinal Resolution (Float32):" + getSource().readFloat());
            System.out.println("2D Transverse Resolution (Float32):" + getSource().readFloat());
            System.out.println("2D Width(int32)" + getSource().readInt());
            System.out.println("2D Length(int32)" + getSource().readInt());
            // read the first header - 14 bytes
//
//            globalTable.put("Magic identifier", getSource().readString(3));
//
//            getSource().skipBytes(4);
//
//            meta.setGlobal(getSource().readInt());
//
//            // read the second header - 40 bytes
//
//            getSource().skipBytes(4);
//
//            int sizeX = 0, sizeY = 0;
//
//            // get the dimensions
//
//            sizeX = getSource().readInt();
//            sizeY = getSource().readInt();
//
//            iMeta.addAxis(Axes.X, sizeX);
//            iMeta.addAxis(Axes.Y, sizeY);
//
//            if (sizeX < 1) {
//                log().trace("Invalid width: " + sizeX + "; using the absolute value");
//                sizeX = Math.abs(sizeX);
//            }
//            if (sizeY < 1) {
//                log().trace("Invalid height: " + sizeY + "; using the absolute value");
//                sizeY = Math.abs(sizeY);
//                meta.setInvertY(true);
//            }
//
//            globalTable.put("Color planes", getSource().readShort());
//
//            final short bpp = getSource().readShort();
//
//            iMeta.setBitsPerPixel(bpp);
//
//            meta.setCompression(getSource().readInt());
//
//            getSource().skipBytes(4);
//            final int pixelSizeX = getSource().readInt();
//            final int pixelSizeY = getSource().readInt();
//            int nColors = getSource().readInt();
//            if (nColors == 0 && bpp != 32 && bpp != 24) {
//                nColors = bpp < 8 ? 1 << bpp : 256;
//            }
//            getSource().skipBytes(4);
//
//            // read the palette, if it exists
//
//            if (nColors != 0 && bpp == 8) {
//                final byte[][] palette = new byte[3][256];
//
//                for (int i = 0; i < nColors; i++) {
//                    for (int j = palette.length - 1; j >= 0; j--) {
//                        palette[j][i] = getSource().readByte();
//                    }
//                    getSource().skipBytes(1);
//                }
//
//                meta.palette = new ColorTable8(palette);
//            }
//            else if (nColors != 0) getSource().skipBytes(nColors * 4);
//
//            if (config.parserGetLevel() != MetadataLevel.MINIMUM) {
//                globalTable.put("Indexed color", meta.getColorTable(0, 0) != null);
//                globalTable.put("Image width", sizeX);
//                globalTable.put("Image height", sizeY);
//                globalTable.put("Bits per pixel", bpp);
//                String comp = "invalid";
//
//                switch (meta.getCompression()) {
//                    case RAW:
//                        comp = "None";
//                        break;
//                    case RLE_8:
//                        comp = "8 bit run length encoding";
//                        break;
//                    case RLE_4:
//                        comp = "4 bit run length encoding";
//                        break;
//                    case RGB_MASK:
//                        comp = "RGB bitmap with mask";
//                        break;
//                }
//
//                globalTable.put("Compression type", comp);
//                globalTable.put("X resolution", pixelSizeX);
//                globalTable.put("Y resolution", pixelSizeY);
//            }
        }
    }

    public static class Reader extends ByteArrayReader<Metadata> {

        // -- AbstractReader API Methods --

        @Override
        protected String[] createDomainArray() {
            return new String[] { FormatTools.GRAPHICS_DOMAIN };
        }

        // -- Reader API Methods --

        @Override
        public ByteArrayPlane openPlane(final int imageIndex, final long planeIndex,
                                        final ByteArrayPlane plane, final Interval bounds,
                                        final SCIFIOConfig config) throws FormatException, IOException
        {
            final Metadata meta = getMetadata();
            final ImageMetadata imageMeta = meta.get(imageIndex);
//            final int xIndex = imageMeta.getAxisIndex(Axes.X);
//            final int yIndex = imageMeta.getAxisIndex(Axes.Y);
//            final int x = (int) bounds.min(xIndex);
//            final int y = (int) bounds.min(yIndex);
//            final int w = (int) bounds.dimension(xIndex);
//            final int h = (int) bounds.dimension(yIndex);
//
//            final byte[] buf = plane.getData();
//            final int compression = meta.getCompression();
//            final int bpp = imageMeta.getBitsPerPixel();
//            final int sizeX = (int) imageMeta.getAxisLength(Axes.X);
//            final int sizeY = (int) imageMeta.getAxisLength(Axes.Y);
//            final int sizeC = (int) imageMeta.getAxisLength(Axes.CHANNEL);
//
//            FormatTools.checkPlaneForReading(meta, imageIndex, planeIndex, buf.length,
//                    bounds);
//
//            if (compression != RAW && getHandle().length() < FormatTools.getPlaneSize(
//                    this, imageIndex))
//            {
//                throw new UnsupportedCompressionException(compression +
//                        " not supported");
//            }
//
//            getHandle().seek(meta.getGlobal() + rowsToSkip * rowLength);
//
//            int pad = ((rowLength * bpp) / 8) % 2;
//            if (pad == 0) {
//                pad = ((rowLength * bpp) / 8) % 4;
//            }
//            else {
//                pad *= sizeC;
//            }
//            int planeSize = sizeX * sizeC * h;
//            if (bpp >= 8) planeSize *= (bpp / 8);
//            else planeSize /= (8 / bpp);
//            planeSize += pad * h;
//            if (planeSize + getHandle().offset() > getHandle().length()) {
//                planeSize -= (pad * h);
//
//                // sometimes we have RGB images with a single padding byte
//                if (planeSize + sizeY + getHandle().offset() <= getHandle().length()) {
//                    pad = 1;
//                    planeSize += h;
//                }
//                else {
//                    pad = 0;
//                }
//            }
//
            getHandle().skipBytes(286 + 255);
//
//            final byte[] rawPlane = new byte[planeSize];
//            getHandle().read(rawPlane);
//
//            final BitBuffer bb = new BitBuffer(rawPlane);
//
//            final ColorTable palette = meta.getColorTable(0, 0);
//            plane.setColorTable(palette);
//
//            final int effectiveC = palette != null && palette.getLength() > 0 ? 1
//                    : sizeC;
//            for (int row = h - 1; row >= 0; row--) {
//                final int rowIndex = meta.isInvertY() ? h - 1 - row : row;
//                bb.skipBits(x * bpp * effectiveC);
//                for (int i = 0; i < w * effectiveC; i++) {
//                    if (bpp <= 8) {
//                        buf[rowIndex * w * effectiveC + i] = (byte) (bb.getBits(bpp) &
//                                0xff);
//                    }
//                    else {
//                        for (int b = 0; b < bpp / 8; b++) {
//                            buf[(bpp / 8) * (rowIndex * w * effectiveC + i) + b] = (byte) (bb
//                                    .getBits(8) & 0xff);
//                        }
//                    }
//                }
//                if (row > 0) {
//                    bb.skipBits((sizeX - w - x) * bpp * effectiveC + pad * 8);
//                }
//            }
//
//            if (imageMeta.getAxisLength(Axes.CHANNEL) > 1) {
//                ImageTools.bgrToRgb(buf, imageMeta.getInterleavedAxisCount() > 0, 1,
//                        (int) imageMeta.getAxisLength(Axes.CHANNEL));
//            }
            return plane;
        }

    }
}
