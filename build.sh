#!/bin/sh

#todo here define a entry point to all build system
cd build-helpers
sh rebuild-solr.sh
cd ..
gradle configSolr