#!/bin/bash

cd ./src/
javac Main.java
java -Duser.dir=../ Main.java
