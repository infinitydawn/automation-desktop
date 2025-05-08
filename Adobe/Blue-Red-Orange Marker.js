var STARTING_NUMBER = 20; //Starting number for each color
var MARKER_SIZE = 18; //Text size of the created marking


var annots = this.getAnnots();
var pages = new Array(this.numPages);

//Create empty arrays for each page
for(var i = 0; i < pages.length; i++) {
    pages[i] = [];
}

//Sort by creation date
annots.sort(function(a, b){
    return a.creationDate - b.creationDate;
});

//Push each annotation into its respective page
for (var i = 0; i < annots.length; i++) {
    pages[annots[i].page].push(annots[i]);
}

var blue_Count = 0;
var red_Count = 0;
var orange_Count = 0;

//Go through each page, then each annotation per page, then add their markings
for(var i = 0; i < pages.length; i++) {
    pages[i].forEach(function(annot) {
        if(isBlueCircle(annot)) {
            addColoredNumber(annot, color.blue, STARTING_NUMBER + blue_Count);
            blue_Count++;
        }

        if(isRedCircle(annot)) {
            addColoredNumber(annot, color.red, STARTING_NUMBER + red_Count);
            red_Count++;
        }

        if (isOrangeCircle(annot)) {          
            addColoredNumber(annot, ["RGB", 1 , 0.6, 0 ], STARTING_NUMBER + orange_Count);
            orange_Count++;
        }
    });
}

function isBlueCircle(annot) {
    return annot.type === "Circle" && annot.fillColor.toString() === "RGB,0,0,1" && annot.strokeColor.toString() === "RGB,0,0,1";
}

function isRedCircle(annot) {
    return annot.type === "Circle" && annot.fillColor.toString() === "RGB,0.898040771484375,0.133331298828125,0.2156829833984375" &&
     annot.strokeColor.toString() === "RGB,0.898040771484375,0.133331298828125,0.2156829833984375";
}

function isOrangeCircle(annot) {
    return annot.type === "Circle" && annot.fillColor.toString() === "RGB,1,0.439208984375,0.007843017578125" && 
    annot.strokeColor.toString() === "RGB,1,0.439208984375,0.007843017578125";
}

function addColoredNumber(annot, annot_color, num) {
    // Get the current position of the annotation (as a rectangle: [x1, y1, x2, y2])
    var rect = annot.rect;

    // Calculate position 6 pixels above the center of the annot
    var xPos = (rect[0] + rect[2]) / 2; // X center
    var yPos = rect[1] + 6;             // 6 pixels above the top of the annot

    // Create a new FreeText annotation 6 pixels above, with white text, red background, and 50% opacity
    var newAnnot = this.addAnnot({
        type: "FreeText",               // FreeText annotation type
        page: annot.page,               // Same page as the annot
        rect: [xPos, yPos + 30, xPos, yPos + 30], // Text box with appropriate width and height
        contents: num.toString(),       // Just the number
        author: "Automated Script"      // Optional: Author name
    });

    newAnnot.fillColor = color.transparent;
    newAnnot.opacity = 1;              // Set background opacity to 50%
    newAnnot.strokeColor = annot_color;

    // Modify text color in the rich text content
    if (newAnnot.richContents) {
        var spans = [];
        for each(var span in newAnnot.richContents) {
            span.textColor = annot_color; // Set text color to white for each span
            span.textSize = MARKER_SIZE;
            spans.push(span);
        }
        newAnnot.richContents = spans;
    }
}
