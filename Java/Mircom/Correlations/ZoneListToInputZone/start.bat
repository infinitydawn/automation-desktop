javac -cp  ../dependencies/* ../../DataEntryBot.java  FSAEBot.java Automation.java -d .
java --enable-native-access=ALL-UNNAMED -cp "../dependencies/*;."  Automation.java
PAUSE