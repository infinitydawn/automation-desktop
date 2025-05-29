var USE_RECTANGLES = false; //Count rectangles instead of circles
// Press CTRL+J to open debugger, paste everything, CTRL+A to select all, CTRL+Enter to run

//Count all colored circles and print them into debugger

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

var annots = this.getAnnots();
var color_count = {};

function checkAnnotType(annot) {
    if(USE_RECTANGLES) {
        return annot.type === "Square";
    }
    return annot.type === "Circle";
}

for each(var annot in annots) {
    if(checkAnnotType(annot)) {
        if(color_count[annot.fillColor.toString()] == null) {
            color_count[annot.fillColor.toString()] = 0;             
        }
        color_count[annot.fillColor.toString()]++;
    }
}

console.println("-------------- Counts --------------");
for each(var color in Object.keys(color_count)) {
    console.println(COLOR_DICT[color] + " : " + color_count[color]);
}
