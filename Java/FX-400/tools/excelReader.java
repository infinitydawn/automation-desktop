package tools;

import java.io.File;

public class excelReader {

    public static void main(String[] args) {
        File excel = GFG.getExcelFile();

        System.out.println("Selected file: " + excel.getAbsolutePath());
    }
}
