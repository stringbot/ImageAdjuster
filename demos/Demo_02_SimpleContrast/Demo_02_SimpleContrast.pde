import imageadjuster.*;

/**
Illustrates usage of the high-level contrast() method
*/

size(200,200);
background(loadImage("milan_rubbish.jpg")); 
ImageAdjuster adjust = new ImageAdjuster(this);
adjust.contrast(g,   0, 0, 50, 200, 0.25f);
adjust.contrast(g,  50, 0, 50, 200, 0.75f);
adjust.contrast(g, 100, 0, 50, 200, 2.0f);
adjust.contrast(g, 150, 0, 50, 200, 3.0f);

