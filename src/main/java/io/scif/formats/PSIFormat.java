package io.scif.formats;

import io.scif.*;
import io.scif.config.SCIFIOConfig;
import io.scif.util.FormatTools;

import java.io.IOException;
import java.util.StringTokenizer;

import net.imagej.axis.Axes;
import net.imglib2.Interval;

import org.scijava.io.handle.DataHandle;
import org.scijava.io.handle.DataHandle.ByteOrder;
import org.scijava.io.handle.DataHandleService;
import org.scijava.io.location.BytesLocation;
import org.scijava.io.location.Location;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 * Handler plugin for the PSI file format.
 */
@Plugin(type = Format.class, name = "Pavement Surface Image")
public class PSIFormat extends AbstractFormat {

    public enum DATA_BIT_DEPTH_2D {
        DEPTH8(8);

        private final int value;

        DATA_BIT_DEPTH_2D(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static boolean inEnum(int checkValue) {
            DATA_BIT_DEPTH_2D[] enumValues = DATA_BIT_DEPTH_2D.values();
            for (DATA_BIT_DEPTH_2D enumValue : enumValues) {
                if (enumValue.getValue() == checkValue) {
                    return true;
                }
            }
            return false;
        }
    }

    public enum DATA_BIT_DEPTH_3D {
        DEPTH16(16);

        private final int value;

        DATA_BIT_DEPTH_3D(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static boolean inEnum(int checkValue) {
            DATA_BIT_DEPTH_3D[] enumValues = DATA_BIT_DEPTH_3D.values();
            for (DATA_BIT_DEPTH_3D enumValue : enumValues) {
                if (enumValue.getValue() == checkValue) {
                    return true;
                }
            }
            return false;
        }
    }

    public enum CODEC_2D {
        BIN_UNCOMPRESSED(0), BIN_ZIP(1), JPEG(2), PNG(3);

        private final int value;

        CODEC_2D(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static boolean inEnum(int checkValue) {
            CODEC_2D[] enumValues = CODEC_2D.values();
            for (CODEC_2D enumValue : enumValues) {
                if (enumValue.getValue() == checkValue) {
                    return true;
                }
            }
            return false;
        }
    }

    public enum CODEC_3D {
        BIN_UNCOMPRESSED(0), BIN_ZIP(1), OPENCRG_UNCOMPRESSED(2), OPENCRG_ZIP(3), JPEG2000(4);

        private final int value;

        CODEC_3D(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static boolean inEnum(int checkValue) {
            CODEC_3D[] enumValues = CODEC_3D.values();
            for (CODEC_3D enumValue : enumValues) {
                if (enumValue.getValue() == checkValue) {
                    return true;
                }
            }
            return false;
        }
    }

    public enum REGISTRATION_3D {
        REGISTERED(0), UNREGISTERED(1);

        private final int value;

        REGISTRATION_3D(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static boolean inEnum(int checkValue) {
            REGISTRATION_3D[] enumValues = REGISTRATION_3D.values();
            for (REGISTRATION_3D enumValue : enumValues) {
                if (enumValue.getValue() == checkValue) {
                    return true;
                }
            }
            return false;
        }
    }

    public enum PIXEL_STORAGE_2D {
        ROW(0), COLUMN(1);

        private final int value;

        PIXEL_STORAGE_2D(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static boolean inEnum(int checkValue) {
            PIXEL_STORAGE_2D[] enumValues = PIXEL_STORAGE_2D.values();
            for (PIXEL_STORAGE_2D enumValue : enumValues) {
                if (enumValue.getValue() == checkValue) {
                    return true;
                }
            }
            return false;
        }
    }

    public enum PIXEL_STORAGE_3D {
        ROW(0), COLUMN(1);

        private final int value;

        PIXEL_STORAGE_3D(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static boolean inEnum(int checkValue) {
            PIXEL_STORAGE_3D[] enumValues = PIXEL_STORAGE_3D.values();
            for (PIXEL_STORAGE_3D enumValue : enumValues) {
                if (enumValue.getValue() == checkValue) {
                    return true;
                }
            }
            return false;
        }
    }

    // -- Constants --
    public static final int numImages = 2;

    public static final int imageIndex2D = 0;
    public static final int imageIndex3D = 1;

    public static final int headerSize = 542;

    public static final int SIGNATURE_LEN = 3;
    public static final int TRAILER_LEN = 4;

    // -- AbstractFormat Methods --

    @Override
    protected String[] makeSuffixArray() {
        return new String[] { "psi" };
    }

    // -- Nested classes --

    public static class Metadata extends AbstractMetadata {

        // -- Fields --

        private int pixelStorageOrder2D;
        private int codec2D;
        private int width2D;
        private int length2D;

        private int pixelStorageOrder3D;
        private int codec3D;
        private int width3D;
        private int length3D;

        /** Offset to 2D pixel data. */
        private long offset2D;

        /** Offset to 3D pixel data */
        private long offset3D;

        // -- PSIMetadata getters and setters --

        public int getPixelStorageOrder2D() {
            return pixelStorageOrder2D;
        }

        public void setPixelStorageOrder2D(int pixelStorageOrder2D) {
            this.pixelStorageOrder2D = pixelStorageOrder2D;
        }

        public int getCodec2D() {
            return codec2D;
        }

        public void setCodec2D(int codec2D) {
            this.codec2D = codec2D;
        }

        public int getWidth2D() {
            return width2D;
        }

        public void setWidth2D(int width2D) {
            this.width2D = width2D;
        }

        public int getLength2D() {
            return length2D;
        }

        public void setLength2D(int length2D) {
            this.length2D = length2D;
        }

        public int getPixelStorageOrder3D() {
            return pixelStorageOrder3D;
        }

        public void setPixelStorageOrder3D(int pixelStorageOrder3D) {
            this.pixelStorageOrder3D = pixelStorageOrder3D;
        }

        public int getCodec3D() {
            return codec3D;
        }

        public void setCodec3D(int codec3D) {
            this.codec3D = codec3D;
        }

        public int getWidth3D() {
            return width3D;
        }

        public void setWidth3D(int width3D) {
            this.width3D = width3D;
        }

        public int getLength3D() {
            return length3D;
        }

        public void setLength3D(int length3D) {
            this.length3D = length3D;
        }

        public long getOffset2D() {
            return offset2D;
        }

        public void setOffset2D(final long offset2D) {
            this.offset2D = offset2D;
        }

        public long getOffset3D() {
            return offset3D;
        }

        public void setOffset3D(long offset3D) {
            this.offset3D = offset3D;
        }

        // -- Metadata API Methods --

        @Override
        public void populateImageMetadata() {
            final ImageMetadata meta2D = get(PSIFormat.imageIndex2D);

            meta2D.setPlanarAxisCount(2);
            meta2D.setLittleEndian(true);
            meta2D.setIndexed(false);
            meta2D.setFalseColor(false);
            meta2D.setMetadataComplete(true);

            final ImageMetadata meta3D = get(PSIFormat.imageIndex3D);

            meta3D.setPlanarAxisCount(2);
            meta3D.setLittleEndian(true);
            meta3D.setIndexed(false);
            meta3D.setFalseColor(false);
            meta3D.setMetadataComplete(true);
        }

        @Override
        public void close(final boolean fileOnly) throws IOException {
            super.close(fileOnly);
            if (!fileOnly) {
                setOffset2D(0);
                setOffset3D(0);
            }
        }
    }

    public static class Checker extends AbstractChecker {

        // -- Constants --

        public static final String PSI_MAGIC_STRING = "psi";
        public static final String PSI_TRAILER_STRING = "@@@@";

        // -- Checker API Methods --

        @Override
        public boolean suffixSufficient() {
            return false;
        }

        @Override
        public boolean isFormat(final DataHandle<Location> stream)
                throws IOException
        {
            if (!FormatTools.validStream(stream, SIGNATURE_LEN, false)) return false;
            boolean correctSignature = stream.readString(SIGNATURE_LEN).startsWith(PSI_MAGIC_STRING);

            if (stream.length() - TRAILER_LEN < 0) return false;
            stream.seek(stream.length() - TRAILER_LEN);
            boolean correctTrailer = stream.readString(TRAILER_LEN).startsWith(PSI_TRAILER_STRING);

            return correctSignature && correctTrailer;
        }

    }

    public static class Parser extends AbstractParser<Metadata> {

        // -- Parser API Methods --

        @Override
        protected void typedParse(final DataHandle<Location> stream,
                                  final Metadata meta, final SCIFIOConfig config) throws IOException,
                FormatException
        {
            meta.createImageMetadata(PSIFormat.numImages);

            final ImageMetadata meta2D = meta.get(PSIFormat.imageIndex2D);
            final MetaTable table2D = meta2D.getTable();

            final ImageMetadata meta3D = meta.get(PSIFormat.imageIndex3D);
            final MetaTable table3D = meta3D.getTable();

            final MetaTable globalTable = meta.getTable();

            stream.setOrder(ByteOrder.LITTLE_ENDIAN);

            // Global Metadata

            globalTable.put("Signature", getSource().readString(SIGNATURE_LEN));

            String version = getSource().readString(5);
            if (!(Character.isDigit(version.charAt(0))
                    && version.charAt(1) == '.'
                    && Character.isDigit(version.charAt(2))
                    && Character.isDigit(version.charAt(3)))) {
                throw new FormatException("[Metadata] Version Format Is Incorrect\n");
            }
            globalTable.put("Version", version);
            globalTable.put("Software Version", getSource().readString(9));
            globalTable.put("State Name", getSource().readString(3));
            globalTable.put("Route Name", getSource().readString(13));
            globalTable.put("Heading in Degrees", getSource().readFloat());
            globalTable.put("Lane Index", getSource().readUnsignedByte());
            globalTable.put("File Serial Number", Integer.toUnsignedLong(getSource().readInt()));
            globalTable.put("GPS Longitude", getSource().readDouble());
            globalTable.put("GPS Latitude", getSource().readDouble());
            globalTable.put("DMI Reading", getSource().readFloat());
            globalTable.put("Date", getSource().readString(9));
            globalTable.put("Time", getSource().readString(7));

            // 2D Image Metadata

            int pixelStorageOrder2D = getSource().readUnsignedByte();
            if (!PIXEL_STORAGE_2D.inEnum(pixelStorageOrder2D)) {
                throw new FormatException("[Metadata] Unrecognized 2D Pixel Storage Order Value\n");
            }
            meta.setPixelStorageOrder2D(pixelStorageOrder2D);
            table2D.put("Pixel Storage Order", pixelStorageOrder2D);

            int codec2D = getSource().readUnsignedByte();
            if (!CODEC_2D.inEnum(codec2D)) {
                throw new FormatException("[Metadata] Unrecognized 2D Codec Value\n");
            }
            meta.setCodec2D(codec2D);
            table2D.put("Codec", codec2D);

            float longitudinalResolution2D = getSource().readFloat();
            float transverseResolution2D = getSource().readFloat();
            table2D.put("Longitudinal Resolution", longitudinalResolution2D);
            table2D.put("Transverse Resolution", transverseResolution2D);

            int width2D = getSource().readInt();
            meta.setWidth2D(width2D);
            table2D.put("Width", width2D);

            int length2D = getSource().readInt();
            meta.setLength2D(length2D);
            table2D.put("Length", length2D);

            int bitDepth2D = getSource().readUnsignedByte();
            if (!DATA_BIT_DEPTH_2D.inEnum(bitDepth2D)) {
                throw new FormatException("[Metadata] Unrecognized 2D Bit Depth Value\n");
            }
            table2D.put("Data Bit Depth", bitDepth2D);

            long dataSize2D = Integer.toUnsignedLong(getSource().readInt());
            if (dataSize2D > 0) {
                if (width2D <= 0) {
                    throw new FormatException("[Metadata] 2D Width Must Be A Positive Value\n");
                }
                if (length2D <= 0) {
                    throw new FormatException("[Metadata] 2D Length Must Be A Positive Value\n");
                }
                if (longitudinalResolution2D <= 0) {
                    throw new FormatException("[Metadata] 2D Longitudinal Resolution Must Be A Positive Value\n");
                }
                if (transverseResolution2D <= 0) {
                    throw new FormatException("[Metadata] 2D Transverse Resolution Must Be A Positive Value\n");
                }

                if (codec2D == CODEC_2D.BIN_UNCOMPRESSED.getValue()) {
                    if (!(dataSize2D == (bitDepth2D / 8) * width2D * length2D)) {
                        throw new FormatException("[Metadata] 2D Data Size Does Not Match 2D Width, 2D Length, and 2D Bit Depth\n");
                    }
                }
            }
            table2D.put("Data Size", dataSize2D);

            table2D.put("Compression Quality", getSource().readFloat());

            // 3D Image Metadata

            int pixelStorageOrder3D = getSource().readUnsignedByte();
            if (!PIXEL_STORAGE_3D.inEnum(pixelStorageOrder3D)) {
                throw new FormatException("[Metadata] Unrecognized 3D Pixel Storage Order Value\n");
            }
            meta.setPixelStorageOrder3D(pixelStorageOrder3D);
            table3D.put("Pixel Storage Order", pixelStorageOrder3D);

            int codec3D = getSource().readUnsignedByte();
            if (!CODEC_3D.inEnum(codec3D)) {
                throw new FormatException("[Metadata] Unrecognized 3D Codec Value\n");
            }
            meta.setCodec3D(codec3D);
            table3D.put("Codec", codec3D);

            float longitudinalResolution3D = getSource().readFloat();
            float transverseResolution3D = getSource().readFloat();
            float verticalResolution3D = getSource().readFloat();
            table3D.put("Longitudinal Resolution", longitudinalResolution3D);
            table3D.put("Transverse Resolution", transverseResolution3D);
            table3D.put("Vertical Resolution", verticalResolution3D);

            int width3D = getSource().readInt();
            meta.setWidth3D(width3D);
            table3D.put("Width", width3D);

            int length3D = getSource().readInt();
            meta.setLength3D(length3D);
            table3D.put("Length", length3D);

            int bitDepth3D = getSource().readUnsignedByte();
            if (!DATA_BIT_DEPTH_3D.inEnum(bitDepth3D)) {
                throw new FormatException("[Metadata] Unrecognized 3D Bit Depth Value\n");
            }
            table3D.put("Data Bit Depth", bitDepth3D);

            long dataSize3D = Integer.toUnsignedLong(getSource().readInt());
            if (dataSize3D > 0) {
                if (width3D <= 0) {
                    throw new FormatException("[Metadata] 3D Width Must Be A Positive Value\n");
                }
                if (length3D <= 0) {
                    throw new FormatException("[Metadata] 3D Length Must Be A Positive Value\n");
                }
                if (longitudinalResolution3D <= 0) {
                    throw new FormatException("[Metadata] 3D Longitudinal Resolution Must Be A Positive Value\n");
                }
                if (transverseResolution3D <= 0) {
                    throw new FormatException("[Metadata] 3D Transverse Resolution Must Be A Positive Value\n");
                }

                if (codec3D == CODEC_3D.BIN_UNCOMPRESSED.getValue()) {
                    if (!(dataSize3D == (bitDepth3D / 8) * width3D * length3D)) {
                        throw new FormatException("[Metadata] 3D Data Size Does Not Match 3D Width, 3D Length, and 3D Bit Depth\n");
                    }
                }
            }
            table3D.put("Data Size", dataSize3D);

            table3D.put("Compression Quality", getSource().readFloat());

            int registration3D = getSource().readUnsignedByte();
            if (!REGISTRATION_3D.inEnum(registration3D)) {
                throw new FormatException("[Metadata] Unrecognized 3D Registration Value\n");
            }
            table3D.put("Registration", registration3D);

            // Global Metadata

            globalTable.put("Reference Range Value", getSource().readFloat());
            long metadataSize = Integer.toUnsignedLong(getSource().readInt());
            globalTable.put("Metadata Data Size", metadataSize);
            globalTable.put("Speed", getSource().readFloat());
            globalTable.put("Time Stamp", getSource().readLong());
            globalTable.put("Vehicle Name", getSource().readString(33));
            globalTable.put("Operator Name", getSource().readString(33));
            globalTable.put("Contractor Name", getSource().readString(33));
            globalTable.put("Sensor System", getSource().readString(33));

            // Skip the Reserved Header Item
            getSource().skipBytes(256);

            meta.setOffset2D(getSource().offset());
            // System.out.println("2D Image Data Offset: " + meta.getOffset2D());

            getSource().skip(dataSize2D);
            meta.setOffset3D(getSource().offset());
            // System.out.println("3D Image Data Offset: " + meta.getOffset3D());

            if (!(getSource().length() == SIGNATURE_LEN + headerSize + dataSize2D + dataSize3D + metadataSize + TRAILER_LEN)) {
                throw new FormatException("[Metadata] File Size Does Not Match Expected Value\n");
            }

            if (dataSize2D == 0 && dataSize3D == 0) {
                throw new FormatException("[Metadata] Both 2D And 3D Data Size Must Not Be 0\n");
            }

            if (dataSize2D > 0) {
                meta2D.setAxisLength(Axes.X, meta.getWidth2D());
                meta2D.setAxisLength(Axes.Y, meta.getLength2D());
                if (bitDepth2D == 8) {
                    meta2D.setPixelType(FormatTools.UINT8);
                }
            }

            if (dataSize3D > 0) {
                meta3D.setAxisLength(Axes.X, meta.getWidth3D());
                meta3D.setAxisLength(Axes.Y, meta.getLength3D());
                if (bitDepth3D == 16) {
                    meta3D.setPixelType(FormatTools.UINT16);
                }
            }

//            System.out.println("2D Width: " + table2D.get("Width"));
//            System.out.println("2D Length: " + table2D.get("Length"));
//            System.out.println("2D Data Bit Depth: " + table2D.get("Data Bit Depth"));
//            System.out.println("2D Data Size: " + table2D.get("Data Size"));
        }

    }

    public static class Reader extends ByteArrayReader<PSIFormat.Metadata> {

        @Parameter
        private DataHandleService dataHandleService;

        // -- AbstractReader API Methods --

        @Override
        protected String[] createDomainArray() {
            return new String[] { FormatTools.GRAPHICS_DOMAIN };
        }

        // -- Reader API methods --

        @Override
        public ByteArrayPlane openPlane(final int imageIndex, final long planeIndex,
                                        final ByteArrayPlane plane, final Interval bounds,
                                        final SCIFIOConfig config) throws FormatException, IOException
        {
            final byte[] buf = plane.getData();
            final Metadata meta = getMetadata();
            FormatTools.checkPlaneForReading(meta, imageIndex, planeIndex, buf.length,
                    bounds);

            if (imageIndex == PSIFormat.imageIndex2D) {
                getHandle().seek(meta.getOffset2D());
                if (meta.getCodec2D() == 0) {
                    readPlane(getHandle(), imageIndex, bounds, plane);
                }
            }
            if (imageIndex == PSIFormat.imageIndex3D) {
                getHandle().seek(meta.getOffset3D());
                if (meta.getCodec3D() == 0) {
                    readPlane(getHandle(), imageIndex, bounds, plane);
                }
            }
            return plane;

        }
    }
}
