import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;

class ZoneList {
    private String FILENAME = "assets/temp_zones.csv";

    ArrayList<Zone> zones = new ArrayList<Zone>();
    boolean CONTAINS_AR = false;
    boolean CONTAINS_DUAL_HEAT = false;
    int AP_START = 1;

    public ZoneList() {

    } // end constructor

    public void addZone(double address, String tag1, String tag2) {
        if (!isSubZone(address)) {
            zones.add(new Zone(address, tag1, tag2));
        } else {
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

            Zone last_zone = zones.getLast();
            if(isSubZone(Double.parseDouble(parts[0]))) {
                last_zone = last_zone.getSubAddress();
            }

            //Load Overrides from column 4 if they exist. Overrides are separated by ;
            if(parts.length > 3 && parts[3] != null && !parts[3].isEmpty()) {
                String[] overrides = parts[3].split(";");
                boolean toggle;

                for(String over : overrides) {
                    toggle = true;

                    //Override Type
                    if(over.contains("type=")) {
                        String[] new_type = over.split("type=");
                        if(new_type.length > 0) {
                            last_zone.setType(new_type[1]);
                        }
                        else {
                            System.out.println(last_zone.getAddress() + " : invalid type provided");
                        }
                    }

                    if(over.contains("!")) {
                        toggle = false;
                    }

                    //Set AR
                    if(over.contains("isAR")) {
                        last_zone.setAR(toggle);
                    }

                    //Set Dual
                    if(over.contains("isDualInput")) {
                        last_zone.setDualInput(toggle);
                    }

                    //Set NS
                    if(over.contains("isNS")) {
                        last_zone.setNS(toggle);
                    }
                }

                /*
                System.out.println(last_zone.getAddress()
                 +  " type : " + last_zone.getType() 
                 +  ", isAR:" + last_zone.isAR() 
                 + ", isNS: " + last_zone.isNS() 
                 + " , isDual: " + last_zone.isDualInput()); 
                 */
            }

            if (last_zone.isAR()) {
                CONTAINS_AR = true;
            }

            if (last_zone.getType().equals("Heat Detector") && last_zone.isDualInput()) {
                CONTAINS_DUAL_HEAT = true;
            } 
        }
        temp_scan.close();
    }

    public boolean isSubZone(Double address) {
        String decimal = Double.toString(address);
        decimal = decimal.substring(decimal.length() - 1, decimal.length());

        if (decimal.equals("1") || decimal.equals("0")) {
            return false;
        } else if (decimal.equals("2")) {
            return true;
        }

        return false;
    }
}
