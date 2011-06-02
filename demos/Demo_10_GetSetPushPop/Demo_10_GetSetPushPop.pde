import imageadjuster.*;

/**
Illustrates using the very low-level LUT routines to accomplish push/pop operations.<br>
*/

size(200,200);
background(loadImage("milan_rubbish.jpg")); 
ImageAdjuster adjust = new ImageAdjuster(this);

// create a custom lookup table
float [] mylut = new float[256];

// define a fancy transformation
adjust.brightness(0.25f);
adjust.contrast(1.5f);
adjust.gamma(0.55f);

// store that transformation for later use (aka "push" it)
adjust.getLUT(mylut);

// interrupt the fancy transform
// by using a high-level method that will reset the current transformation
adjust.brightness(g, 0, 0, 100, 200, -0.25);

// restore the transformation (aka "pop" it)
adjust.setLUT(mylut);

// apply the fancy transformation
adjust.apply(g, 100, 0, 100, 200);
