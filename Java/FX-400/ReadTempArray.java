import java.io.File; // Import the File class
import java.util.Scanner; // Import the Scanner class to read text files

public class ReadTempArray {

    // public static void main(String[] args) throws Exception {
    //     readFile();
    // }

    public String [][] readFile() throws Exception {
        File tempFile = new File("assets/temp_zones.csv");
        Scanner tempScan = new Scanner(tempFile);
        String content = "";

        tempScan.nextLine();

        while (tempScan.hasNext()) {
            content += tempScan.nextLine();
        }

        tempScan.close();

        //System.out.println(content);

        String[] parts = content.split("\\,");
        String[] addresses = new String[parts.length / 3];
        String[] tag1 = new String[parts.length / 3];
        String[] tag2 = new String[parts.length / 3];

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
                    partIdx = 1;
                    newIdx++;
                    break;

                default:
                    break;
            }

            origIdx++;
        }

        String [][] result = {addresses,tag1,tag2};

        return result;
    }
}
