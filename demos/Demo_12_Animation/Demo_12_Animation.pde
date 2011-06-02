import imageadjuster.*;

/**
Animation of color adjustments.<br>
*/

BouncyAdjustBox [] boxes;
int nboxes = 3;
PImage bkgnd;
ImageAdjuster adjust;

void setup() {
  size(200,200);
  bkgnd = loadImage("milan_rubbish.jpg"); 
  adjust = new ImageAdjuster(this);
  boxes = new BouncyAdjustBox[nboxes];
  boxes[0] = new BouncyAdjustBox(0, 0.25f);
  boxes[1] = new BouncyAdjustBox(1, 1.5f);
  boxes[2] = new BouncyAdjustBox(2, 0.55f);
}

void draw() {
  background(bkgnd);
  for (int i=0; i<nboxes; i++)
    boxes[i].draw();
}

class BouncyAdjustBox {
  int x, y, w, h, dx, dy;
  int operation;
  float amount;
  BouncyAdjustBox(int op, float amt) {
    operation = op;
    amount = amt;
    w = 50;
    h = 50;
    x = (int)(random(width-w));
    y = (int)(random(height-h));
    dx = (int)(random(1,5));
    dy = (int)(random(1,5));
  }
  void draw() {
    x += dx;
    if ((x < 0) || (x > width-w)) {
      x -= dx;
      dx = -dx;
    }
    y += dy;
    if ((y < 0) || (y > height-h)) {
      y -= dy;
      dy = -dy;
    }
    switch(operation) {
      case 0 : adjust.brightness(g, x, y, w, h, amount); break;
      case 1 : adjust.contrast(g, x, y, w, h, amount); break;
      case 2 : adjust.gamma(g, x, y, w, h, amount); break;
    }
  }
}
