This directory contains a set of python and related
code to generate the graph images, imagemaps and html
pages for the Vitro system.  Right now we will start
with classes and properties.

The plan is to use the program dot, which takes a 
file as input that describes a set of relations
and produces a graph image and imagemap, mysql,
and python scripts to connect these pieces.  

Work flow to be directed by a python script:
a) query db for a list of classes
b) foreach class make a dot file:
   b.1 get each superclass and append in 
   dot format append to dot file for that class
   b.2 get each subclass and add to dot file
   b.3 get each property and add to dot file
c) run each dot file for image
d) run each dot file for image map and post process to
   create a client side imagemap.
e) create a html to serve up the image and map

Name schema:
This can be generated as a directory with all the files
and then moved to the vivo directory.

graph image:            /vivo/graph/$classname.jpg  
graph imagemap:         /vivo/graph/$classname.imgmap
graph test page:        /vivo/graph/$classname.html
