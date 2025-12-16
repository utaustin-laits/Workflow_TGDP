#!/bin/sh

mkdir cmd
mkdir lib
mkdir bin
mkdir doc

tar -zxf tree-tagger-linux-3.2.5.tar.gz
echo 'TreeTagger version for PC-Linux installed.'

cp *.par lib/

for file in cmd/*
do
    awk '$0=="BIN=./bin"{print "BIN=\"'`pwd`'/bin\"";next}\
         $0=="CMD=./cmd"{print "CMD=\"'`pwd`'/cmd\"";next}\
         $0=="LIB=./lib"{print "LIB=\"'`pwd`'/lib\"";next}\
         {print}' $file > $file.tmp;
    mv $file.tmp $file;
done
echo 'Path variables modified in tagging scripts.'

chmod 0755 cmd/*
