var STARTING_NUMBER = 20; //Starting number for each color
var MARKER_SIZE = 18; //Text size of the created marking
var USE_RECTANGLES = false; //Count rectangles instead of circles
// Press CTRL+J to open debugger, paste everything, CTRL+A to select all, CTRL+Enter to run

//Gets every color and prints them into a numbered list
var COLOR_DICT = {
     "RGB,0.898040771484375,0.133331298828125,0.2156829833984375" : "Red",
     "RGB,0.415679931640625,0.850982666015625,0.1568603515625" : "Green",
     "RGB,0,0,1" : "Blue",
     "RGB,1,0.439208984375,0.007843017578125" : "Orange",
     "RGB,1,0.7529449462890625,0.6196136474609375" : "Light_Orange",
     "RGB,1,0.819610595703125,0" : "Yellow",
     "RGB,0.9882354736328125,0.9568634033203125,0.521575927734375" : "Light_Yellow",
     "RGB,0.2196044921875,0.898040771484375,1" : "Light_Blue",
     "RGB,0.63922119140625,0.188232421875,0.5254974365234375" : "Purple",
     "RGB,0.98431396484375,0.5333404541015625,1" : "Purple_Pink",
     "RGB,1,0.5019683837890625,0.615692138671875" : "Light_Pink",
     "RGB,0.415679931640625,0.850982666015625,0.1568603515625" : "Dark_Green",
     "RGB,0.772552490234375,0.98431396484375,0.447052001953125" : "Light_Green",
     "RGB,0,0,0" : "Black",
     "RGB,0.26666259765625,0.26666259765625,0.26666259765625" : "Gray_4",
     "RGB,0.4666595458984375,0.4666595458984375,0.4666595458984375" : "Gray_3",
     "RGB,0.6666717529296875,0.6666717529296875,0.6666717529296875" : "Gray_2",
     "RGB,0.8000030517578125,0.8000030517578125,0.8000030517578125" : "Gray_1",
     "RGB,1,1,1" : "White",
};

var annots = this.getAnnots();
var pages = new Array(this.numPages);
var color_list = {};

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

//Go through each page, then add each annotation into their respective color array
for(var i = 0; i < pages.length; i++) {
    pages[i].forEach(function(annot) {
        if(checkAnnotType(annot) && COLOR_DICT[annot.fillColor.toString()] != null) {
            if(color_list[annot.fillColor.toString()] == null) {
                color_list[annot.fillColor.toString()] = [];
            }
            color_list[annot.fillColor.toString()].push(annot);
        }
    });
}

for each(var color in Object.keys(color_list)) {
    console.println(" ------------- " + COLOR_DICT[color] + " -------------");
    for (var i = 0 ; i < color_list[color].length; i++) {
        console.println((i + 1) + " " + COLOR_DICT[color] + " Page_" + color_list[color][i].page);
    }
}

function checkAnnotType(annot) {
    if(USE_RECTANGLES) {
        return annot.type === "Square";
    }
    return annot.type === "Circle";
}

