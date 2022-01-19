import socket
import speech_recognition
import speech_recognition as sr

"""
No shot: Lose 5 arrows
Bear: Spawn 5 hostile polar bears
Axolotl: Give pufferfish effects, spawn lots of fish
Rot: Spawn 7 zombies
Bone: Spawn 7 skeletons
Pig: Drop hunger by 2
Sub: Lose something random from your inventory
Creep: Spawn 5 creepers
Rod: Spawn 5 blazes
End: Spawn 5 angry endermen
Nether: Spawn 7 wither skeletons
Follow: Create an 8 block hole under you
Day: Set time to night
"""

r = sr.Recognizer()
r.energy_threshold = 300

mic = sr.Microphone()

host = 'localhost'
port = 7777
client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
client_socket.connect((host, port))


def stream(in_list):
    for element in in_list:
        try:
            client_socket.send((element + '\r\n').encode('utf-8'))

        except Exception as e:
            print(element)
            raise e


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
        ret.append("Shuffle inventory")    # Shuffle inventory
    if 'port' in response:
        ret.append("Teleport randomly")
    if 'water' in response:
        ret.append("In water")
    if 'block' in response:
        ret.append("Spawn killer rabbits")
    if 'up' in response:
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
    if 'poggers' in response:
        ret.append("He said poggers")

    ret.append(res)
    return ret


while 1:
    try:
        with mic as src:
            r.adjust_for_ambient_noise(src)
            audio = r.listen(src)
            resp = r.recognize_google(audio)
            cmd = get_response(resp)

            stream(cmd)

    except speech_recognition.UnknownValueError:
        pass
