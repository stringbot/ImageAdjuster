set LIB=imageadjuster
mkdir %LIB%
erase %LIB%\*.class
%JDK_HOME%\bin\javac -source 1.3 -target 1.1 -d . -classpath %P5_HOME%\lib\core.jar *.java
mkdir %LIB%\library
erase %LIB%\library\*.jar
%JDK_HOME%\bin\jar cvf %LIB%\library\%LIB%.jar %LIB%\*.class
mkdir docs
erase /q docs
%JDK_HOME%\bin\javadoc -author -version -d docs *.java
