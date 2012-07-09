# Sim Jr #

This is a Git clone of the [Sim Jr project hosted on Google Code][1].

[1]: https://code.google.com/p/simjr/

The setup instructions for development differ here compared to Google Code because of the version control difference (Sim Jr on Google Code remains using SVN). Updated instructions are below.

# Development Setup #

## Overview ##

Sim Jr is composed of a set of OSGi plugins. As such, Sim Jr must be developed in Eclipse. Note that these instructions do *not* use the built in Eclipse team Git support.

1. Clone the repository (native Github client recommended if you are not comfortable with the command-line)
1. Open Eclipse (Java EE Juno recommended, otherwise other plugins need to be installed)
1. Import -> All projects in the Git root (don't copy)
1. Open `simjr-build/default.target` in Eclipse and select *Set as Target Platform* (upper-right corner)
