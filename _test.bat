@echo off
if [%1] == [] (
    echo No file argument supplied.
) else (
    javac CFPL.java
    java CFPL "./tests/%1"
)