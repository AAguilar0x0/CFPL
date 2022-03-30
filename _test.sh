if [ -z "$1" ]
    then
        echo "No file argument supplied."
    else
        javac CFPL.java
        java CFPL "./tests/$1"
fi