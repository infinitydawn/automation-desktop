shortSleep := 50

~RButton::
    while GetKeyState("RButton","P") {
        SendInput, {Delete Down}{Delete Up}
        Sleep, shortSleep
        SendInput, {Left Down}{Left Up}
        Sleep, shortSleep
        SendInput, {Enter Down}{Enter Up}
    }
    return