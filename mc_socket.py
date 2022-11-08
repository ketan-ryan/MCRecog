import socket
import json
import os


class MCSocket:
    host = 'localhost'
    port = 0
    client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

    def __init__(self, port):
        self.port = port
        self.client_socket.connect((self.host, self.port))

    def stream(self, in_list):
        for element in in_list:
            try:
                self.client_socket.send((element + '\r\n').encode('utf-8'))

            except Exception as e:
                print(element)
                raise e


def update(filename, lst):
    path = f'stats_{filename}.json'

    if not os.path.isfile(path):
        with open(path, 'w+') as fp:
            fp.write('{\n}')

    else:
        with open(path, 'r+') as fp:
            data = json.load(fp)

            for key in lst:
                if key not in data:
                    data[key] = 1
                else:
                    data[key] = int(data[key]) + 1

            fp.seek(0)  # rewind
            json.dump(data, fp)
            fp.truncate()


def get_stats(filename):
    ret = []
    with open(f'stats_{filename}.json', 'r') as fp:
        data = json.load(fp)
        for key in data:
            ret.append(f'STATS {key} TIMES {data[key]}')
    return ret
