#Persistent
; Used to delete dialer udact groups
; needs to run as admin using hotkey v1
; usage: select first line, hold Ctrl and left click over it
~Ctrl & LButton::
    SetTimer, SendDelEnter, 10
    return

~LButton Up::
~Ctrl Up::
    SetTimer, SendDelEnter, Off
    return

SendDelEnter:
    if GetKeyState("Ctrl", "P") && GetKeyState("LButton", "P")
    {
        Send, {Del}
        Sleep, 30
        Send, {Enter}
        Sleep, 30
    }
    else
    {
        SetTimer, SendDelEnter, Off
    }
    return