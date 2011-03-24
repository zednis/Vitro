This file is a brief description of the biosis load project.

The file HowToDownloadBiosisRecentPubs.txt describes how to do the
actual download from a biosis into vivo/vitro. 

This directory contains the following directories:

Build............................... a directory that get created by
                                     ant for classes
data ............................... downloaded from biosis
lib................................  for  jars that this project uses.
logs................................ of the execution of the biosis load
                                     and baseline logs for regression
                                     testing
src ................................ source code

This directory contains the following files:
README.txt.......................... this file
build.properties.................... properties for configuring the build
build.xml. ......................... ant build script
doAll.sh ........................... that will do the steps of the load
vivo2_jdbc.properties .............. jdbc connection strings.
doc/
 HowToDownloadBiosisRecentPubs.txt... instructions on doing a download.
 NotesBiosis.txt..................... some notes on future development
bin/
 resetdb.sh ......................... to load a mysqldump
 unit.sh ............................ simple test


Make sure to check the jdbc connection strings in
vivo2_jdbc.properties before attemping to run unit.sh so that you
don't overwrite you existing database.
