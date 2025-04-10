from pynput.keyboard import Controller, Key
import time

keyboard = Controller()

time.sleep(3)
# Type a string
keyboard.type("Hello, world!")

# Press and release a special key (e.g., Enter)
keyboard.press(Key.enter)
keyboard.release(Key.enter)

# Simulate pressing and releasing shift + a
with keyboard.pressed(Key.shift):
    keyboard.press('a')
    keyboard.release('a')

# Type with a delay between characters
for char in "Typing slowly":
    keyboard.type(char)
    time.sleep(1)