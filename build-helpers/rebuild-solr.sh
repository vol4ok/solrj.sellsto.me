#!/bin/sh
#immediately exit if any of these commands fail
set -e

#run build of the lucene project
cd ./../../solr/trunk/lucene
ant jar-core
#copy lucene core lib to a projects libs
cp ./build/lucene-core-4.0-SNAPSHOT.jar ./../../../solrj.sellsto.me/lib/main/provided/lucene-core-4.0.jar -f

#run build of the solr project
cd ./../solr
ant dist
#copy builded project lib to a solrj.sellsto.me project
cp ./dist/apache-solr-core-4.0-SNAPSHOT.jar    ./../../../solrj.sellsto.me/lib/main/provided/solr-core-4.0.jar -f
cp ./dist/apache-solr-solrj-4.0-SNAPSHOT.jar   ./../../../solrj.sellsto.me/lib/main/provided/solr-solrj-4.0.jar -f

#update solr distribution for tomcat instance
cp ./dist/apache-solr-4.0-SNAPSHOT.war /usr/local/tomcat7/webapps/solr.war -f