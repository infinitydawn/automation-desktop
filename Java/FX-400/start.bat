python ./tools/excelReader.py
javac -cp  dependencies/* Automation.java FX400.java ZoneList.java Zone.java ReadTempArray.java DataEntryBot.java
java -cp  dependencies/* Automation.java
