; TODO:
; 3) Path Input On Start For readexcel
; 4) skipping blanks instead of adding zone
; 6) Dry System Stuff

#Include commands.ahk

#Persistent
Msgbox, Before starting make sure the columns are in this order: tag 1, tag 2, Type
;Read the array from the temp file once at the start of the script
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

clipsFinished := False

^j:: ; Ctrl+J
    ; Loop through each numbersArray[index] in the array
    ; MsgBox, % numbersArray.MaxIndex()
    for index in numbersArray
    {
        ; MsgBox, T % tag1Array[index] tag2Array[index]
        ; if(tag1Array[index]>99 && !clipsFinished)
        ; {
        ;     blankCount := 0
        ;     clipsFinished := true
        ; }

        ; Use RegExMatch to find "pull" or "water" in any case (i flag for case-insensitive)
        if (RegExMatch(tag1Array[index], "i)pull") or RegExMatch(tag1Array[index], "i)waterf") or RegExMatch(tag1Array[index], "i)ps10") or RegExMatch(tag1Array[index], "i)ps 10"))
        {
            if(RegExMatch(tag1Array[index], "i)waterf") or RegExMatch(tag1Array[index], "i)ps10") or RegExMatch(tag1Array[index], "i)ps 10")){
                add_AP_Dual_WFVT() ;
                ; MsgBox, Waterflows
                Populate_Tags_Dual_Monitor_AP(tag2Array[index])
            } else {

                add_AP_Mini_Alarm() ;
                Populate_Tags2(tag1Array[index], tag2Array[index]) ; 

            }
            ; MsgBox, T 1

            ; add_CLIP_Alarm()
            ; MsgBox, T after clip alarm
            ; Populate_Tags2(tag1Array[index], tag2Array[index])
            ; MsgBox, T after populate tags
            array.RemoveAt(index)
        } else if (RegExMatch(tag1Array[index], "i)carbon combo")) {
            add_Carbon_Photo_Combo()
            Populate_Tags_Carbon_Photo_Combo(tag2Array[index])
            array.RemoveAt(index)
        }
        else if (RegExMatch(tag1Array[index], "i)co ") or RegExMatch(tag1Array[index], "i)carb"))
        {
            ; MsgBox, 2
            add_AP_Latch()
            Populate_Tags2(tag1Array[index], tag2Array[index])
            array.RemoveAt(index)
        }
        else if (RegExMatch(tag1Array[index], "i)valve") or RegExMatch(tag1Array[index], "i)pump") or RegExMatch(tag1Array[index], "i)tamper") or RegExMatch(tag1Array[index], "i)stat") or RegExMatch(tag2Array[index], "i)stat"))
        {
            ; MsgBox, T 3

            add_AP_NonLatch() ;

            Populate_Tags2(tag1Array[index], tag2Array[index])
            array.RemoveAt(index)
        }
        else if (RegExMatch(tag1Array[index], "i)smoke") or RegExMatch(tag1Array[index], "i)duct"))
        {
            ; MsgBox, T 4

            if(!clipsFinished){
                clipsFinished := true
            }

            if(RegExMatch(tag1Array[index], "i)sb") or RegExMatch(tag1Array[index], "i)sounder")){
                add_S_SB_Photo()
                Populate_Tags_Slow(tag1Array[index], tag2Array[index])
                SendInput, {Up}
                array.RemoveAt(index)
            } else {
                add_S_Photo()
                Populate_Tags2(tag1Array[index], tag2Array[index])
                array.RemoveAt(index)
            }
        }
        else if (RegExMatch(tag1Array[index], "i)heat") && RegExMatch(tag1Array[index], "i)dual"))
        {
            ; MsgBox, T 5.0
            add_S_Dual_Heat_Photo()
            Populate_Tags_Dual_Heat(tag2Array[index])
            array.RemoveAt(index)
        }
        else if (RegExMatch(tag1Array[index], "i)heat"))
        {
            ; MsgBox, T 5.1
            add_S_Heat()
            Sleep, 400
            Populate_Tags2(tag1Array[index], tag2Array[index])
            array.RemoveAt(index)
        }
        else if (RegExMatch(tag1Array[index], "i)relay") or RegExMatch(tag1Array[index], "i)speaker") or RegExMatch(tag1Array[index], "i)door") or (RegExMatch(tag1Array[index], "i)damper") && !(RegExMatch(tag1Array[index], "i)stat") or RegExMatch(tag1Array[index], "i)stat"))) or RegExMatch(tag1Array[index], "i)shutdown") or RegExMatch(tag1Array[index], "i)Recall") or RegExMatch(tag1Array[index], "i)fsd") or (RegExMatch(tag1Array[index], "i)fan") && !(RegExMatch(tag1Array[index], "i)stat") or RegExMatch(tag1Array[index], "i)stat")))){
            ; MsgBox, T 6
            add_AP_Relay()
            Populate_Tags_Slow(tag1Array[index], tag2Array[index])
            array.RemoveAt(index)
        }
        else if (RegExMatch(tag1Array[index], "i)blank"))
        {
            if(RegExMatch(tag2Array[index], "i)smoke")){
                smoke_detector_blank()
                array.RemoveAt(index)
                ; MsgBox, T 7
            }
            if(RegExMatch(tag2Array[index], "i)Duals/clip")){
                unknown_device()
                array.RemoveAt(index)
                ; MsgBox, 8
            }

        }
        else {
            ; MsgBox, T last else

            if(clipsFinished){
                ; MsgBox, blank SM
                smoke_detector_blank()
                Populate_Tags2(tag1Array[index], tag2Array[index])
                array.RemoveAt(index)
                ; MsgBox, T 9
            }
            else {
                unknown_device()
                Populate_Tags2(tag1Array[index], tag2Array[index])
                array.RemoveAt(index)
                ; MsgBox, T 10

            }

        }

    }
    MsgBox, % "Finished entering " . NumbersArray.MaxIndex() . " zones"
return

^w::
    add_AP_Dual_WFVT() ;
    Populate_Tags_Dual_Monitor_AP("TAG 2")
    add_AP_Mini_Alarm() ;
    Populate_Tags2("Test 1","Test 2") ;
return
^n::
    add_CLIP_NonLatch()
return
^p::
    add_S_SB_Photo()
    ; SendInput, {End Down}{End Up}
    ; Sleep, 7500
    ; SendInput, {Up}
    ; Sleep, 1000
    ; Populate_Tags_NoScroll("123", "456")
return

^4::
    SendInput, red
return
^3::
    add_S_Photo()
return

^l::
    add_CLIP_Latch()
return

^h::
    add_S_Heat()
return

^o::
    add_CLIP_SupvOutput()
return

^d::
    add_S_Dual_Heat_Photo()
    Populate_Tags_Dual_Heat("tag2")
return

^t::
    add_AP_Dual_WFVT()
return

^k::
    Populate_Tags_Slow("1","2")
return