ImageAdjuster v0.04
Copyright (C) 2007 David Bollinger
(see gpl.txt for license)

As of 2007-Jan-12, Processing Beta 0123:
As if 2009-May-26, Processing 1.0.3

Note: I have added this library to GitHub because the original maintainer's
website seems to have disappeared. I didn't write it, I haven't
contributed to it, I am just a user who felt like it should still be
available someplace. David, if you see this and want it taken down just
let me know. -@stringbot

WHAT IS IT?
-----------

Adjusts image brightness, contrast, gamma, and more.
Contains high-level routines to simplify basic usage.
Contains low-level routines for high-performance advanced usage.


TO INSTALL LIBRARY:
-------------------

Copy the imageadjuster folder into your Processing libraries folder.

You do not need the ImageAdjuster.java source code, or make files
and et cetera, if you only intend to use the library as is.  Source
code is provided only for those who intend to modify the library
itself for their own specific needs.


TO USE LIBRARY:
---------------

From the Processing IDE, use the Sketch-Import Library menu to
import the library into your sketch.  You should see the following
line added at the top of your sketch:

import imageadjuster.*;

Create an instance of the ImageAdjuster class, for example:

ImageAdjuster adjust = new ImageAdjuster(this);

Perform some type of adjustment, for example:

adjust.brightness(g, 0.25f);

See demos and documentation for further usage.


THE DEMOS:
----------

The demos are intended to be studied in order.  They are arranged
roughly by increasing complexity, therefore you should start with
the simplest ones first before proceeding to the more complex
ones.  Otherwise the complex ones may appear frightening.  :D

