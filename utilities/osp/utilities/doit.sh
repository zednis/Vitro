#!/bin/bash

#This is a script that runs the osp import

#set the working directory here
cd /usr/local/src/Vitro/dream/utilities/osp/utilities

#set the properties file here
propertiesfile=vivo2_jdbc.properties

echo "-------------------------------------------------------------------"
echo "osp download on $(date) as user $(whoami) on system $(hostname)."
echo "using working directory $(pwd) "
echo "using properties file   $(pwd)$propertiesfile"

./run.sh OSPDWDownload $propertiesfile true 
./run.sh LocalOsp2Vivo $propertiesfile  

echo "script done"

