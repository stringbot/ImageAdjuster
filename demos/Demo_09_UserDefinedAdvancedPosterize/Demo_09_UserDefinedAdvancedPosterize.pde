import imageadjuster.*;

/**
Illustrates using the very low-level LUT routines to accomplish a "posterize" operation.<br>
The mapLUT() method is used to apply the posterize operation on values that have already<br>
been adjusted by brightness and contrast.  Compare with Demo_08_UserDefinedPosterize where<br>
the setLUT() method is used to apply ONLY the posterize operation.
*/

size(200,200);
background(loadImage("milan_rubbish.jpg")); 
ImageAdjuster adjust = new ImageAdjuster(this);

// set up the brightness and contrast adjustments
adjust.brightness(0.25f);
adjust.contrast(1.5f);

// create a custom lookup table
float [] mylut = new float[256];

// populate it with 4-level posterize values
for (int i=0; i<256; i++)
  mylut[i] = (float)((((i * 4) >> 8) * 255) / 3);
  
// remap the adjuster based on the custom lookup table
adjust.mapLUT(mylut);

// perform the adjustment
adjust.apply(g);
