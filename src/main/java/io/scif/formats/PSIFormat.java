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

    // -- Constants --
    public static final int numImages = 2;

    public static final int imageIndex2D = 0;
    public static final int imageIndex3D = 1;

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
            final ImageMetadata meta2D = get(0);

            meta2D.setPlanarAxisCount(2);
            meta2D.setLittleEndian(true);
            meta2D.setIndexed(false);
            meta2D.setFalseColor(false);
            meta2D.setMetadataComplete(true);

            final ImageMetadata meta3D = get(1);

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

        // -- Checker API Methods --

        @Override
        public boolean isFormat(final DataHandle<Location> stream)
                throws IOException
        {
            final int blockLen = 3;
            if (!FormatTools.validStream(stream, blockLen, false)) return false;
            return stream.readString(blockLen).equals(PSI_MAGIC_STRING);
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

            globalTable.put("Version", getSource().readString(5));
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

            meta.setPixelStorageOrder2D(getSource().readUnsignedByte());
            table2D.put("Pixel Storage Order", meta.getPixelStorageOrder2D());

            meta.setCodec2D(getSource().readUnsignedByte());
            table2D.put("Codec", meta.getCodec2D());

            table2D.put("Longitudinal Resolution", getSource().readFloat());
            table2D.put("Transverse Resolution", getSource().readFloat());

            meta.setWidth2D(getSource().readInt());
            table2D.put("Width", meta.getWidth2D());

            meta.setLength2D(getSource().readInt());
            table2D.put("Length", meta.getLength2D());

            int bitDepth2D = getSource().readUnsignedByte();
            table2D.put("Data Bit Depth", bitDepth2D);

            long dataSize2D = Integer.toUnsignedLong(getSource().readInt());
            table2D.put("Data Size", dataSize2D);

            table2D.put("Compression Quality", getSource().readFloat());

            // 3D Image Metadata

            meta.setPixelStorageOrder3D(getSource().readUnsignedByte());
            table3D.put("Pixel Storage Order", meta.getPixelStorageOrder3D());

            meta.setCodec3D(getSource().readUnsignedByte());
            table3D.put("Codec", meta.getCodec3D());

            table3D.put("Longitudinal Resolution", getSource().readFloat());
            table3D.put("Transverse Resolution", getSource().readFloat());
            table3D.put("Vertical Resolution", getSource().readFloat());

            meta.setWidth3D(getSource().readInt());
            table3D.put("Width", meta.getWidth3D());

            meta.setLength3D(getSource().readInt());
            table3D.put("Length", meta.getLength3D());

            int bitDepth3D = getSource().readUnsignedByte();
            table3D.put("Data Bit Depth", bitDepth3D);

            long dataSize3D = Integer.toUnsignedLong(getSource().readInt());
            table3D.put("Data Size", dataSize3D);

            table3D.put("Compression Quality", getSource().readFloat());
            table3D.put("Registration", getSource().readUnsignedByte());

            // Global Metadata

            globalTable.put("Reference Range Value", getSource().readFloat());
            globalTable.put("Metadata Data Size", Integer.toUnsignedLong(getSource().readInt()));
            globalTable.put("Speed", getSource().readFloat());
            globalTable.put("Time Stamp", getSource().readLong());
            globalTable.put("Vehicle Name", getSource().readString(33));
            globalTable.put("Operator Name", getSource().readString(33));
            globalTable.put("Contractor Name", getSource().readString(33));
            globalTable.put("Sensor System", getSource().readString(33));

            // Skip the Reserved Header Item
            getSource().skipBytes(256);

            meta.setOffset2D(getSource().offset());
            System.out.println("2D Image Data Offset: " + meta.getOffset2D());

            getSource().skip(dataSize2D);
            meta.setOffset3D(getSource().offset());
            System.out.println("3D Image Data Offset: " + meta.getOffset3D());

            if (dataSize2D == 0 && dataSize3D == 0) {
                throw new FormatException("No 2D or 3D Image Data Present");
            }

            if (dataSize2D > 0) {
                meta2D.setAxisLength(Axes.X, meta.getWidth2D());
                meta2D.setAxisLength(Axes.Y, meta.getLength2D());
            }

            if (dataSize3D > 0) {
                meta3D.setAxisLength(Axes.X, meta.getWidth3D());
                meta3D.setAxisLength(Axes.Y, meta.getLength3D());
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
