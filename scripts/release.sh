#!/bin/bash
set -e

#
# This script do version releases :
#   1. change the project version from x.y-SNAPSHOT to x.y
#   2. create a git tag and push it
#   3. run mvn deploy of the release version
#   4. change the project version to x.(y+1)-SNAPSHOT
#   5. commit the version change

#go to the root directory
cd ..

#fetch the current snapshot version
python > version.txt << "_EOF"
import re, xml.etree.ElementTree as ET
root = ET.parse('pom.xml')
print root.find('{http://maven.apache.org/POM/4.0.0}version').text
_EOF
export currentVersion=`cat version.txt`
rm version.txt
export releaseVersion=`echo $currentVersion | sed -e s/-SNAPSHOT//`

echo "Current Version : " $currentVersion
echo "Release Version : " $releaseVersion

#change version in the pom
mvn versions:set -DnewVersion=$releaseVersion
if [ $? -ne 0 ]; then
	mv pom.xml.versionsBackup pom.xml
	echo 'versions:set failed' 1>&2
	exit 1
fi
rm -f pom.xml.versionsBackup

#create a git tag
git commit pom.xml -m"releasing to version $releaseVersion"
git tag "$releaseVersion"
git push origin --tags


#update minor version
python > version.txt << "_EOF"
import os
v = os.environ['releaseVersion'].split('.')
v[-1] = str( int(v[-1]) +1 )
print '.'.join(v)+'-SNAPSHOT'
_EOF

curl https://jitpack.io/#jrialland/ajp-client/$releaseVersion

#deploy the release version
mvn clean deploy

export newVersion=`cat version.txt`
rm version.txt
echo "New Version : " $newVersion

#change version in pom
mvn versions:set -DnewVersion=$newVersion
rm -f pom.xml.versionsBackup

#commit
git commit pom.xml -m"switching version to $newVersion after the release of $releaseVersion"
git push







