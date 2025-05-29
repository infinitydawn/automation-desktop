
var USED_COLOR = "Red"; // All comments will become this color
var INCLUDE_BORDER = true; //Recolor borders as well
var USE_RECTANGLES = false; //Recolor rectangles instead of circles

//Sets all circles to a specified color

var COLOR_DICT = {
     "Red": ["RGB",0.898040771484375,0.133331298828125,0.2156829833984375],
     "Green" : ["RGB",0.415679931640625,0.850982666015625,0.1568603515625],
     "Blue" : ["RGB",0,0,1],
     "Orange" : ["RGB",1,0.439208984375,0.007843017578125],
     "Light Orange" : ["RGB",1,0.7529449462890625,0.6196136474609375],
     "Yellow" : ["RGB",1,0.819610595703125,0],
     "Light Yellow" : ["RGB",0.9882354736328125,0.9568634033203125,0.521575927734375],
     "Light Blue" : ["RGB",0.2196044921875,0.898040771484375,1],
     "Purple" : ["RGB",0.63922119140625,0.188232421875,0.5254974365234375],
     "Purple Pink" : ["RGB",0.98431396484375,0.5333404541015625,1],
     "Light Pink" : ["RGB",1,0.5019683837890625,0.615692138671875],
     "Dark Green" : ["RGB",0.415679931640625,0.850982666015625,0.1568603515625],
     "Light Green" : ["RGB",0.772552490234375,0.98431396484375,0.447052001953125],
     "Black" : ["RGB",0,0,0],
     "Gray 4" : ["RGB",0.26666259765625,0.26666259765625,0.26666259765625],
     "Gray 3" : ["RGB",0.4666595458984375,0.4666595458984375,0.4666595458984375],
     "Gray 2" : ["RGB",0.6666717529296875,0.6666717529296875,0.6666717529296875],
     "Gray 1" : ["RGB",0.8000030517578125,0.8000030517578125,0.8000030517578125],
      "White" : ["RGB",1,1,1],
};

var annots = this.getAnnots();

for each(var annot in annots) {
    if(checkAnnotType(annot)) {
        annot.fillColor = COLOR_DICT[USED_COLOR];
        if (INCLUDE_BORDER) {
            annot.strokeColor = COLOR_DICT[USED_COLOR]; 
        }
    }
}

function checkAnnotType(annot) {
    if(USE_RECTANGLES) {
        return annot.type === "Square";
    }
    return annot.type === "Circle";
}
