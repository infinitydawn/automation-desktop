python ./tools/excelReader.py
javac -cp  dependencies/* Automation.java FX400.java ZoneList.java Zone.java DataEntryBot.java FX2000.java Flexnet.java
java --enable-native-access=ALL-UNNAMED -cp dependencies/* Automation.java

