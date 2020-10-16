package io.scif.formats;

import io.scif.*;
import io.scif.config.SCIFIOConfig;
import org.scijava.io.handle.DataHandleService;
import org.scijava.io.location.FileLocation;
import org.scijava.plugin.Parameter;

import java.io.IOException;

public class Tester {
    public static void main(final String... agrs) throws FormatException,
            IOException
    {

        // The most convenient way to program against the SCIFIO API is by using
        // an io.scif.SCIFIO instance. This is basically a "SCIFIO lens" on an
        // org.scijava.Context instance. There are several ways to create a SCIFIO
        // instance, e.g. if you already have a Context available, but for now we'll
        // assume we want a fresh Context.
        SCIFIO scifio = new SCIFIO();

        // Let's look at a sample scenario where we have an image path and we just
        // want to open the first 3 planes of a dataset as simply as possible:

        // The path to our sample image. Fake images are special because they don't
        // exist on disk - they are defined purely by their file name -  so they're
        // good for testing.

        // This method checks the Context used by our SCIFIO instance for all its
        // known format plugins, and returns an io.scif.Reader capable of opening
        // the specified image's planes.
        final PSIFormat format = scifio.format().getFormatFromClass(PSIFormat.class);
        final PSIFormat.Checker checker = (io.scif.formats.PSIFormat.Checker) format.createChecker();
        final SCIFIOConfig config = new SCIFIOConfig();
        config.checkerSetOpen(true);
        boolean correctFormat = checker.isFormat(new FileLocation("C:\\Users\\jshaw\\Desktop\\scifio\\src\\main\\java\\io\\scif\\formats\\small_uncompressed_new.psi"), config);
        final Reader reader = scifio.initializer().initializeReader(new FileLocation("C:\\Users\\jshaw\\Desktop\\scifio\\src\\main\\java\\io\\scif\\formats\\small_uncompressed_new.psi"), config);
        final BMPFormat BMPFormat =
                scifio.format().getFormatFromClass(BMPFormat.class);
        final BMPFormat.Reader reader2 = (io.scif.formats.BMPFormat.Reader) BMPFormat.createReader();
        final Parser parser = (Parser) BMPFormat.createParser();
        final BMPFormat.Metadata meta = (BMPFormat.Metadata) parser.parse(new FileLocation("D:\\Github\\scifio\\src\\main\\java\\io\\scif\\formats\\test.bmp"));
        System.out.println("Color table: " + meta.getColorTable(0, 0));
        reader2.setMetadata(meta);

//        final Reader reader2 = scifio.initializer().initializeReader(new FileLocation("andrew_zhao_ps1 (2).jpg"));
        // ------------------------------------------------------------------------
        // COMPARISON WITH BIO-FORMATS 4.X
        // Bio-Formats 4.X used a single aggregated reader class:
        // loci.formats.ImageReader. This reader kept an instance of each other
        // known reader and delegated to the appropriate reader when working with a
        // given image format.
        // The SCIFIO context is similar to ImageReader, in that it keeps singleton
        // references to each Format type, and provides convenience methods for
        // creating appropriate components. But in SCIFIO, each image operation -
        // such as opening/saving a plane, parsing metadata, or checking image
        // format compatibility - is encapsulated in a single class. The context
        // is the entry point for gaining access to these components. Additionally,
        // each context allows separate loading of Formats, for differentiated
        // environments.
        // ------------------------------------------------------------------------

        // Here we open and "display" the actual planes.
        for (int i = 0; i < 3; i++) {
            // Pixels read from images in SCIFIO are returned as io.scif.Plane
            // objects, agnostic of the underlying data type (e.g. byte[] or
            // java.awt.BufferedImage)
//            Plane plane = reader.openPlane(0, i);
//            displayImage(i, plane);
//            plane = reader2.openPlane(0, i);
//            displayImage(i, plane);
        }

        // ------------------------------------------------------------------------
        // COMPARISON WITH BIO-FORMATS 4.X
        // In Bio-Formats 4.X, planes were opened via a reader.openBytes call which
        // typically had one less index parameter than in SCIFIO. This is because
        // Bio-Formats readers cached the current "series" for each reader.
        // In SCIFIO we have moved away from caching state whenever possible,
        // except on components designed to hold state (such as Metadata).
        // The data model used by SCIFIO follows the OME notation, that each path
        // points to a "Dataset," which contains 1 or more "Images" and each image
        // contains 1 or more "Planes" - typically planes are XY across some
        // number of dimensions.
        // In the openPlane call above, the Dataset is implicit (the "sampleImage")
        // the first index specifies the image number, and the second index
        // specifies the plane number - which would result in returning the
        // first 3 planes depending (whether these are Z, C, T, etc... slices
        // depends on the structure of the dataset).
        // ------------------------------------------------------------------------

        // All SciJava applications that create a Context should dispose it before
        // shutting down.
        // See http://imagej.net/Writing_plugins#The_Context
        scifio.getContext().dispose();
    }

    // Dummy method for demonstrating io.scif.Plane#getBytes()
    private static void displayImage(final int index, final Plane plane) {
        // All planes, regardless of type, can automatically convert their pixel data
        // to a byte[].
        System.out.println("plane " + index + ": " + plane + ", length: " +
                plane.getBytes().length);
    }
}

