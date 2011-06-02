import imageadjuster.*;

/**
Illustrates usage of the low-level brightness()+contrast()+gamma() and apply() methods.<br>
This is the recommended approach -- compare with Demo_04_SimpleCombo.
*/

size(200,200);
background(loadImage("milan_rubbish.jpg")); 
ImageAdjuster adjust = new ImageAdjuster(this);
adjust.brightness(0.25f);
adjust.contrast(1.5f);
adjust.gamma(0.55f);
// now perform all three operations with only a single pass through the pixels
// (this is also a bit more accurate because less intermediate rounding occurs)
adjust.apply(g);

