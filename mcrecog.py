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
Prime/Sub: 
Creep: Spawn 5 creepers
Rod: Spawn 5 blazes
End: Spawn 5 angry endermen
Nether: Spawn 7 wither skeletons
"""

r = sr.Recognizer()
r.energy_threshold = 300

mic = sr.Microphone()

host = 'localhost'
port = 8080
client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
client_socket.connect((host, port))


def stream(in_list):
    for element in in_list:
        try:
            client_socket.send((element + '\r\n').encode('utf-8'))

            if element == "quit":
                client_socket.close()
        except Exception as e:
            print(element)
            raise e


def get_response(response):
    res = response
    response = str(resp).replace(" ", "").lower()
    print(response)

    ret = []
    if "no shot" in response:
        ret.append("Lose 5 arrows")
    if "bear" in response:
        ret.append("Spawn 5 hostile polar bears")
    if "axolotl" in response:
        ret.append("Axolotl time")
    if "rot" in response:
        ret.append("Spawn 7 zombies")
    if "bone" in response:
        ret.append("Spawn 7 skeletons")
    if "pig" in response:
        ret.append("Drop hunger by 5")
    if "prime" in response or "sub" in response:
        ret.append("")
    if "creep" in response:
        ret.append("Spawn 5 creepers")
    if "rod" in response:
        ret.append("Spawn 5 blazes")
    if "end" in response:
        ret.append("Spawn 5 angry endermen")
    if "nether" in response:
        ret.append("Spawn 7 wither skeletons")
    if "cave" in response:
        ret.append("Mining fatigue")
    if 'quit' in response:
        ret.append("Quit")

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
