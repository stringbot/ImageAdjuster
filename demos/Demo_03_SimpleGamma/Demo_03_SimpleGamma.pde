import imageadjuster.*;

/**
Illustrates usage of the high-level gamma() method
*/

size(200,200);
background(loadImage("milan_rubbish.jpg")); 
ImageAdjuster adjust = new ImageAdjuster(this);
adjust.gamma(g,   0, 0, 50, 200, 0.25f);
adjust.gamma(g,  50, 0, 50, 200, 0.75f);
adjust.gamma(g, 100, 0, 50, 200, 1.25f);
adjust.gamma(g, 150, 0, 50, 200, 1.75f);

