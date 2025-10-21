#SingleInstance force
SendMode Input
SetWorkingDir %A_ScriptDir%

; text inserted by Alt+S
InsertText := "@sherlocksecuritysystems.com"  ; change as needed

; threshold for detecting two Alt+A presses (milliseconds)
DoublePressThreshold := 800
lastA := 0

!s::
    SendInput %InsertText%
return

!q::
    Send ,{Text}104
    SendInput, {Tab}
    Send ,{Text}deb4213
    SendInput, {Tab}
    SendInput, {Enter}
return


