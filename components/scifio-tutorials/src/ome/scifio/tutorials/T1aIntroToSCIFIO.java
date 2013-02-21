/*
 * #%L
 * Tutorials for SCIFIO API
 * %%
 * Copyright (C) 2013 Open Microscopy Environment:
 *   - Board of Regents of the University of Wisconsin-Madison
 *   - Glencoe Software, Inc.
 *   - University of Dundee
 * %%
 * To the extent possible under law, the SCIFIO developers have waived
 * all copyright and related or neighboring rights to this tutorial code.
 * 
 * See the CC0 1.0 Universal license for details:
 * http://creativecommons.org/publicdomain/zero/1.0/
 * #L%
 */
package ome.scifio.tutorials;

import java.io.IOException;
import java.util.List;

import org.scijava.Context;

import ome.scifio.Format;
import ome.scifio.FormatException;
import ome.scifio.Plane;
import ome.scifio.Reader;
import ome.scifio.SCIFIO;

/**
 * An introduction to the SCIFIO API. Demonstrates basic plane reading.
 * 
 * @author Mark Hiner
 *
 */
public class T1aIntroToSCIFIO {
  
  public static void main(final String... agrs) throws FormatException, IOException {
    // The first step when working with SCIFIO is to create a context.
    // This is an instance of ome.scifio.SCIFIO, created as follows:
    Context context = new Context();
    
    // This context provides access to all supported Format types, which
    // will allow corresponding images to be opened:
    List<Format> formats = context.getService(SCIFIO.class).formats().getAllFormats();
    
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
    
    // Let's look at a sample scenario where we have an image path and we just
    // want to open the first 3 planes as simply as possible:
    
    // The path to our sample image
    String sampleImage = "8bit-signed&pixelType=int8&sizeZ=3&sizeC=5&sizeT=7&sizeY=50.fake";
    
    // Planes read from images in SCIFIO are returned as ome.scifio.Plane
    // objects, agnostic of the underlying data type (e.g. byte[] or
    // java.awt.BufferedImage)
    Plane[] planes = new Plane[3];
    
    // This method tells the context to check all of its known formats and
    // return an ome.scifio.Reader capable of opening the specified image's
    // planes.
    Reader reader = context.getService(SCIFIO.class).initializer().initializeReader(sampleImage);
    
    // Here we open the actual planes and store them for future use
    for (int i=0; i<planes.length; i++) {
      planes[i] = reader.openPlane(0, i);
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
    // contains one or more "Planes" - typically planes are XY across some
    // arbitrary number of dimensions.
    // In the openPlane call above, the Dataset is implicit (the "sampleImage")
    // the first index specifies the image number, and the second index
    // specifies the plane number - which would result in returning the
    // first 3 C, Z or T planes depending on the ordering of the image.
    // ------------------------------------------------------------------------
    
    // Now that we have image planes, suppose we want to display them.
    // In Bio-Formats 4.X, planes were returned as byte[]'s. This data
    // structure is still available in SCIFIO:
    
    for (Plane p : planes)
      displayImage(p.getBytes());
  }
  
  // Dummy method for demonstrating ome.scifio.Plane#getBytes()
  private static void displayImage(byte[] bytes) {
    System.out.println(bytes + " " + bytes.length);
  }

}