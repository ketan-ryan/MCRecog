import pyttsx3

def text_to_speech(cmd):

    print(cmd[:-1])
    speech_engine = pyttsx3.init()
    speech_engine.say(cmd[:-1])
    speech_engine.runAndWait()