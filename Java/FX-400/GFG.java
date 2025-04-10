
// Java program using label (swing)
// to display the message “GFG WEB Site Click”
import java.io.*;
import javax.swing.*;

// Main class
class GFG {

    // Main driver method
    public static void main(String[] args) {

        // Using this process to invoke the constructor,
        // JFileChooser points to user's default directory
        JFileChooser j = new JFileChooser("Quick access");

        // Open the save dialog
        j.showSaveDialog(null);
    }
}