import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;

class ZoneList {
    private String FILENAME = "assets/temp_zones.csv";

    ArrayList<Zone> zones = new ArrayList<Zone>();
    boolean CONTAINS_AR = false;
    int AP_START = 1;

    public ZoneList() {

    } // end constructor

    public void addZone(double address, String tag1, String tag2) {
        String decimal = Double.toString(address);
        decimal = decimal.substring(decimal.length() - 1, decimal.length());

        if (decimal.equals("1") || decimal.equals("0")) {
            zones.add(new Zone(address, tag1, tag2));
        } else if (decimal.equals("2")) {
            Zone last = zones.get(zones.size() - 1);
            last.addSubZone(address, tag1, tag2);
        }
    }

    public void displayZoneList() {
        for (Zone zone : zones) {
            if (!zone.getType().equals("Blank Device")) {
                System.out.println(zone.getZoneinfo());
                if(zone.getSubAddress() != null){
                    System.out.println("  |__ " + zone.getSubAddress().getZoneinfo());
                }
            }
        }
    }

    public void readFile() throws Exception {
        File temp_file = new File(FILENAME);
        Scanner temp_scan = new Scanner(temp_file);

        String headers = temp_scan.nextLine();

        //Get AP Start from headers if it exists
        int ap_start_index = headers.toLowerCase().indexOf("ap start");

        if(ap_start_index > 0) {
            String nums = "";
            int next_comma = Math.min(headers.indexOf(",", ap_start_index), headers.length());
            for (char c : headers.substring(ap_start_index, next_comma).toCharArray()) {
                if (Character.isDigit(c)) {
                    nums += c;
                }
            }
            
            AP_START = Integer.parseInt(nums);
        }

        String[] parts;
        String content = "";

        while (temp_scan.hasNext()) {
            content = temp_scan.nextLine();
            parts = content.split("\\,");

            this.addZone(Double.parseDouble(parts[0]), parts[1], parts[2]);
            /*
            //Load options if they exist
            if(parts.length > 4 && parts[4] != null && !parts[4].isEmpty()) {

                //Turn something like waterflow/valve to single input (for Flexnet)
                if(parts[4].toLowerCase().contains("single")) {
                    zones.getLast().setDualInput(false);
                }
            }
            */
            if (zones.getLast().isAR()) {
                CONTAINS_AR = true;
            }
        }

        temp_scan.close();
    }
}