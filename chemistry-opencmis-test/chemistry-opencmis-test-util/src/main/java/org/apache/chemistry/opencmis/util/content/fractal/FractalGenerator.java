////////////////////////////////////////////////////////////////////////////////
/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

/*
 * Original code inspired by work from David Lebernight 
 * see: http://www.gui.net/fractal.html
 * email as requested in original source has been sent,
 * to david@leberknight.com, but address is invalid (2012-02-07)
 */

package org.apache.chemistry.opencmis.util.content.fractal;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.ImageOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class FractalGenerator {
    private static final Logger LOG = LoggerFactory.getLogger(FractalGenerator.class);

    private final static int ZOOM_STEPS_PER_BATCH = 10;
    private final static int DEFAULT_MAX_ITERATIONS = 33;
    private final static ComplexRectangle INITIAL_RECT = new ComplexRectangle(-2.1, 1.1, -1.3, 1.3);
    private final static ComplexRectangle INITIAL_JULIA_RECT = new ComplexRectangle(-2.0, 2.0, -2.0, 2.0);
    private final static int INITIAL_ITERATIONS = 33;

    // Color:
    private Map<String, int[]> colorTable;
    private final String COLORS_BLACK_AND_WHITE = "black & white";
    private final String COLORS_BLUE_ICE = "blue ice";
    private final String COLORS_FUNKY = "funky";
    private final String COLORS_PASTEL = "pastel";
    private final String COLORS_PSYCHEDELIC = "psychedelic";
    private final String COLORS_PURPLE_HAZE = "purple haze";
    private final String COLORS_RADICAL = "radical";
    private final String COLORS_RAINBOW = "rainbow";
    private final String COLORS_RAINBOWS = "rainbows";
    private final String COLORS_SCINTILLATION = "scintillation";
    private final String COLORS_WARPED = "warped";
    private final String COLORS_WILD = "wild";
    private final String COLORS_ZEBRA = "zebra";
    private final String[] colorSchemes = {COLORS_BLACK_AND_WHITE, COLORS_BLUE_ICE, COLORS_FUNKY, COLORS_PASTEL,
        COLORS_PSYCHEDELIC, COLORS_PURPLE_HAZE, COLORS_RADICAL, COLORS_RAINBOW, COLORS_RAINBOWS,
        COLORS_SCINTILLATION, COLORS_WARPED, COLORS_WILD, COLORS_ZEBRA};
    private final int imageHeight = 512; // default
    private final int imageWidth = 512; // default
    private final int numColors = 512; // colors per colormap
    private FractalCalculator calculator;
    private int previousIterations = 1;
    private int maxIterations;
    String color;
    int counter = 0;
    int newRowTile, newColTile;
    int parts = 16;
    private int stepInBatch = 0;
    ComplexRectangle rect;
    ComplexPoint juliaPoint;
    
    public FractalGenerator() {
        reset();
    }
    
    private void reset() {
        rect = new ComplexRectangle(-1.6, -1.2, -0.1, 0.1);
        juliaPoint = null; // new ComplexPoint();
        maxIterations = DEFAULT_MAX_ITERATIONS;
       
        Random ran = new Random();
        color = colorSchemes[ran.nextInt(colorSchemes.length)];
        parts = ran.nextInt(13)+3;
        LOG.debug("Parts: " + parts);
        maxIterations = DEFAULT_MAX_ITERATIONS;
        LOG.debug("Original rect " + ": (" + rect.getRMin() + "r," + rect.getRMax() +
                "r, " + rect.getIMin() + "i, " + rect.getIMax() + "i)");
        randomizeRect(rect);
    }
    
    public ByteArrayOutputStream generateFractal() throws IOException {
        ByteArrayOutputStream bos = null;
        
        if (stepInBatch == ZOOM_STEPS_PER_BATCH) {
            stepInBatch = 0;
            reset();
        }

        ++stepInBatch;
        LOG.debug("Generating rect no " + stepInBatch + ": (" + rect.getRMin() + "r," + 
                rect.getRMax() +  "r, " + rect.getIMin() + "i, " + rect.getIMax() + "i)");
        LOG.debug("   width: " + rect.getWidth() + " height: " + rect.getHeight());
        bos = genFractal(rect, juliaPoint);

        double r1New = rect.getWidth() * newColTile / parts +  rect.getRMin();
        double r2New = rect.getWidth() * (newColTile+1) / parts +  rect.getRMin();
        double i1New =  rect.getIMax() - (rect.getHeight() * newRowTile / parts);
        double i2New =  rect.getIMax() - (rect.getHeight() * (newRowTile+1) / parts);
        rect.set(r1New, r2New, i1New, i2New);
        randomizeRect(rect);
        LOG.debug("Done generating fractals.");
        
        return bos;
    }

    private void randomizeRect( ComplexRectangle rect) {
        double jitterFactor = 0.15; // +/- 15%
        double ran = Math.random() * jitterFactor +  (1.0 - jitterFactor);
        double width = rect.getWidth() * ran;
        ran = Math.random() * jitterFactor +  (1.0 - jitterFactor);
        double height = rect.getHeight() * ran;
        ran = Math.random() * jitterFactor +  (1.0 - jitterFactor);
        double r1 = (rect.getWidth() - width) * ran + rect.getRMin();
        ran = Math.random() * jitterFactor +  (1.0 - jitterFactor);
        double i1 = (rect.getHeight() - height) * ran + rect.getIMin();
        rect.set(r1, r1+width, i1, i1+height);
    }

    /**
     * Create a fractal image as JPEG in memory and return it  
     * @param rect
     *      rectangle of mandelbrot or julia set
     * @param juliaPoint
     *      point in Julia set or null
     * @return
     *      byte array with JPEG stream
     * @throws IOException 
     */
    public ByteArrayOutputStream genFractal(ComplexRectangle rect, ComplexPoint juliaPoint) throws IOException {

        boolean isJulia = null != juliaPoint;
        expandRectToFitImage(rect);
        initializeColors();

        maxIterations = maybeGuessMaxIterations(maxIterations, rect, isJulia);
        LOG.debug("using " + maxIterations + " iterations.");
        detectDeepZoom(rect);

        calculator = new FractalCalculator(rect, maxIterations, imageWidth, imageHeight, getCurrentColorMap(),
                juliaPoint);
        int[][] iterations = calculator.calcFractal();
        BufferedImage image = calculator.mapItersToColors(iterations);
        findNewRect(image, iterations);

        // fast method to write to a file with default options
        // ImageIO.write((BufferedImage)(image), "jpg", new File("fractal-" + counter++ + ".jpg"));

        // create image in memory
        ByteArrayOutputStream bos = new ByteArrayOutputStream(200*1024);
        ImageOutputStream ios = ImageIO.createImageOutputStream(bos);
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName( "jpg" );
        ImageWriter imageWriter = writers.next();

        JPEGImageWriteParam params = new JPEGImageWriteParam( Locale.getDefault() );
        params.setCompressionMode( ImageWriteParam.MODE_EXPLICIT );
        params.setCompressionQuality( 0.9f );

        imageWriter.setOutput( ios );
        imageWriter.write( null, new IIOImage( image, null, null ), params );
        ios.close();

        // write memory block to a file
        // String fileName = String.format(pattern, counter++);
        // FileOutputStream outputStream = new FileOutputStream (fileName);
        // bos.writeTo(outputStream);
        // bos.close();
        // outputStream.close();

        return bos;
    }

    protected int[] getCurrentColorMap() {
        return colorTable.get(getColor());
    }

    protected String getColor() {
        return color;
    }

    protected void expandRectToFitImage(ComplexRectangle complexRect) {
        // The complex rectangle must be scaled to fit the pixel image view.
        // Method: compare the width/height ratios of the two rectangles.
        double imageWHRatio = 1.0;
        double complexWHRatio = 1.0;
        double iMin = complexRect.getIMin();
        double iMax = complexRect.getIMax();
        double rMin = complexRect.getRMin();
        double rMax = complexRect.getRMax();
        double complexWidth = rMax - rMin;
        double complexHeight = iMax - iMin;

        if ((imageWidth != 0) && (imageHeight != 0)) {
            imageWHRatio = ((double) imageWidth / (double) imageHeight);
        } else
            return;

        if ((complexWidth != 0) && (complexHeight != 0)) {
            complexWHRatio = complexWidth / complexHeight;
        } else
            return;

        if (imageWHRatio == complexWHRatio)
            return;

        if (imageWHRatio < complexWHRatio) {
            // Expand vertically
            double newHeight = complexWidth / imageWHRatio;
            double heightDifference = Math.abs(newHeight - complexHeight);
            iMin = iMin - heightDifference / 2;
            iMax = iMax + heightDifference / 2;
        } else {
            // Expand horizontally
            double newWidth = complexHeight * imageWHRatio;
            double widthDifference = Math.abs(newWidth - complexWidth);
            rMin = rMin - widthDifference / 2;
            rMax = rMax + widthDifference / 2;
        }
        complexRect.set(rMin, rMax, iMin, iMax);
    }

    private int guessNewMaxIterations(ComplexRectangle cr, boolean isJulia) {
        // The higher the zoom factor, the more iterations that are needed to
        // see
        // the detail. Guess at a number to produce a cool looking fractal:
        double zoom = INITIAL_RECT.getWidth() / cr.getWidth();
        if (zoom < 1.0) {
            zoom = 1.0; // forces logZoom >= 0
        }
        double logZoom = Math.log(zoom);
        double magnitude = (logZoom / 2.3) - 2.0; // just a guess.
        if (magnitude < 1.0) {
            magnitude = 1.0;
        }
        double iterations = INITIAL_ITERATIONS * (magnitude * logZoom + 1.0);
        if (isJulia)
            iterations *= 2.0; // Julia sets tend to need more iterations.
        return (int) iterations;
    }

    private int maybeGuessMaxIterations(int maxIterations, ComplexRectangle cr, boolean isJulia) {
        // If the user did not change the number of iterations, make a guess...
        if (previousIterations == maxIterations) {
            maxIterations = guessNewMaxIterations(cr, isJulia);
        }
        previousIterations = maxIterations;
        return maxIterations;
    }

    private boolean detectDeepZoom(ComplexRectangle cr) {
        // "Deep Zoom" occurs when the precision provided by the Java type
        // double
        // runs out of resolution. The use of BigDecimal is required to fix
        // this.
        double deltaDiv2 = cr.getWidth() / ((imageWidth) * 2.0);
        String min = "" + (cr.getRMin());
        String minPlus = "" + (cr.getRMin() + deltaDiv2);

        if (Double.valueOf(min).doubleValue() == Double.valueOf(minPlus).doubleValue()) {
            LOG.warn("Deep Zoom...  Drawing resolution will be degraded ;-(");
            return true;
        }
        return false;
    }

    private void initializeColors() {
        colorTable = new HashMap<String, int[]>();

        int red = 255;
        int green = 255;
        int blue = 255;

        float hue = (float) 1.0;
        float saturation = (float) 1.0;
        float brightness = (float) 1.0;

        // COLORS_BLACK_AND_WHITE:
        int[] colorMap = new int[numColors];
        for (int colorNum = numColors - 1; colorNum >= 0; colorNum--) {
            colorMap[colorNum] = Color.white.getRGB();
        }
        colorTable.put(COLORS_BLACK_AND_WHITE, colorMap);

        // COLORS_BLUE_ICE:
        blue = 255;
        colorMap = new int[numColors];
        for (int colorNum = numColors - 1; colorNum >= 0; colorNum--) {
            red = (int) ((255 * (float) colorNum / numColors)) % 255;
            green = (int) ((255 * (float) colorNum / numColors)) % 255;
            colorMap[colorNum] = new Color(red, green, blue).getRGB();
        }
        colorTable.put(COLORS_BLUE_ICE, colorMap);

        // COLORS_FUNKY:
        colorMap = new int[numColors];
        for (int colorNum = numColors - 1; colorNum >= 0; colorNum--) {
            red = (int) ((1024 * (float) colorNum / numColors)) % 255;
            green = (int) ((512 * (float) colorNum / numColors)) % 255;
            blue = (int) ((256 * (float) colorNum / numColors)) % 255;
            colorMap[numColors - colorNum - 1] = new Color(red, green, blue).getRGB();
        }
        colorTable.put(COLORS_FUNKY, colorMap);

        // COLORS_PASTEL
        brightness = (float) 1.0;
        colorMap = new int[numColors];
        for (int colorNum = 0; colorNum < numColors; colorNum++) {
            hue = ((float) (colorNum * 4) / (float) numColors) % numColors;
            saturation = ((float) (colorNum * 2) / (float) numColors) % numColors;
            colorMap[colorNum] = Color.HSBtoRGB(hue, saturation, brightness);
        }
        colorTable.put(COLORS_PASTEL, colorMap);

        // COLORS_PSYCHEDELIC:
        saturation = (float) 1.0;
        colorMap = new int[numColors];
        for (int colorNum = 0; colorNum < numColors; colorNum++) {
            hue = ((float) (colorNum * 5) / (float) numColors) % numColors;
            brightness = ((float) (colorNum * 20) / (float) numColors) % numColors;
            colorMap[colorNum] = Color.HSBtoRGB(hue, saturation, brightness);
        }
        colorTable.put(COLORS_PSYCHEDELIC, colorMap);

        // COLORS_PURPLE_HAZE:
        red = 255;
        blue = 255;
        colorMap = new int[numColors];
        for (int colorNum = numColors - 1; colorNum >= 0; colorNum--) {
            green = (int) ((255 * (float) colorNum / numColors)) % 255;
            colorMap[numColors - colorNum - 1] = new Color(red, green, blue).getRGB();
        }
        colorTable.put(COLORS_PURPLE_HAZE, colorMap);

        // COLORS_RADICAL:
        saturation = (float) 1.0;
        colorMap = new int[numColors];
        for (int colorNum = 0; colorNum < numColors; colorNum++) {
            hue = ((float) (colorNum * 7) / (float) numColors) % numColors;
            brightness = ((float) (colorNum * 49) / (float) numColors) % numColors;
            colorMap[colorNum] = Color.HSBtoRGB(hue, saturation, brightness);
        }
        colorTable.put(COLORS_RADICAL, colorMap);

        // COLORS_RAINBOW:
        saturation = (float) 1.0;
        brightness = (float) 1.0;
        colorMap = new int[numColors];
        for (int colorNum = 0; colorNum < numColors; colorNum++) {
            hue = (float) colorNum / (float) numColors;
            colorMap[colorNum] = Color.HSBtoRGB(hue, saturation, brightness);
        }
        colorTable.put(COLORS_RAINBOW, colorMap);

        // COLORS_RAINBOWS:
        saturation = (float) 1.0;
        brightness = (float) 1.0;
        colorMap = new int[numColors];
        for (int colorNum = 0; colorNum < numColors; colorNum++) {
            hue = ((float) (colorNum * 5) / (float) numColors) % numColors;
            colorMap[colorNum] = Color.HSBtoRGB(hue, saturation, brightness);
        }
        colorTable.put(COLORS_RAINBOWS, colorMap);

        // COLORS_SCINTILLATION
        brightness = (float) 1.0;
        saturation = (float) 1.0;
        colorMap = new int[numColors];
        for (int colorNum = 0; colorNum < numColors; colorNum++) {
            hue = ((float) (colorNum * 2) / (float) numColors) % numColors;
            brightness = ((float) (colorNum * 5) / (float) numColors) % numColors;
            colorMap[colorNum] = Color.HSBtoRGB(hue, saturation, brightness);
        }
        colorTable.put(COLORS_SCINTILLATION, colorMap);

        // COLORS_WARPED:
        colorMap = new int[numColors];
        for (int colorNum = numColors - 1; colorNum >= 0; colorNum--) {
            red = (int) ((1024 * (float) colorNum / numColors)) % 255;
            green = (int) ((256 * (float) colorNum / numColors)) % 255;
            blue = (int) ((512 * (float) colorNum / numColors)) % 255;
            colorMap[numColors - colorNum - 1] = new Color(red, green, blue).getRGB();
        }
        colorTable.put(COLORS_WARPED, colorMap);

        // COLORS_WILD:
        colorMap = new int[numColors];
        for (int colorNum = 0; colorNum < numColors; colorNum++) {
            hue = ((float) (colorNum * 1) / (float) numColors) % numColors;
            saturation = ((float) (colorNum * 2) / (float) numColors) % numColors;
            brightness = ((float) (colorNum * 4) / (float) numColors) % numColors;
            colorMap[colorNum] = Color.HSBtoRGB(hue, saturation, brightness);
        }
        colorTable.put(COLORS_WILD, colorMap);

        // COLORS_ZEBRA:
        colorMap = new int[numColors];
        for (int colorNum = 0; colorNum < numColors; colorNum++) {
            if (colorNum % 2 == 0) {
                colorMap[colorNum] = Color.white.getRGB();
                ;
            } else {
                colorMap[colorNum] = Color.black.getRGB();
                ;
            }
        }
        colorTable.put(COLORS_ZEBRA, colorMap);
    }


    private void findNewRect(BufferedImage image, int[][] iterations) {

        int newWidth = image.getWidth() / parts;
        int newHeight = image.getHeight() / parts;
        int i=0, j=0;
        int noTiles = (image.getWidth() / newWidth) * (image.getHeight() / newHeight); // equals parts but be aware of rounding errors!;
        double[] stdDev = new double [noTiles];

        for (int y = 0; y+newHeight <= image.getHeight(); y+=newHeight) {
            for (int x = 0; x+newWidth <= image.getWidth(); x+=newWidth) {
                Rectangle subRect = new Rectangle(x, y, newWidth, newHeight);
                stdDev[i*parts+j] = calcStdDev(iterations, subRect);
                ++j;
            }
            ++i;
            j=0;
        }

        // find tile with greatest std deviation:
        double max = 0;
        int index = 0;
        for (i=0; i<noTiles; i++) {
            if (stdDev[i] > max) {
                index = i;
                max = stdDev[i];
            }
        }
        newRowTile = index / parts;
        newColTile = index % parts;
    }

    private double calcStdDev(int[][] iterations, Rectangle rect) {

        int sum=0;
        long sumSquare=0;

        for (int x = rect.x; x < rect.x+rect.width; x+=1) {
            for (int y = rect.y; y < rect.y+rect.height; y+=1) {
                int iters = iterations[x][y];
                sum +=iters;
                sumSquare +=iters*iters;
            }
        }
        int count = rect.width * rect.height;
        double mean = 0.0;

        mean = sum / count;
        return Math.sqrt(sumSquare/count - (mean * mean));
    }

}
