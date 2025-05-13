import java.io.File; // Import the File class
import java.util.Scanner; // Import the Scanner class to read text files
import java.util.Arrays;


public class ReadTempArray {

    public String [][] readFile() throws Exception {
        File tempFile = new File("assets/temp_zones.csv");
        Scanner tempScan = new Scanner(tempFile);
        String content = "";

        String headers = tempScan.nextLine();

        while (tempScan.hasNext()) {
            content += tempScan.nextLine();
        }

        tempScan.close();

        //System.out.println(content);

        String[] parts = content.split("\\,");
        String[] addresses;
        String[] tag1;
        String[] tag2;
        String[] options;
    
        if(headers.contains("Options")) {
            options = new String[parts.length / 4];
            addresses = new String[parts.length / 4];
            tag1 = new String[parts.length / 4];
            tag2 = new String[parts.length / 4];
        } 
        else {
            addresses = new String[parts.length / 3];
            tag1 = new String[parts.length / 3];
            tag2 = new String[parts.length / 3];
            options = null;
        }

        int partIdx = 1;
        int origIdx = 0;
        int newIdx = 0;

        while(origIdx < parts.length) {
            switch (partIdx) {
                case 1:
                    addresses[newIdx] = parts[origIdx];
                    partIdx++;
                    break;
                case 2:
                    tag1[newIdx] = parts[origIdx];
                    partIdx++;
                    break;
                case 3:
                    tag2[newIdx] = parts[origIdx];                 
                    if(options != null) {
                        options[newIdx] = parts[origIdx];
                    }
                    partIdx = 1;
                    newIdx++;
                    break;
                default:
                    break;
            }

            origIdx++;
        }
    
        String [][] result = {addresses,tag1,tag2, options};
        
        return result;
    }
}
