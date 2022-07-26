import speech_recognition

import mc_socket
from mc_socket import MCSocket
import speech_recognition as sr


"""
https://github.com/ketan-ryan/MCRecog/wiki
"""

r = sr.Recognizer()
r.energy_threshold = 300
mic = sr.Microphone()
print(mic.__dict__)

mc = MCSocket(7777)

filename = input('Enter the world to save stats for: ')


def get_response(response):
    res = response
    response = str(resp).replace(" ", "").lower()
    print(response)

    ret = []
    if "rot" in response:
        ret.append("Spawn 10 zombies")
    if "bone" in response:
        ret.append("Spawn 10 skeletons")
    if "food" in response:
        ret.append("Lose all hunger")
    if "gone" in response:
        ret.append("Lose something random")
    if "end" in response:
        ret.append("Spawn 10 angry endermen")
    if "mine" in response:
        ret.append("Mining fatigue")
    if 'hole' in response:
        ret.append("Big hole")
    if 'night' in response:
        ret.append("Set time to night")
    if 'dragon' in response:
        ret.append("Spawn dragon")
    if 'heart' in response:
        ret.append("Set to half a heart")
    if 'jump' in response:
        ret.append("Launched in the air")
    if 'jail' in response:
        ret.append("Surround in obsidian")
    if 'lava' in response:
        ret.append("Lava source block")
    if 'dead' in response:
        ret.append("Instant death")
    if 'drop' in response:
        ret.append("Drop inventory")

    mc_socket.update(filename, ret)

    if 'showstats' in response:
        for stat in mc_socket.get_stats(filename):
            ret.append(stat)
    else:
        ret.append(res)
    return ret


while 1:
    try:
        with mic as src:
            r.adjust_for_ambient_noise(src)
            audio = r.listen(src)
            resp = r.recognize_google(audio)
            cmd = get_response(resp)

            mc.stream(cmd)

    except speech_recognition.UnknownValueError:
        pass
