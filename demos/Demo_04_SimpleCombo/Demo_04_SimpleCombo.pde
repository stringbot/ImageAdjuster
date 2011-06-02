import imageadjuster.*;

/**
Illustrates usage of the high-level brightness()+contrast()+gamma() methods.<br>
This is not the recommended approach -- compare with Demo_05_AdvancedCombo.
*/

size(200,200);
background(loadImage("milan_rubbish.jpg")); 
ImageAdjuster adjust = new ImageAdjuster(this);
adjust.brightness(g, 0.25f);
adjust.contrast(g, 1.5f);
adjust.gamma(g, 0.55f);

