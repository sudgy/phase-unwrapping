Copyright (C) 2019 Portland State University
Phase Unwrapping for ImageJ2 - David Cohoe

Phase Unwrapping is a pluging for ImageJ2 that performs phase unwrapping on
phase images.  There are several different algorithms that may be used to
perform this unwrapping.  Some of them also have ways extend them through
additional plugins.

INSTALLATION

To install the plugin, the update site "DHM Utilities" with the URL
"http://sites.imagej.net/Sudgy/" must be added in the ImageJ updater.  If you
want to modify the plugin, or if you want to install the plugin without
everything else from DHM utilities, compile it with maven and then copy the jar
to the ImageJ plugins folder, removing the old one if you need to.  This plugin
depends on dynamic_parameters, another plugin in DHM utilities, which can be
found at https://github.com/sudgy/dynamic-parameters.  The documentation can be
created using maven's javadoc plugin, and will be created in
target/site/apidocs/.

USE

Currently, although others may be added in the future, there are only two
algorithms supported: a quality-guided single wavelength unwrapping algorithm,
and a double wavelength algorithm.  These commands are found in "Plugins > DHM
Utilities > Phase Unwrapping".  When the plugins ask for the pixel phase value,
that means the difference between the maximum phase value and the minimum phase
value on the image (for example, if your image is 32-bit and goes from -π to π,
you would put 2π in as the phase value, or if your image is 8-bit and goes from
0 to 256, you would put 256 in as the phase value).  Both algorithms let you see
the intermediate steps if you wish.

The quality-guided algorithm may be extended by any programmer by making a
Scijava @Plugin of type Quality.  If you want to have parameters for your
quality, you must use a dynamic parameter from that plugin.  The name of the
plugin that you create will be the name shown on the dialog where you pick which
quality to use.  The priority of the plugin will determine the order that they
are displayed on the dialog, with the highest priority being on the top.

For more specifics on any of the algorithms and the different quality types,
please consult the documentation.


If you have any questions that are not answered here, in the documentation, or
in the source code, please email David Cohoe at dcohoe@pdx.edu.
