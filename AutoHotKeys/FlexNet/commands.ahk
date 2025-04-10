#SingleInstance, Force

global shortSleep := 50 * 1.2 ; * 4
global longSleep := 250 * 1.2 ; * 2.2

Populate_Tags(var){
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
   SendInput, {End Down}{End Up}
   Sleep, longSleep
   SendInput, {End Down}{End Up}
   Sleep, longSleep
   SendInput, {Enter Down}{Enter Up}
   Sleep, longSleep
   Send, %var1%
   Sleep, longSleep
   SendInput, {Enter Down}{Enter Up}
   Sleep, longSleep
   Send, %var2%
   Sleep, longSleep
   SendInput, {Enter Down}{Enter Up}
   Sleep, longSleep
   SendInput, {Esc Down}{Esc Up}{Esc Up}
}

Populate_Tags_Dual_Heat(tag2){
   ; 135 Heat
   SendInput, {End Down}{End Up}
   Sleep, longSleep
   SendInput, {End Down}{End Up}
   Sleep, longSleep
   SendInput, {Enter Down}{Enter Up}
   Sleep, longSleep
   Send, 135° Heat Detector
   Sleep, longSleep
   SendInput, {Enter Down}{Enter Up}
   Sleep, longSleep
   Send, %tag2%
   Sleep, longSleep
   SendInput, {Enter Down}{Enter Up}
   Sleep, longSleep
   SendInput, {Esc Down}{Esc Up}{Esc Up}
   Sleep, longSleep

   ; do the low heat
   SendInput, {Up Up}{Up Down}
   Sleep, longSleep
   SendInput, {Enter Down}{Enter Up}
   Sleep, longSleep
   Send, Low Heat Detector
   Sleep, longSleep
   SendInput, {Enter Down}{Enter Up}
   Sleep, longSleep
   Send, %tag2%
   Sleep, longSleep
   SendInput, {Enter Down}{Enter Up}
   Sleep, longSleep
   SendInput, n n
   Sleep, longSleep
   SendInput, {Enter Down}{Enter Up}
   Sleep, longSleep * 2
   SendInput, {Esc Down}{Esc Up}{Esc Up}
   Sleep, longSleep

   ; do the smoke
   SendInput, {Up Up}{Up Down}
   Sleep, longSleep
   SendInput, {Enter Down}{Enter Up}
   Sleep, longSleep
   Send, Smoke Detector
   Sleep, longSleep
   SendInput, {Enter Down}{Enter Up}
   Sleep, longSleep
   Send, %tag2%
   Sleep, longSleep
   SendInput, {Enter Down}{Enter Up}
   Sleep, longSleep
   SendInput, {Esc Down}{Esc Up}{Esc Up}
   Sleep, longSleep
}


Populate_Tags_Dual_Monitor_AP(tag2){
   ; Valve Tamper
   SendInput, {End Down}{End Up}
   Sleep, longSleep
   SendInput, {End Down}{End Up}
   Sleep, longSleep
   SendInput, {Enter Down}{Enter Up}
   Sleep, longSleep
   Send, Valve Tamper
   Sleep, longSleep
   SendInput, {Enter Down}{Enter Up}
   Sleep, longSleep
   Send, %tag2%
   Sleep, longSleep
   SendInput, {Enter Down}{Enter Up}
   Sleep, longSleep
   SendInput, n n
   Sleep, longSleep
   SendInput, {Enter Down}{Enter Up}
   Sleep, longSleep * 2
   SendInput, {Esc Down}{Esc Up}{Esc Up}
   Sleep, longSleep

   ; Waterflow
   SendInput, {Up Up}{Up Down}
   Sleep, longSleep * 3
   SendInput, {Enter Down}{Enter Up}
   Sleep, longSleep
   Send, Waterflow
   Sleep, longSleep
   SendInput, {Enter Down}{Enter Up}
   Sleep, longSleep * 2
   Send, %tag2%
   Sleep, longSleep
   SendInput, {Enter Down}{Enter Up}
   Sleep, longSleep
   SendInput, {Enter Down}{Enter Up}
   Sleep, longSleep
   SendInput, n n
   Sleep, longSleep
   SendInput, {Enter Down}{Enter Up}
   Sleep, longSleep * 2
   SendInput, {Esc Down}{Esc Up}{Esc Up}
   Sleep, longSleep * 3
}

Populate_Tags_NoScroll(var1, var2){
   ;SendInput, {End Down}{End Up}
   ; Sleep, longSleep
   ; SendInput, {End Down}{End Up}
   ; Sleep, shortSleep
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



add_CLIP_Alarm(){
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
   SendInput, {Tab Down}{Tab Up}
   Sleep, shortSleep
   SendInput, n
   Sleep, shortSleep
   SendInput, {Enter Down}{Enter Up}
   Sleep, longSleep * 3

   SendInput, {Esc Down}{Esc Up}
   return
}


add_AP_Mini_Alarm(){
   SendInput, {Shift Down}{F10}{Shift Up}
   Sleep, shortSleep
   SendInput, {Down}
   Sleep, shortSleep
   SendInput, {Enter Down}{Enter Up}
   Sleep, shortSleep
   SendInput, l m m 
   Sleep, shortSleep
   SendInput, {Tab Down}{Tab Up}
   Sleep, shortSleep
   SendInput, {Tab Down}{Tab Up}
   Sleep, shortSleep
   SendInput, n
   Sleep, shortSleep
   SendInput, {Enter Down}{Enter Up}
   Sleep, longSleep
   SendInput, {Esc Down}{Esc Up}
   Sleep, longSleep * 3
   return
}

add_AP_Dual_WFVT(){
   SendInput, {Shift Down}{F10}{Shift Up}{Down}{Enter Down}{Enter Up}
   Sleep, shortSleep * 5
   SendInput, f f d
   Sleep, shortSleep
   SendInput, {Tab Down}{Tab Up}{Tab Down}{Tab Up}
   Sleep, shortSleep
   SendInput, n
   Sleep, shortSleep
   SendInput, {Enter Down}{Enter Up}
   Sleep, longSleep * 3

   SendInput, {Esc Down}{Esc Up}
   return
}

add_Carbon_Photo_Combo(){
   SendInput, {Shift Down}{F10}{Shift Up}
   Sleep, shortSleep
   SendInput, {Down}
   Sleep, shortSleep
   SendInput, {Enter Down}{Enter Up}
   Sleep, shortSleep
   SendInput, z f
   Sleep, shortSleep
   SendInput, {Tab Down}{Tab Up}
   SendInput, {Tab Down}{Tab Up}
   SendInput, {Tab Down}{Tab Up}
   Sleep, shortSleep
   SendInput, n
   Sleep, shortSleep
   SendInput, {Enter Down}{Enter Up}
   Sleep, longSleep * 5
   SendInput, {Esc Down}{Esc Up}
   return
}

Populate_Tags_Carbon_Photo_Combo(tag2) {
   ; CO Detector
   SendInput, {End Down}{End Up}
   Sleep, longSleep
   SendInput, {End Down}{End Up}
   Sleep, longSleep
   SendInput, {Enter Down}{Enter Up}
   Sleep, longSleep
   Send, CO Detector
   Sleep, longSleep
   SendInput, {Enter Down}{Enter Up}
   Sleep, longSleep
   Send, %tag2%
   Sleep, longSleep
   SendInput, {Enter Down}{Enter Up}
   Sleep, longSleep * 2
   SendInput, {Esc Down}{Esc Up}{Esc Up}
   Sleep, longSleep

   ; Smoke Detector
   SendInput, {Up Up}{Up Down}
   Sleep, longSleep * 3
   SendInput, {Enter Down}{Enter Up}
   Sleep, longSleep
   Send, Smoke Detector
   Sleep, longSleep
   SendInput, {Enter Down}{Enter Up}
   Sleep, longSleep * 2
   Send, %tag2%
   Sleep, longSleep
   SendInput, {Enter Down}{Enter Up}
   Sleep, longSleep * 2
   SendInput, {Esc Down}{Esc Up}{Esc Up}
   Sleep, longSleep * 3
}

add_CLIP_NonLatch(){
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
   SendInput, {Tab Down}{Tab Up}
   Sleep, shortSleep
   SendInput, n
   Sleep, shortSleep
   SendInput, {Enter Down}{Enter Up}
   Sleep, longSleep * 3
   SendInput, {Esc Down}{Esc Up}
   return
}

add_AP_NonLatch(){
   SendInput, {Shift Down}{F10}{Shift Up}
   Sleep, shortSleep
   SendInput, {Down}
   Sleep, shortSleep
   SendInput, {Enter Down}{Enter Up}
   Sleep, shortSleep
   SendInput, l m m
   Sleep, shortSleep
   SendInput, {Tab Down}{Tab Up}
   Sleep, shortSleep
   SendInput, n {Tab Down}{Tab Up} n
   Sleep, shortSleep
   SendInput, {Enter Down}{Enter Up}
   Sleep, longSleep * 3
   SendInput, {Esc Down}{Esc Up}
   return
}

add_AP_Latch(){
   SendInput, {Shift Down}{F10}{Shift Up}
   Sleep, shortSleep
   SendInput, {Down}
   Sleep, shortSleep
   SendInput, {Enter Down}{Enter Up}
   Sleep, shortSleep
   SendInput, l m m
   Sleep, shortSleep
   SendInput, {Tab Down}{Tab Up}
   Sleep, shortSleep
   SendInput, l {Tab Down}{Tab Up} n
   Sleep, shortSleep
   SendInput, {Enter Down}{Enter Up}
   Sleep, longSleep * 3
   SendInput, {Esc Down}{Esc Up}
   return
}



add_CLIP_SupvOutput(){
   SendInput, {Shift Down}{F10}{Shift Up}
   Sleep, shortSleep
   SendInput, {Down}
   Sleep, shortSleep
   SendInput, {Enter Down}{Enter Up}
   Sleep, shortSleep
   SendInput, s
   SendInput, {Enter Down}{Enter Up}
   Sleep, longSleep*6
   SendInput, {Esc Down}{Esc Up}
   return
}



add_CLIP_Latch(){
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
   SendInput, {Tab Down}{Tab Up}
   Sleep, shortSleep
   SendInput, n
   Sleep, shortSleep
   SendInput, {Enter Down}{Enter Up}
   Sleep, longSleep * 3
   SendInput, {Esc Down}{Esc Up}
   return
}

add_AP_Relay(){
   Sleep, shortSleep * 2.5
   SendInput, {Shift Down}{F10}{Shift Up}
   Sleep, shortSleep * 2.5
   SendInput, {Down}
   Sleep, shortSleep * 2.5
   SendInput, {Enter Down}{Enter Up}
   Sleep, shortSleep * 2.5
   SendInput, r r
   SendInput, {Enter Down}{Enter Up}
   Sleep, longSleep * 2.5
   SendInput, {Esc Down}{Esc Up}
   Sleep, longSleep * 2.5
   return
}

Populate_Tags_Slow(var1, var2){
   Sleep, longSleep * 3
   SendInput, {End Down}{End Up}
   SendInput, {End Down}{End Up}
   ; Sleep, longSleep * 25
   Sleep, longSleep * 22
   SendInput, {Enter Down}{Enter Up}
   Sleep, longSleep * 5
   Send, %var1%
   Sleep, shortSleep * 5
   SendInput, {Enter Down}{Enter Up}
   Sleep, longSleep * 5
   Send, %var2%
   ; Sleep, shortSleep * 10
   Sleep, shortSleep * 5
   SendInput, {Enter Down}{Enter Up}
   Sleep, shortSleep * 5
   SendInput, {Esc Down}{Esc Up}
   Sleep, longSleep * 5
}

Unknown_Device(){
   SendInput, {Shift Down}{F10}{Shift Up}
   Sleep, shortSleep * 2
   SendInput, {Down}
   Sleep, shortSleep * 2
   SendInput, {Enter Down}{Enter Up}
   Sleep, shortSleep * 2
   SendInput, m m
   Sleep, shortSleep * 2
   SendInput, {Tab Down}{Tab Up}
   Sleep, shortSleep * 2
   SendInput, f
   Sleep, shortSleep * 2
   SendInput, b
   SendInput, {Enter Down}{Enter Up}
   Sleep, longSleep * 6
   SendInput, {Esc Down}{Esc Up}
   return
}

Unknown_Device_Duals(){
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
   Sleep, longSleep * 3 
   SendInput, {Esc Down}{Esc Up}
   return
}

add_S_Photo(){
   SendInput, {Shift Down}{F10}{Shift Up}
   Sleep, shortSleep
   SendInput, {Down}
   Sleep, shortSleep
   SendInput, {Enter Down}{Enter Up}
   Sleep, shortSleep
   SendInput, p
   Sleep, shortSleep
   SendInput, {Tab Down}{Tab Up}
   SendInput, {Tab Down}{Tab Up}
   SendInput, {Tab Down}{Tab Up}
   Sleep, shortSleep
   SendInput, n
   Sleep, shortSleep
   SendInput, {Enter Down}{Enter Up}
   Sleep, longSleep * 5
   SendInput, {Esc Down}{Esc Up}
   return
}

add_S_SB_Photo(){
   SendInput, {Shift Down}{F10}{Shift Up}
   Sleep, shortSleep
   SendInput, {Down}
   Sleep, shortSleep
   SendInput, {Enter Down}{Enter Up}
   Sleep, shortSleep
   SendInput, p
   Sleep, shortSleep
   SendInput, {Tab Down}{Tab Up}
   SendInput, a
   SendInput, {Tab Down}{Tab Up}
   SendInput, {Tab Down}{Tab Up}
   Sleep, shortSleep
   SendInput, n
   Sleep, shortSleep
   SendInput, {Enter Down}{Enter Up}
   Sleep, longSleep * 3
   SendInput, {Esc Down}{Esc Up}
   return
}

add_S_Dual_Heat_Photo(){
   SendInput, {Shift Down}{F10}{Shift Up}
   Sleep, shortSleep
   SendInput, {Down}
   Sleep, shortSleep
   SendInput, {Enter Down}{Enter Up}
   Sleep, shortSleep
   SendInput, d d d d d d
   Sleep, shortSleep
   SendInput, {Tab Down}{Tab Up}
   SendInput, {Tab Down}{Tab Up}
   SendInput, {Tab Down}{Tab Up}
   SendInput, {Tab Down}{Tab Up}
   SendInput, {Tab Down}{Tab Up}
   SendInput, {Tab Down}{Tab Up}
   
   Sleep, shortSleep
   SendInput, {Enter Down}{Enter Up}
   Sleep, longSleep * 6
   SendInput, {Esc Down}{Esc Up}
   return
}

add_S_Heat(){
   SendInput, {Shift Down}{F10}{Shift Up}
   Sleep, shortSleep
   SendInput, {Down}
   SendInput, {Enter Down}{Enter Up}
   SendInput, {ASC 0031}
   Sleep, shortSleep
   SendInput, h
   Sleep, shortSleep
   SendInput, h
   Sleep, shortSleep
   SendInput, {Enter Down}{Enter Up}
   Sleep, longSleep * 3
   SendInput, {Esc Down}{Esc Up}
   return
}

smoke_detector_blank(){
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
   Sleep, longSleep * 3
   SendInput, {Esc Down}{Esc Up}
   return
}
