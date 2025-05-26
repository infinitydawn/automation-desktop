var STARTING_NUMBER = 20; //Starting number for each color
var MARKER_SIZE = 18; //Text size of the created marking

// Press CTRL+J to open debugger, paste everything, CTRL+A to select all, CTRL+Enter to run

var annots = this.getAnnots();
var pages = new Array(this.numPages);

var COLOR_DICT = {
     "RGB,0.898040771484375,0.133331298828125,0.2156829833984375" : "Red",
     "RGB,0.415679931640625,0.850982666015625,0.1568603515625" : "Green",
     "RGB,0,0,1" : "Blue",
     "RGB,1,0.439208984375,0.007843017578125" : "Orange",
     "RGB,1,0.7529449462890625,0.6196136474609375" : "Light Orange",
     "RGB,1,0.819610595703125,0" : "Yellow",
     "RGB,0.9882354736328125,0.9568634033203125,0.521575927734375" : "Light Yellow",
     "RGB,0.2196044921875,0.898040771484375,1" : "Light Blue",
     "RGB,0.63922119140625,0.188232421875,0.5254974365234375" : "Purple",
     "RGB,0.98431396484375,0.5333404541015625,1" : "Purple Pink",
     "RGB,1,0.5019683837890625,0.615692138671875" : "Light Pink",
     "RGB,0.415679931640625,0.850982666015625,0.1568603515625" : "Dark Green",
     "RGB,0.772552490234375,0.98431396484375,0.447052001953125" : "Light Green",
     "RGB,0,0,0" : "Black",
     "RGB,0.26666259765625,0.26666259765625,0.26666259765625" : "Gray 4",
     "RGB,0.4666595458984375,0.4666595458984375,0.4666595458984375" : "Gray 3",
     "RGB,0.6666717529296875,0.6666717529296875,0.6666717529296875" : "Gray 2",
     "RGB,0.8000030517578125,0.8000030517578125,0.8000030517578125" : "Gray 1",
     "RGB,1,1,1" : "White",
};

var color_count = {};

//Start each color count at STARTING_NUMBER
for each(var color in Object.keys(COLOR_DICT)) {
    color_count[color] = STARTING_NUMBER;
}

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

//Go through each page, then each annotation per page, then add their markings
for(var i = 0; i < pages.length; i++) {
    pages[i].forEach(function(annot) {
        if(annot.type === "Circle") {
            addColoredNumber(annot, color_count[annot.fillColor.toString()]);
            color_count[annot.fillColor.toString()]++;
        }
    });
}

function addColoredNumber(annot, num) {   
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
        author: "Automated Script",      // Optional: Author name
        fillColor : color.transparent,
        opacity : 1,
        strokeColor : annot.fillColor,
    });


    // Modify text color in the rich text content
    if (newAnnot.richContents) {
        var spans = [];
        for each(var span in newAnnot.richContents) {
            span.textColor = annot.fillColor;
            span.textSize = MARKER_SIZE;
            spans.push(span);
        }
        newAnnot.richContents = spans;
    }
}
