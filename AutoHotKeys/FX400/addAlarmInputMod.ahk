#SingleInstance, Force

global shortSleep := 250
global longSleep := 500

Populate_Tags(var){
   Sleep, shortSleep
   SendInput, {Enter Down}{Enter Up}
   Sleep, longSleep
   Send, %var%
   Sleep, longSleep
   SendInput, {Enter Down}{Enter Up}
   Sleep, longSleep
   Send, %var%
   Sleep, longSleep
   SendInput, {Enter Down}{Enter Up}
   Sleep, shortSleep
   SendInput, {Esc Down}{Esc Up}
   SendInput, {End}
}

Populate_Tags2(var1, var2){
   Sleep, shortSleep
   SendInput, {Enter Down}{Enter Up}
   Sleep, shortSleep
   Send, %var1%
   Sleep, shortSleep
   SendInput, {Enter Down}{Enter Up}
   Sleep, shortSleep
   Send, %var2%
   Sleep, shortSleep
   SendInput, {Enter Down}{Enter Up}
   Sleep, shortSleep
   SendInput, {Esc Down}{Esc Up}{Esc Up}
   SendInput, {End}
}

; xxxxxxxxxx
Input_Mod_Alarm(){
   SendInput, {Shift Down}{F10}{Shift Up}
   Sleep, shortSleep
   SendInput, {Down}
   Sleep, shortSleep
   SendInput, {Enter Down}{Enter Up}
   ; Opened Window "Add Devices"
   Sleep, shortSleep
   SendInput, d d
   Sleep, shortSleep
   SendInput, {Enter Down}{Enter Up}
   Sleep, longSleep * 3
   SendInput, {Esc Down}{Esc Up}
   SendInput, {End}
   return
}

; xxxxxxxxxx
Input_Mod_NonLatch(){
   SendInput, {Shift Down}{F10}{Shift Up}
   Sleep, shortSleep
   SendInput, {Down}
   Sleep, shortSleep
   SendInput, {Enter Down}{Enter Up}
   Sleep, shortSleep
   ; Opened Window "Add Devices"
   SendInput, d d
   Sleep, shortSleep
   SendInput, {Tab Down}{Tab Up}{Tab Down}{Tab Up} n
   Sleep, longSleep
   SendInput, {Enter Down}{Enter Up}
   Sleep, longSleep
   SendInput, {Esc Down} {Esc Down} {Esc Up} {Esc Down}{Esc Up}
   Sleep, longSleep * 20
   SendInput, {End}

   return
}

Input_Mod_Latched(){
   SendInput, {Shift Down}{F10}{Shift Up}
   Sleep, shortSleep
   SendInput, {Down}
   Sleep, shortSleep
   SendInput, {Enter Down}{Enter Up}
   Sleep, shortSleep
   ; Opened Window "Add Devices"
   SendInput, d d
   Sleep, shortSleep
   SendInput, {Tab Down}{Tab Up}{Tab Down}{Tab Up} l
   Sleep, shortSleep
   SendInput, {Enter Down}{Enter Up}
   Sleep, longSleep
   SendInput, {Esc Down}{Esc Up}
   SendInput, {End}
   return
}

relay(){
   SendInput, {Shift Down}{F10}{Shift Up}
   Sleep, shortSleep
   SendInput, {Down}
   Sleep, shortSleep
   SendInput, {Enter Down}{Enter Up}
   Sleep, shortSleep
   ; Opened Window "Add Devices"
   SendInput, d
   Sleep, shortSleep
   SendInput, {Enter Down}{Enter Up}
   Sleep, longSleep * 3
   SendInput, {Esc Down}{Esc Up}
   SendInput, {End}
   return
}

Unknown_Device(){
   SendInput, {Shift Down}{F10}{Shift Up}
   Sleep, shortSleep
   SendInput, {Down}
   Sleep, shortSleep
   SendInput, {Enter Down}{Enter Up}
   Sleep, shortSleep
   ; Opened Window "Add Devices"
   SendInput, d d
   Sleep, shortSleep
   SendInput, {Tab Down}{Tab Up}{Tab Down}{Tab Up} b b
   Sleep, shortSleep
   SendInput, {Enter Down}{Enter Up}
   Sleep, longSleep
   SendInput, {Esc Down}{Esc Up}
   SendInput, {End}
   return
}

Unknown_Device_Duals(){

   SendInput, {Shift Down}{F10}{Shift Up}
   Sleep, shortSleep
   SendInput, {Down}
   Sleep, shortSleep
   SendInput, {Enter Down}{Enter Up}
   Sleep, shortSleep
   ; Opened Window "Add Devices"
   SendInput, d d
   Sleep, shortSleep
   SendInput, {Tab Down}{Tab Up}{Tab Down}{Tab Up} b b
   Sleep, shortSleep
   SendInput, {Enter Down}{Enter Up}
   Sleep, longSleep
   SendInput, {Esc Down}{Esc Up}
   SendInput, {End}
   return
}

smoke_detector(){ 
   SendInput, {Shift Down}{F10}{Shift Up}
   Sleep, shortSleep
   SendInput, {Down}
   Sleep, shortSleep
   ; Opened Window "Add Devices" and add a smoke 
   SendInput, {Enter Down}{Enter Up}{Enter Down}{Enter Up}
   Sleep, longSleep * 3
   SendInput, {Esc Down}{Esc Up}
   SendInput, {End}
   return
}

heat_detector(){
   SendInput, {Shift Down}{F10}{Shift Up}
   Sleep, shortSleep
   SendInput, {Down}
   Sleep, shortSleep
   ; Opened Window "Add Devices"
   SendInput, {Enter Down}{Enter Up} h h h {Enter Down}{Enter Up}
   Sleep, longSleep*2
   SendInput, {Esc Down}{Esc Up}
   SendInput, {End}
   return
}

smoke_detector_blank(){
   SendInput, {Shift Down}{F10}{Shift Up}
   Sleep, shortSleep
   SendInput, {Down}
   Sleep, shortSleep
   ; Opened Window "Add Devices" and add a smoke 
   SendInput, {Enter Down}{Enter Up}{Tab Down}{Tab Up} t t {Enter Down}{Enter Up}
   Sleep, longSleep * 3
   SendInput, {Esc Down}{Esc Up}
   SendInput, {End}
   return
}
