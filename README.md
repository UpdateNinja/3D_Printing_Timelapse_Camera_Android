This is android app for creating timelapse videos of 3d printing.
Currently app is optimized for Prusa MK4 printer.
Prusa MK4 has to be in wifi connection, app connects to wifi and reads printer status from API.

You have to add this lines inside slicer 

After layer change:
;AFTER_LAYER_CHANGE
G1 X5 Y205 F{travel_speed*60} ;Move away from the print
G4 S0 ;Wait for move to finish
M221 S0
G4 P6000 ;Wait for 6000ms
M221 S100
G4 P2000 ;Wait for 2000ms
;[layer_z]

Connect printer using printer IP and Api key(password used in prusaprint website).
<img src="https://github.com/UpdateNinja/3D_Printing_Timelapse_Camera_Android/assets/142933009/c3ca8159-ffda-4a51-ba9c-99c067a3ab23" width="400">

App takes picture everytime printer is parked.
<img src="https://github.com/UpdateNinja/3D_Printing_Timelapse_Camera_Android/assets/142933009/3dbaaa3a-991c-46e5-8647-27332585a1a6" width="400">
