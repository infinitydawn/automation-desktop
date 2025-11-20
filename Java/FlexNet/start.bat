javac -cp  ../dependencies/* DataEntryBot.java Automation.java FSAEBot.java
java --enable-native-access=ALL-UNNAMED -cp ../dependencies/* Automation.java
PAUSE