import imageadjuster.*;

/**
Illustrates using the very low-level LUT routines to accomplish a "posterize" operation.<br>
*/

size(200,200);
background(loadImage("milan_rubbish.jpg")); 
ImageAdjuster adjust = new ImageAdjuster(this);

// create a custom lookup table
float [] mylut = new float[256];

// populate it with 4-level posterize values
for (int i=0; i<256; i++)
  mylut[i] = (float)((((i * 4) >> 8) * 255) / 3);
  
// tell the adjuster to use custom lookup table
adjust.setLUT(mylut);

// perform the adjustment
adjust.apply(g);
