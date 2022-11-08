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

mc = MCSocket(7777)

filename = input('Enter the world to save stats for: ')


def get_response(response):
    res = response
    response = str(resp).replace(" ", "").lower()
    print(response)

    ret = []
    if "noshot" in response:
        ret.append("Lose 10 arrows")
    if "bear" in response:
        ret.append("Spawn 7 hostile polar bears")
    if "axolotl" in response:
        ret.append("Axolotl time")
    if "rot" in response:
        ret.append("Spawn 7 zombies")
    if "bone" in response:
        ret.append("Spawn 7 skeletons")
    if "pig" in response:
        ret.append("Lose all hunger")
    if "sub" in response:
        ret.append("Lose something random")
    if "creep" in response:
        ret.append("Spawn 7 creepers")
    if "rod" in response:
        ret.append("Spawn 7 blazes")
    if "end" in response:
        ret.append("Spawn 7 angry endermen")
    if "nether" in response:
        ret.append("Spawn 7 wither skeletons")
    if "cave" in response:
        ret.append("Mining fatigue")
    if 'follow' in response:
        ret.append("Big hole")
    if 'day' in response:
        ret.append("Set time to night")
    if 'bed' in response:
        ret.append("Spawn 7 phantoms")
    if 'dragon' in response:
        ret.append("Play dragon noise, spawn 10 endermite")
    if 'twitch' in response:
        ret.append("Spawn supercharged creeper")
    if 'coal' in response:
        ret.append("Set on fire")
    if 'iron' in response:
        ret.append("Spawn aggro iron golem")
    if 'gold' in response:
        ret.append("Spawn pigmen")
    if 'diamond' in response:
        ret.append("Set to half a heart")
    if 'mod' in response:
        ret.append("Shuffle inventory")  # Shuffle inventory
    if 'port' in response:
        ret.append("Teleport randomly")
    if 'water' in response:
        ret.append("In water")
    if 'block' in response:
        ret.append("Spawn killer rabbits")
    if 'high' in response:
        ret.append("Launched in the air")
    if 'craft' in response:
        ret.append("Surround in stone")
    if 'village' in response:
        ret.append("Spawn witches")
    if 'mine' in response:
        ret.append("Give something useless")
    if 'gam' in response:
        ret.append("Random explosion")
    if 'light' in response:
        ret.append("Lightning")
    if 'ink' in response:
        ret.append("Ink Splat")
    if 'bud' in response:
        ret.append("Knockback")
    if 'yike' in response:
        ret.append("Lava source block")
    if 'poggers' in response:
        ret.append("Heal 1 heart")
    if 'blessmepapi' in response:
        ret.append("No effects for 20 seconds")
    if 'dream' in response:
        ret.append("Instant death")
    if 'thing' in response:
        ret.append("Give iron nugget")
    if 'godlike' in response:
        ret.append("Strength effect")
    if 'troll' in response:
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
