import imageadjuster.*;

/**
Going crazy with LUT routines.<br>
Not intended to have any practical application, but illustrates many low-level concepts.<br>
Goal to accomplish:<br>
  1) apply brightness<br>
  2) invert the brightened values<br>
  3) apply contrast to inverted brightened values<br>
  4) apply a crazy sine wave remap to the contrasted inverted brightened values<br>
*/

size(200,200);
background(loadImage("milan_rubbish.jpg")); 
ImageAdjuster adjust = new ImageAdjuster(this);
float [] mylut = new float[256];

// 1) brightness
adjust.brightness(0.25f);

// 2) negate/invert
for (int i=0; i<256; i++)
  mylut[i] = (float)(255-i);
adjust.mapLUT(mylut);

// 3) contrast
adjust.contrast(1.5f);

// 4) wacky sine wave remap
// (sort of like a double gamma adjustment:  0-->0, 128-->255, 255-->0)
for (int i=0; i<256; i++)
  mylut[i] = sin((float)(i)/255f*PI) * 255f;
adjust.mapLUT(mylut);

// finally, apply this silly transform
adjust.apply(g);

// and to reiterate... the advantage of such an approach is that now
// your very elaborate transformation can be re-apply()-ed, for example
// to subsequent video frames, with very high performance.
