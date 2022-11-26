import socket
import json
import os


class MCSocket:
    host = 'localhost'
    port = 0
    client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

    def __init__(self, port):
        self.port = port
        try:
            self.client_socket.connect((self.host, self.port))
        except ConnectionRefusedError:
            print("Unable to connect to Minecraft. Please run Minecraft (with the mod installed) first!")
            exit(0)

    def stream(self, in_list):
        for element in in_list:
            try:
                self.client_socket.send((element + '\r\n').encode('utf-8'))
            except ConnectionResetError:
                print("Minecraft instance is no longer running. Restart Minecraft then restart this script.")
                exit(0)
            except Exception as e:
                print(element)
                raise e