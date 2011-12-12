#!/bin/sh
#immediately exit if any of these commands fail
set -e

#run build of the lucene project
cd ./../../solr/trunk
svn update #update working directory from trunk
cd ./lucene
ant jar-core
#copy lucene core lib to a projects libs
cp ./build/lucene-core-4.0-SNAPSHOT.jar ./../../../solrj.sellsto.me/build-solr/lucene-core-4.0.jar -f
cd ./../modules
ant generate-maven-artifacts
cp ./queryparser/build/lucene-queryparser-4.0-SNAPSHOT.jar ./../../../solrj.sellsto.me/build-solr/lucene-queryparser-4.0.jar  -f

#run build of the solr project
cd ./../solr
ant dist
#copy builded project lib to a solrj.sellsto.me project
cp ./dist/apache-solr-core-4.0-SNAPSHOT.jar    ./../../../solrj.sellsto.me/build-solr/solr-core-4.0.jar -f
cp ./dist/apache-solr-solrj-4.0-SNAPSHOT.jar   ./../../../solrj.sellsto.me/build-solr/solr-solrj-4.0.jar -f

#update solr distribution for tomcat instance
cp ./dist/apache-solr-4.0-SNAPSHOT.war /usr/local/tomcat7/webapps/solr.war -f
cp ./dist/apache-solr-4.0-SNAPSHOT.war ./../../../solrj.sellsto.me/server/solr.war -f
cd ./../../../solrj.sellsto.me/