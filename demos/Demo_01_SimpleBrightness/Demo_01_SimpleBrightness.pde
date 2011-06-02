import imageadjuster.*;

/**
Illustrates usage of the high-level brightness() method
*/

size(200,200);
background(loadImage("milan_rubbish.jpg")); 
ImageAdjuster adjust = new ImageAdjuster(this);
adjust.brightness(g,   0, 0, 50, 200, -0.50f);
adjust.brightness(g,  50, 0, 50, 200, -0.25f);
adjust.brightness(g, 100, 0, 50, 200,  0.25f);
adjust.brightness(g, 150, 0, 50, 200,  0.50f);

