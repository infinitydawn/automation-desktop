#SingleInstance, Force

Populate_Tags(var){
   shortSleep := 50
   longSleep := 250
   SendInput, {End Down}{End Up}
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
}


Populate_Tags2(var1, var2){
   shortSleep := 50
   longSleep := 250
   SendInput, {End Down}{End Up}
   Sleep, longSleep
   SendInput, {End Down}{End Up}
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
}



Input_Mod_Alarm(){
   shortSleep := 50
   longSleep := 250
   SendInput, {Shift Down}{F10}{Shift Up}
   Sleep, shortSleep
   SendInput, {Down}
   Sleep, shortSleep
   SendInput, {Enter Down}{Enter Up}
   Sleep, shortSleep
   SendInput, i
   Sleep, shortSleep
   SendInput, {Tab Down}{Tab Up}
   Sleep, shortSleep
   SendInput, f
   Sleep, shortSleep
   SendInput, f
   Sleep, shortSleep
   SendInput, a a a a a a a
   SendInput, {Enter Down}{Enter Up}
   Sleep, longSleep
   SendInput, {Esc Down}{Esc Up}
   return
}

Input_Mod_NonLatch(){
   shortSleep := 50
   longSleep := 250
   SendInput, {Shift Down}{F10}{Shift Up}
   Sleep, shortSleep
   SendInput, {Down}
   Sleep, shortSleep
   SendInput, {Enter Down}{Enter Up}
   Sleep, shortSleep
   SendInput, i
   Sleep, shortSleep
   SendInput, {Tab Down}{Tab Up}
   Sleep, shortSleep
   SendInput, f
   Sleep, shortSleep
   SendInput, n n
   SendInput, {Enter Down}{Enter Up}
   Sleep, longSleep
   SendInput, {Esc Down}{Esc Up}
   return
}


Input_Mod_Latched(){
   shortSleep := 50
   longSleep := 250
   SendInput, {Shift Down}{F10}{Shift Up}
   Sleep, shortSleep
   SendInput, {Down}
   Sleep, shortSleep
   SendInput, {Enter Down}{Enter Up}
   Sleep, shortSleep
   SendInput, i
   Sleep, shortSleep
   SendInput, {Tab Down}{Tab Up}
   Sleep, shortSleep
   SendInput, f
   Sleep, shortSleep
   SendInput, l
   SendInput, {Enter Down}{Enter Up}
   Sleep, longSleep
   SendInput, {Esc Down}{Esc Up}
   return
}


relay(){
   shortSleep := 50
   longSleep := 250
   SendInput, {Shift Down}{F10}{Shift Up}
   Sleep, shortSleep
   SendInput, {Down}
   Sleep, shortSleep
   SendInput, {Enter Down}{Enter Up}
   Sleep, shortSleep
   SendInput, r
   Sleep, shortSleep
   SendInput, {Enter Down}{Enter Up}
   Sleep, longSleep
   SendInput, {Esc Down}{Esc Up}
   return
}

Unknown_Device(){
   shortSleep := 50
   longSleep := 250
   SendInput, {Shift Down}{F10}{Shift Up}
   Sleep, shortSleep
   SendInput, {Down}
   Sleep, shortSleep
   SendInput, {Enter Down}{Enter Up}
   Sleep, shortSleep
   SendInput, i
   Sleep, shortSleep
   SendInput, {Tab Down}{Tab Up}
   Sleep, shortSleep
   SendInput, f
   Sleep, shortSleep
   SendInput, b
   SendInput, {Enter Down}{Enter Up}
   Sleep, longSleep
   SendInput, {Esc Down}{Esc Up}
   return
}

Unknown_Device_Duals(){
   shortSleep := 50
   longSleep := 250
   SendInput, {Shift Down}{F10}{Shift Up}
   Sleep, shortSleep
   SendInput, {Down}
   Sleep, shortSleep
   SendInput, {Enter Down}{Enter Up}
   Sleep, shortSleep
   SendInput, i
   Sleep, shortSleep
   SendInput, {Tab Down}{Tab Up}
   Sleep, shortSleep
   SendInput, f
   Sleep, shortSleep
   SendInput, b
   SendInput, {Enter Down}{Enter Up}
   Sleep, longSleep
   SendInput, {Esc Down}{Esc Up}
   return
}

smoke_detector(){
   shortSleep := 50
   longSleep := 250
   SendInput, {Shift Down}{F10}{Shift Up}
   Sleep, shortSleep
   SendInput, {Down}
   Sleep, shortSleep
   SendInput, {Enter Down}{Enter Up}
   Sleep, shortSleep
   SendInput, p
   Sleep, shortSleep
   SendInput, {Enter Down}{Enter Up}
   Sleep, longSleep
   SendInput, {Esc Down}{Esc Up}
   return
}

heat_detector(){
   shortSleep := 50
   longSleep := 250
   SendInput, {Shift Down}{F10}{Shift Up}
   Sleep, shortSleep
   SendInput, {Down}
   Sleep, shortSleep
   SendInput, {Enter Down}{Enter Up}
   Sleep, shortSleep
   SendInput, h
   Sleep, shortSleep
   SendInput, {Enter Down}{Enter Up}
   Sleep, longSleep
   SendInput, {Esc Down}{Esc Up}
   return
}

smoke_detector_blank(){
   shortSleep := 50
   longSleep := 250
   SendInput, {Shift Down}{F10}{Shift Up}
   Sleep, shortSleep
   SendInput, {Down}
   Sleep, shortSleep
   SendInput, {Enter Down}{Enter Up}
   Sleep, shortSleep
   SendInput, p
   Sleep, shortSleep
   SendInput, {Tab Down}{Tab Up}
   Sleep, shortSleep
   SendInput, t
   SendInput, {Tab Down}{Tab Up}
   SendInput, {Enter Down}{Enter Up}
   Sleep, longSleep
   SendInput, {Esc Down}{Esc Up}
   return
}
