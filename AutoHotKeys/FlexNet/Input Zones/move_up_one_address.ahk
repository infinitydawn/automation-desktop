#Persistent
shortSleep := 50
longSleep := 500

^j:: 
    Loop 227
    {

        ; SendInput, {Down}
        ; Sleep, longSleep
        ; Sleep, longSleep
        ; Sleep, longSleep
        ; SendInput, {Down}
        ; Sleep, longSleep
        ; Sleep, longSleep
        ; Sleep, longSleep

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

        ; SendInput, {Right}
        ; Sleep, longSleep
        ; SendInput, {Right}
        ; Sleep, longSleep
        ; SendInput, {Right}
        ; Sleep, longSleep
        ; SendInput, {Right}
        ; Sleep, longSleep
        ; SendInput, {Right}
        ; Sleep, longSleep
        ; SendInput, {Right}
        ; Sleep, longSleep

        ; SendInput, {Enter Down}{Enter Up}
        ; Sleep, longSleep
        ; SendInput, 5
        ; Sleep, longSleep
        ; SendInput, {Enter Down}{Enter Up}
        ; Sleep, longSleep

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

^m::
    ; Loop 1
    ; {
    ;     SendInput, {Enter}
    ;     SendInput, {Up Down}{Up Up}
    ;     SendInput, {Enter}
    ;     SendInput, {Esc Down}{Esc Up}{Esc Up}
    ;     SendInput, {Down Down}{Down Up}
    ; }
    Loop 1
    {
        SendInput, {Enter}
        SendInput, {Home Down}{Home Up}
        SendInput, {Enter}
        SendInput, {Esc Down}{Esc Up}{Esc Up}
        SendInput, {Down Down}{Down Up}
    }
return

^.::
    Loop 20
    {
        SendInput, {Enter}
        SendInput, {Down Down}{Down Up}
        SendInput, {Enter}
        SendInput, {Esc Down}{Esc Up}{Esc Up}
        SendInput, {Up Down}{Up Up}
    }
return

