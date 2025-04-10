; TODO:
; 3) Path Input On Start For readexcel
; 4) skipping blanks instead of adding zone
; 6) Dry System Stuff

#Include addAlarmInputMod.ahk

#Persistent

; Read the array from the temp file once at the start of the script
tempFilePath := "tempArray.txt"
FileRead, arrayString, %tempFilePath%
if ErrorLevel ; Non-zero when the operation failed
{
    MsgBox, Error reading from file.
    return
}
array := StrSplit(arrayString, "|") ; Split the string back into an array

tag1Array := []
tag2Array := []
numbersArray := []
elementCounter := 1

for index, value in array {
    if (elementCounter = 1) {
        numbersArray.Push(array[index])
    } else if (elementCounter = 2) {
        tag1Array.Push(array[index])
    } else if (elementCounter = 3) {
        tag2Array.Push(RegExReplace(array[index], "`r`n"))
    }

    elementCounter += 1
    if (elementCounter > 3) {
        elementCounter := 1
    }
}

numbersString := "" ;

for index in tag1Array{

    numbersString := numbersString . tag1Array[index] . "-" . tag2Array[index]", " ;
}

MsgBox, %numbersString%

reachedDuals := true

^j:: ; Ctrl+J
    ; Loop through each numbersArray[index] in the array
    for index in numbersArray
    {

        ; Example number
        num := numbersArray[index]

        ; Convert number to string
        numberStr := Format("{:.4f}", num)

        ; Extract the decimal part
        StringSplit, parts, numberStr, .

        if(numbersArray[index]>99 && !reachedDuals)
        {
            blankCount := 0
            reachedDuals := true
        }

        ; Use RegExMatch to find "pull" or "water" in any case (i flag for case-insensitive)
        if (RegExMatch(tag1Array[index], "i)pull") or RegExMatch(tag1Array[index], "i)waterf") or RegExMatch(tag1Array[index], "i)ps10") or RegExMatch(tag1Array[index], "i)ps 10"))
        {
            ; Check if the decimal part starts with 2
            if (SubStr(parts2, 1, 1) = "1")
            {
                Input_Mod_Alarm()
                SendInput, {Up}
            }

            Populate_Tags2(tag1Array[index], tag2Array[index])
            array.RemoveAt(index)
        }
        else if (RegExMatch(tag1Array[index], "i)co ") or RegExMatch(tag1Array[index], "i)carb"))
        {
            if (SubStr(parts2, 1, 1) = "1")
            {
                Input_Mod_Latched()
                SendInput, {Up}
            }

            Populate_Tags2(tag1Array[index], tag2Array[index])
            array.RemoveAt(index)
        }
        else if (RegExMatch(tag1Array[index], "i)valve") or RegExMatch(tag1Array[index], "i)pump") or RegExMatch(tag1Array[index], "i)tamper") or RegExMatch(tag1Array[index], "i)stat") or RegExMatch(tag2Array[index], "i)stat"))
        {
            if (SubStr(parts2, 1, 1) = "1")
            {
                Input_Mod_NonLatch()
                SendInput, {Up}
            }

            Populate_Tags2(tag1Array[index], tag2Array[index])
            array.RemoveAt(index)
        }
        else if (RegExMatch(tag1Array[index], "i)smoke") or RegExMatch(tag1Array[index], "i)duct"))
        {

            smoke_detector()
            Populate_Tags2(tag1Array[index], tag2Array[index])
            array.RemoveAt(index)
        }
        else if (RegExMatch(tag1Array[index], "i)heat"))
        {
            heat_detector()
            Populate_Tags2(tag1Array[index], tag2Array[index])
            array.RemoveAt(index)
        }
        else if (RegExMatch(tag1Array[index], "i)relay") or RegExMatch(tag1Array[index], "i)door") or (RegExMatch(tag1Array[index], "i)damper") && !(RegExMatch(tag1Array[index], "i)stat") or RegExMatch(tag1Array[index], "i)stat"))) or RegExMatch(tag1Array[index], "i)shutdown") or RegExMatch(tag1Array[index], "i)Recall") or RegExMatch(tag1Array[index], "i)fsd") or (RegExMatch(tag1Array[index], "i)fan") && !(RegExMatch(tag1Array[index], "i)stat") or RegExMatch(tag1Array[index], "i)stat")))){

            if (SubStr(parts2, 1, 1) = "1")
            {
                relay()
                SendInput, {Up}
            }

            Populate_Tags2(tag1Array[index], tag2Array[index])
            array.RemoveAt(index)
        }
        else if (RegExMatch(tag1Array[index], "i)blank"))
        {
            if(RegExMatch(tag2Array[index], "i)smoke")){
                smoke_detector_blank()
                array.RemoveAt(index)
            }
            if(RegExMatch(tag2Array[index], "i)Duals/clip")){
                if (SubStr(parts2, 1, 1) = "1")
                {
                    unknown_device()
                    SendInput, {Up}
                }
                
                array.RemoveAt(index)
            }

        }
        else {

            if(reachedDuals){
                if (SubStr(parts2, 1, 1) = "1")
                {
                    unknown_device()
                    SendInput, {Up}
                }
                Populate_Tags2(tag1Array[index], tag2Array[index])
                array.RemoveAt(index)
            }
            else {
                smoke_detector_blank()
                Populate_Tags2(tag1Array[index], tag2Array[index])
                array.RemoveAt(index)

            }

        }

    }
return

^w::
    Input_Mod_Alarm()
return
^n::
    Input_Mod_NonLatch()
return
^p::
    smoke_detector()
return

^4::
    Populate_Tags2("test1", "test2")
return
^h::
    heat_detector()
return
^m::
    smoke_detector_blank()
return
^r::
    SendInput, {End} {Up}
return

