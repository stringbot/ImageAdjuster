import imageadjuster.*;

/**
Illustrates accumulation of transformations, and the difference between low- and high-level methods.<br>
*/

size(200,200);
background(loadImage("milan_rubbish.jpg")); 
ImageAdjuster adjust = new ImageAdjuster(this);

// begin accumulating transforms using low-level methods
adjust.brightness(0.25f);
adjust.apply(g, 0, 0, 50, 200); // just brightness 0.25

// continue accumulating transforms using low-level methods
adjust.contrast(1.5f);
adjust.apply(g, 50, 0, 50, 200); // brightness 0.25 AND contrast 1.5

// using a high-level method will reset the transformation to just the operation specified
// effectively the same as this series of low-level calls:
//   adjust.reset();
//   adjust.gamma(0.55f);
//   adjust.apply(g, 100, 0, 50, 200);
adjust.gamma(g, 100, 0, 50, 200, 0.55f);

// accumulate using low-level methods
adjust.brightness(0.25f);
adjust.apply(g, 150, 0, 50, 200); // gamma 0.55 (from high-level) AND brightness 0.25 (from low-level)

