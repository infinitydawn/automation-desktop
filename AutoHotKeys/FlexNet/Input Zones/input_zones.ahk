#Persistent
shortSleep := 50
longSleep := 500

MsgBox % "Usage: "

^j:: 
    Loop 227
    {
        SendInput, {Enter Down}{Enter Up}
        Sleep, longSleep

        SendInput, {Down}
        Sleep, longSleep
        SendInput, {Down}
        Sleep, longSleep
        SendInput, {Down}
        Sleep, longSleep

        SendInput, {Enter Down}{Enter Up}
        Sleep, longSleep

        SendInput, {Tab}
        Sleep, longSleep
        SendInput, {Tab}
        Sleep, longSleep
        SendInput, {Tab}
        Sleep, longSleep
        SendInput, {Tab}
        Sleep, longSleep
    }
return

^w::
    shortSleep := 50
    shortSleep := 250
    SendInput, {Down}
    Sleep, shortSleep
    SendInput, {Down}
    Sleep, shortSleep

    SendInput, {Right}
    Sleep, shortSleep
    SendInput, {Right}
    Sleep, shortSleep
    SendInput, {Right}
    Sleep, shortSleep
    SendInput, {Right}
    Sleep, shortSleep
    SendInput, {Right}
    Sleep, shortSleep
    SendInput, {Right}
    Sleep, shortSleep
    SendInput, {Right}
    Sleep, shortSleep

    SendInput, {Enter}
    Sleep, shortSleep
    SendInput, 4
    Sleep, shortSleep
    SendInput, {Enter}
    Sleep, shortSleep
return

entryAllowed := false ;
rows := {} ; 

; to get the zone tags
^e::
    Gui, Add, Edit, x10 y10 h350 w180 vEntry

    Gui, Add, Button, x140 y370 w40 h20 , OK

    Gui, Show, x806 y201 h400 w200, New GUI Window

Return

ButtonOK:

    GuiControlGet, Entry 

    msgbox, %Entry%

    rows := StrSplit(Entry,"`n","`r")
    rowsCount := rows.MaxIndex()

    MsgBox % "There are " rowsCount " rows to be entered"
    MsgBox % "Move tag column all the way to the left, click on the first zone, and press ctrl+t"
Return


enterZones(rows){
    For index, row in rows {
            ; MsgBox % row
            SendInput, {Enter}
            SendInput, %row%
            SendInput, {Enter}
            SendInput, {Escape}
            SendInput, {Down}
        }

        MsgBox, Finished Entering
    return
}

; to enter the tags one by one 
^t::
    if(rows.MaxIndex() > 0){
        enterZones(rows)
    } else {
        MsgBox, Rows Array Is Empty
    }
return


global countEntered := 0 ;
global correlated := 0 ;
global skip := 0 ;

; get how many devices to skip
^m::
    InputBox, skip, Correlations Setup, Enter how many first devices to skip
    InputBox, countEntered, Correlations Setup, How many sounder bases are there?
return


^k::
correlated := 0 ;
    loop %countEntered% {
        ; Open Signals Correlation Window
        Sleep, longSleep
        SendInput, {Shift}+{F10}

        Sleep, longSleep
        SendInput, {Down 4} {Enter} {Tab 3} {Right} {Tab 3} {Down}

        Sleep, longSleep
        ; Skip X Devices
        nextTarget := skip + correlated
        ; MsgBox, %nextTarget%
        SendInput, {Down %nextTarget%} {Tab 2}
        Sleep, longSleep       
        ; Correlate Device And Exit
        SendInput, {Enter} 
        Sleep, longSleep
        SendInput, {Escape}
        correlated := correlated + 1 ;
        Sleep, longSleep
        ; Move On To Next Zone
        SendInput, {Down}
        Sleep, longSleep
    }
return

; Alarms Correlation
^l::
; MsgBox, %countEntered%
; MsgBox, %correlated%
; MsgBox, %skip%
; countEntered := 55 ;
; correlated := 0 ;
; skip := 11 ;
correlated := 0 ;
    loop %countEntered% {
        ; Open Signals Correlation Window
        Sleep, longSleep
        SendInput, {Shift}+{F10}

        Sleep, longSleep
        SendInput, {Down 4} {Enter} {Tab 3} {Tab 3} {Down}

        Sleep, longSleep
        ; Skip X Devices
        nextTarget := skip + correlated
        ; MsgBox, %nextTarget%
        SendInput, {Down %nextTarget%} {Tab 2}
        Sleep, longSleep       
        ; Correlate Device And Exit
        SendInput, {Enter} 
        Sleep, longSleep
        SendInput, {Escape}
        correlated := correlated + 1 ;
        Sleep, longSleep
        ; Move On To Next Zone
        SendInput, {Down}
        Sleep, longSleep
    }
return


; for supv zones
^h::
; MsgBox, %countEntered%
; MsgBox, %correlated%
; MsgBox, %skip%
; countEntered := 55 ;
; correlated := 0 ;
; skip := 11 ;
correlated := 0 ;
    loop %countEntered% {
        ; Open Signals Correlation Window
        Sleep, longSleep
        SendInput, {Shift}+{F10}

        Sleep, longSleep
        SendInput, {Down 4} {Enter} {Tab 3} {Tab 3} {Down}
        
        

        Sleep, longSleep
        ; Skip X Devices
        nextTarget := skip + correlated
        SendInput, {Down %nextTarget%} {Tab 2}
        Sleep, longSleep       
        ; Correlate Device And Exit
        SendInput, {Enter} 
        Sleep, longSleep
        SendInput, {Escape}
        correlated := correlated + 1 ;
        Sleep, longSleep
        ; Move On To Next Zone
        SendInput, {Down}
        Sleep, longSleep
    }
return




; REVERSE for supv zones
^g::
; MsgBox, %countEntered%
; MsgBox, %correlated%
; MsgBox, %skip%
; countEntered := 55 ;
; correlated := 0 ;
; skip := 11 ;
correlated := 0 ;
    loop %countEntered% {
        ; Open Signals Correlation Window
        Sleep, longSleep
        SendInput, {Shift}+{F10}

        Sleep, longSleep
        SendInput, {Down 4} {Enter} {Tab 3} {Tab 3} {Down}
        
        

        Sleep, longSleep
        ; Skip X Devices
        nextTarget := skip-1 + countEntered-1 - correlated
        SendInput, {Down %nextTarget%} {Tab 2}
        Sleep, longSleep       
        ; Correlate Device And Exit
        SendInput, {Enter} 
        Sleep, longSleep
        SendInput, {Escape}
        correlated := correlated + 1 ;
        Sleep, longSleep
        ; Move On To Next Zone
        SendInput, {Down}
        Sleep, longSleep
    }
return




