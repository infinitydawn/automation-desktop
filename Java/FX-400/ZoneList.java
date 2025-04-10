import java.util.ArrayList;

class ZoneList {
    ArrayList<Zone> zones = new ArrayList<Zone>();

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
}