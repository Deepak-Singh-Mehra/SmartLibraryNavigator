# SmartLibraryNavigator
:: compile
javac -cp ".;backend\lib\gson-2.13.1.jar" backend\*.java

:: run
java -cp ".;backend\lib\gson-2.13.1.jar;backend" backend.Main
