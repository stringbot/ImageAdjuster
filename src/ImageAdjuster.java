/*
Copyright (C) 2007 David Bollinger
davebollinger (at) gmail (dot) com
http://www.davebollinger.com

This program is free software; you can redistribute it
and/or modify it under the terms of the GNU General
Public License as published by the Free Software
Foundation; either version 2 of the License, or (at
your option) any later version.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the
implied warranty of MERCHANTABILITY or FITNESS FOR A
PARTICULAR PURPOSE. See the GNU General Public License
for more details.

You should have received a copy of the GNU General
Public License along with this program; if not, write
to the Free Software Foundation, Inc., 59 Temple Place,
Suite 330, Boston, MA 02111-1307 USA
*/

package imageadjuster;
import processing.core.*;

/**
 * Provides methods for adjusting brightness, contrast and gamma of images.  Contains
 * both high-level methods that operate directly on images, and low-level methods that
 * operate on an internal color transformation matrix that can later be applied to
 * images.
 * <p>
 * High-level routines can be identified as those that take an image as their first
 * parameter.  Low-level routines can be identified as those that take only an adjustment
 * amount as a parameter.
 * <p>
 * The low-level routines are optimized for uses such as applying a predefined series
 * of adjustments repeatedly, for example while processing video frames.  It is possible
 * to define the transformation once, then apply it multiple times, potentially resulting
 * in condsiderable performance gains.
 * <p>
 * Version 0.05 - compile against Processing 1.0.3, primary difference is to remove support
 * for imageMode==CORNERS (since Processing 1.0.3 itself no longer supports it)
 *
 * @author David Bollinger
 * @author http://www.davebollinger.com
 * @version 0.04
 */
public class ImageAdjuster implements PConstants {
  /**
   * The current version of this code.
   * Note that only two decimal digits are significant.
   */
  public static final float version = 0.04f;
  /*
   * A reference to the applet that created this instance.
   */
  protected PApplet papplet;
  /*
   * The current color transformation in integers for optimized use by apply()
   */
  protected int [] ilut;
  /*
   * The current color transformation in native floating point.
   */
  protected float [] flut;
  /**
   * Indicates whether these routines should automatically maintain an image's pixels array
   * by calling loadPixels() and updatePixels() as necessary.
   * <P>
   * The default is true, indicating that these routines will automatically load and update an
   * image's pixels array every time it is accessed.  However, there are certain scenarios
   * where maintaining the pixels array manually may have performance advantages.  In those
   * cases, set this value to false, and pixel maintenance will not occur herein.  It is then
   * left to the user to manually ensure that the pixels array is adequately maintained.
   */
  public boolean autoMaintainPixels;
  //
  //
  //=========================================================================
  // CONSTRUCTOR
  //=========================================================================
  //
  //
  /**
   * Constructor.
   * For example:
   * <pre>
   *   ImageAdjuster adjust = new ImageAdjuster(this);
   * </pre>
   * @param papplet A reference to the applet that created this instance.
   */
  public ImageAdjuster(PApplet papplet) {
    this.papplet = papplet;
    ilut = new int[256];
    flut = new float[256];
    autoMaintainPixels = true;
    reset();
  }
  //
  //
  //=========================================================================
  // LOOKUP TABLE MANAGEMENT
  //=========================================================================
  //
  //
  /**
   * Resets the current color transformation to unity, such that all input
   * values in [0,255] map exactly to the same output values in [0,255].
   */
  public void reset() {
    for (int i=0; i<256; i++)
      flut[i] = (float)(i);
    buildILUT();
  }
  /**
   * Retrieves the current color transformation.  A very low-level routine
   * intended only for use in special situations.
   * For example:
   * <pre>
   *   ImageAdjuster adjust = new ImageAdjuster(this);
   *   adjust.contrast(1.5f);
   *   float [] mylut = new float[256];
   *   adjust.getLUT(mylut); // store current transformation
   *   adjust.brightness(g, 0.25f); // high-level call will modify transformation
   *   adjust.setLUT(mylut); // restore previous transformation
   * </pre>
   * @param  values  An array of 256 floating point values.
   * @throws RuntimeException  If array is null or length is not exactly 256.
   * @see #setLUT(float [])
   */
  public void getLUT(float [] values) {
    if (values == null)
      throw new RuntimeException("ImageAdjuster.getLUT(): lookup table value array is null.");
    if (values.length != 256)
      throw new RuntimeException("ImageAdjuster.getLUT(): length of lookup table value array must be exactly 256.");
    for (int i=0; i<256; i++)
      values[i] = flut[i];
  }
  /**
   * Sets the lookup table to user-specified values.  A very low-level routine
   * intended only for use in special situations.
   * For example:
   * <pre>
   *   ImageAdjuster adjust = new ImageAdjuster(this);
   *   float [] mylut = new float[256];
   *   for (int i=0; i<256; i++)
   *     mylut[i] = (float)(255-i); // reverse the raw values, aka "negate"
   *   adjust.setLUT(mylut);
   * </pre>
   * @param  values  An array of 256 floating point values.  Each value should be in
   *                 the range [0,255].  Values outside that range will be clipped.
   * @throws RuntimeException  If array is null or length is not exactly 256.
   * @see #getLUT(float [])
   */
  public void setLUT(float [] values) {
    if (values == null)
      throw new RuntimeException("ImageAdjuster.setLUT(): lookup table value array is null.");
    if (values.length != 256)
      throw new RuntimeException("ImageAdjuster.setLUT(): length of lookup table value array must be exactly 256.");
    for (int i=0; i<256; i++)
      flut[i] = peg(values[i]);
    buildILUT();
  }
  /**
   * Maps the current lookup table through user-specified values.  A very low-level routine
   * intended only for use in special situations.  The value of the user-array is used as an
   * index into the current color transformation.  That transformation value is then remapped
   * to the index of the user-array.  For example, if user array [0] is 255f, then the current
   * transformation value for color #255 is reassigned to color #0.  This can be used to
   * accomplish effects such as posterization, threshold, et cetera, that are not natively
   * supported herein.  The important distinction between doing such effects here versus using
   * any of Processing's built-in routines is that they can be accumulated here with other
   * transformations and applied en masse as a single operation.
   * For example:
   * <pre>
   *   ImageAdjuster adjust = new ImageAdjuster(this);
   *   adjust.contrast(1.5f);
   *   float [] mylut = new float[256];
   *   for (int i=0; i<256; i++)
   *     mylut[i] = (float)(255-i); // reverse the contrasted values, aka "negate"
   *   adjust.mapLUT(mylut);  // apply contrast+negate simultaneously
   * </pre>
   * @param  values  An array of 256 floating point values.  Each value should be in
   *                 the range [0,255].  Values outside that range will be clipped.
   * @throws RuntimeException  If array is null or length is not exactly 256.
   * @see #getLUT(float [])
   * @see #setLUT(float [])
   */
  public void mapLUT(float [] values) {
    if (values == null)
      throw new RuntimeException("ImageAdjuster.mapLUT(): lookup table value array is null.");
    if (values.length != 256)
      throw new RuntimeException("ImageAdjuster.mapLUT(): length of lookup table value array must be exactly 256.");
    float [] premap = new float[256];
    for (int i=0; i<256; i++)
      premap[i] = flut[i];
    for (int i=0; i<256; i++)
      flut[i] = premap[(int)(peg(values[i]))];
    buildILUT();
  }
  /*
   * Converts the floating point lookup table to an integer lookup table.
   */
  protected void buildILUT() {
    for (int i=0; i<256; i++)
      ilut[i] = (int)(flut[i]);  
  }
  //
  //
  //=========================================================================
  // UTILITY ROUTINES
  //=========================================================================
  //
  //
  /*
   * Contrains a value to the range [0,255]
   * @param value The value to be constrained.
   * @return The contrained value.
   */
  protected final float peg(float value) {
    return (value < 0f) ? 0f : ((value > 255f) ? 255f : value);
  }
  /*
   * Checks that specified image is not null.
   * @param img  The image to check.
   * @throws RuntimeException  If the image is null
   */
  protected void checkImage(PImage img) {
    if (img == null)
      throw new RuntimeException("ImageAdjuster: the specified image is null.");
  }
  //
  //
  //=========================================================================
  // APPLY -- WHERE THE REAL WORK GETS DONE
  //=========================================================================
  //
  //
  /**
   * Applies the currently specified color transformation to an entire image.
   * For example:
   * <pre>
   *   PImage img = loadImage("whatever.jpg");
   *   ImageAdjuster adjust = new ImageAdjuster(this);
   *   adjust.brightness(0.5f);
   *   adjust.contrast(1.25f);
   *   adjust.apply(img);
   * </pre>
   * @param  img  a PImage or subclass of PImage (f.e. PGraphics)
   * @see         #apply(PImage, int, int, int , int)
   */
  public final void apply(PImage img) {
    // this could have simply been a call to:
    //   apply(img, 0, 0, img.width, img.height);
    // but applying to an entire image is such a frequent operation that
    // code is duplicated here in order to optimize the looping
    checkImage(img);
    // setup for looping
    if (autoMaintainPixels)
    	img.loadPixels();
    int [] pix = img.pixels;
    int area = img.width * img.height;
    int index = 0;
    // perform adjustment    
    do {
      int c = pix[index];
      pix[index] = (c & 0xFF000000) | (ilut[c>>16&0xFF] << 16) | (ilut[c>>8&0xFF] << 8) | (ilut[c&0xFF]);
    } while (++index < area);
    if (autoMaintainPixels)
      img.updatePixels();
  }
  /**
   * Applies the currently specified color transformation to the specified area of an image.
   * For example:
   * <pre>
   *   PImage img = loadImage("whatever.jpg");
   *   ImageAdjuster adjust = new ImageAdjuster(this);
   *   adjust.brightness(0.5f);
   *   adjust.contrast(1.25f);
   *   adjust.apply(img, 0, 0, img.width/2, img.height/2);
   * </pre>
   * @param  img  a PImage or subclass of PImage (f.e. PGraphics)
   * @param  x    the left coordinate of the area
   * @param  y    the top coordinate of the area
   * @param  w    the width of the area
   * @param  h    the height of the area
   * @see         #apply(PImage)
   */
  public final void apply(PImage img, int x, int y, int w, int h) {
    checkImage(img);
    // clip coordinates
    if (x < 0) { w += x;  x = 0; }
    if (y < 0) { h += y;  y = 0; }
    if (x + w > img.width) { w = img.width - x; }
    if (y + h > img.height) { h = img.height - y; }
    // setup for looping
    if (autoMaintainPixels)
    	img.loadPixels();
    int [] pix = img.pixels;
    int index = y * img.width + x;
    int stride = img.width - w;
    // perform adjustment    
    for (int row=0; row<h; row++) {
      for (int col=0; col<w; col++) {
        int c = pix[index];
        pix[index++] = (c & 0xFF000000) | (ilut[c>>16&0xFF] << 16) | (ilut[c>>8&0xFF] << 8) | (ilut[c&0xFF]);
      }
      index += stride;
    }
    if (autoMaintainPixels)
      img.updatePixels();
  }
  //
  //
  //=========================================================================
  // BRIGHTNESS
  //=========================================================================
  //
  //
  /**
   * Applies a brightness adjustment to the current color transformation.  This is a low-level
   * routine intended for use when multiple discrete transformations are intended to be concatenated
   * and later applied simultaneously to an image.
   * @param amount The amount of brightness adjustment, typically in the range -1.0 through 1.0,
   *               where -1.0 represents full-scale brightness reduction resulting in a completely
   *               black result, 0.0 represent no alteration to original brightness, and 1.0
   *               represents full-scale increase resulting in a completely white result.  Values
   *               outside this range are allowed but will have no further effect.
   * @see #brightness(PImage,float)
   * @see #brightness(PImage,int,int,int,int,float)
   * @see #apply(PImage)
   */
  public void brightness(float amount) {
    amount *= 255f;
    for (int i=0; i<256; i++)
      flut[i] = peg(flut[i] + amount);
    buildILUT();
  }
  /**
   * Applies a brightness adjustment to an entire image.
   * @param  img  The image to adjust.
   * @param  amount  The amount of adjustment.
   * @see #brightness(float)
   * @see #brightness(PImage,int,int,int,int,float)
   * @see #apply(PImage)
   */
  public final void brightness(PImage img, float amount) {
    checkImage(img);
    brightness(img, 0, 0, img.width, img.height, amount);
  }
  /**
   * Applies a brightness adjustment to an area within an image.
   * @param  img  The image to adjust.
   * @param  x    the left coordinate of the area
   * @param  y    the top coordinate of the area
   * @param  w    the width of the area
   * @param  h    the height of the area
   * @param  amount  The amount of adjustment.
   * @see #brightness(float)
   * @see #brightness(PImage,float)
   * @see #apply(PImage)
   */
  public final void brightness(PImage img, int x, int y, int w, int h, float amount) {
    checkImage(img);
    reset();
    brightness(amount);
    apply(img, x, y, w, h);
  }
  //
  //
  //=========================================================================
  // CONTRAST
  //=========================================================================
  //
  //
  /**
   * Applies a contrast adjustment to the current color transformation.  This is a low-level
   * routine intended for use when multiple discrete transformations are intended to be concatenated
   * and later applied simultaneously to an image.
   * @param amount The amount of contrast adjustment, typically in the range 0.0 through 2.0,
   *               where 0.0 represents full-scale contrast reduction resulting in a completely
   *               gray result, 1.0 represent no alteration to original contrast, and 2.0
   *               represents a doubling of original contrast.  Values outside this range are
   *               allowed but are somewhat less useful.
   * @see #contrast(PImage,float)
   * @see #contrast(PImage,int,int,int,int,float)
   * @see #apply(PImage)
   */
  public void contrast(float amount) {
    for (int i=0; i<256; i++)
      flut[i] = peg((flut[i] - 127.5f) * amount + 127.5f);
    buildILUT();
  }
  /**
   * Applies a contrast adjustment to an entire image.
   * @param  img  The image to adjust.
   * @param  amount  The amount of adjustment.
   * @see #contrast(float)
   * @see #contrast(PImage,int,int,int,int,float)
   * @see #apply(PImage)
   */
  public final void contrast(PImage img, float amount) {
    checkImage(img);
    contrast(img, 0, 0, img.width, img.height, amount);
  }
  /**
   * Applies a contrast adjustment to an area within an image.
   * @param  img  The image to adjust.
   * @param  x    the left coordinate of the area
   * @param  y    the top coordinate of the area
   * @param  w    the width of the area
   * @param  h    the height of the area
   * @param  amount  The amount of adjustment.
   * @see #contrast(float)
   * @see #contrast(PImage,float)
   * @see #apply(PImage)
   */
  public final void contrast(PImage img, int x, int y, int w, int h, float amount) {
    checkImage(img);
    reset();
    contrast(amount);
    apply(img, x, y, w, h);
  }
  //
  //
  //=========================================================================
  // GAMMA
  //=========================================================================
  //
  //
  /**
   * Applies a gamma adjustment to the current color transformation.  This is a low-level
   * routine intended for use when multiple discrete transformations are intended to be concatenated
   * and later applied simultaneously to an image.
   * @param amount The amount of gamma adjustment, typically in the range 0.0 through 3.0,
   *               where 0.0 represents full-scale gamma reduction resulting in a strongly
   *               darkened result, 1.0 represent no alteration to original gamma, and 3.0
   *               represents a strong increase in gamma generally brightening the image.  Values
   *               outside this range are allowed but are somewhat less useful.
   * @see #gamma(PImage,float)
   * @see #gamma(PImage,int,int,int,int,float)
   * @see #apply(PImage)
   */
  public void gamma(float amount) {
    if (amount == 0f)
      amount = 0.000001f; // or return, or throw exception
    amount = 1f / amount;
    for (int i=0; i<256; i++)
      flut[i] = peg((float)Math.pow(flut[i]/255f, amount) * 255f);
    buildILUT();
  }
  /**
   * Applies a gamma adjustment to an entire image.
   * @param  img  The image to adjust.
   * @param  amount  The amount of adjustment.
   * @see #gamma(float)
   * @see #gamma(PImage,int,int,int,int,float)
   * @see #apply(PImage)
   */
  public final void gamma(PImage img, float amount) {
    checkImage(img);
    gamma(img, 0, 0, img.width, img.height, amount);
  }
  /**
   * Applies a gamma adjustment to an area within an image.
   * @param  amount  The amount of adjustment.
   * @param  x    the left coordinate of the area
   * @param  y    the top coordinate of the area
   * @param  w    the width of the area
   * @param  h    the height of the area
   * @param  img  The image to adjust.
   * @see #gamma(float)
   * @see #gamma(PImage,float)
   * @see #apply(PImage)
   */
  public final void gamma(PImage img, int x, int y, int w, int h, float amount) {
    checkImage(img);
    reset();
    gamma(amount);
    apply(img, x, y, w, h);
  }
}

