import tomllib
from dataclasses import dataclass
from abc import ABC, abstractmethod
import socket
import struct
import json
import requests
import logging


class Info:
    def __init__(self, data: dict):
        if not data:
            self.is_online = False
            return
        self.raw_data = data
        self.is_online = True

    def __str__(self) -> str:
        return f"Info: {self.is_online}"


class Status(Info):
    def __init__(self, data: dict):
        super().__init__(data)
        self.version = data.get("version")
        self.players_num = data.get("players")
        self.station_time = data.get("stationtime")
        self.round_duration = data.get("roundtime")
        self.map = data.get("map")
        self.admins_num = data.get("admins")
        self.round_id = data.get("round_id")

    def __str__(self) -> str:
        return (
            f"Server rev: {self.version}\n"
            f"Is online: {self.is_online}\n"
            f"Players: {self.players_num}\n"
            f"Admins: {self.admins_num}"
        )


class Revision(Info):
    def __init__(self, data: dict):
        super().__init__(data)
        self.gameid = data.get("gameid")
        self.dm_version = data.get("dm_version")
        self.dm_build = data.get("dm_build")
        self.dd_verion = data.get("dd_version")
        self.dd_build = data.get("dd_build")
        self.revision = data.get("revision")
        self.git_branch = data.get("branch")
        self.date = data.get("date")


class Manifest(Info):
    def __init__(self, data: dict):
        super().__init__(data)
        self.manifest = data


class Who(Info):
    def __init__(self, data: dict) -> None:
        super().__init__(data)
        self.players = data.get("data")

    def __str__(self) -> str:
        return str(self.players)


class AWho(Info):
    def __init__(self, data: dict) -> None:
        super().__init__(data)
        self.admins = data.get("data")

    def __str__(self) -> str:
        return "\n".join(str(admin) for admin in self.admins)


class Success(Info):
    def __init__(self, data: dict):
        super().__init__(data)
        if "success" in data:
            self.success = data.get("success")
        else:
            self.success = data.get("error")

    def __str__(self) -> str:
        return f"Response: {self.success}"


@dataclass
class Server(ABC):
    name: str
    build: str
    ip: str
    port: int
    key: str

    @abstractmethod
    def get_server_status(self) -> Status:
        pass

    @abstractmethod
    def get_players_list(self) -> Who:
        pass

    @abstractmethod
    def get_admin_who(self) -> AWho:
        pass

    @abstractmethod
    def send_host_announce(self, msg) -> Success:
        pass

    @abstractmethod
    def send_admin_msg(self, ckey, msg, admin_name) -> Success:
        pass


class Server13(Server):
    # TODO: add cyrillic support
    @staticmethod
    def __prepare_packet(data: str):
        # Magic reverse engineered request format
        return b"\x00\x83" + struct.pack('>H', len(data) + 6) + b"\x00\x00\x00\x00\x00" + data.encode() + b"\x00"

    def __send_receive_data(self, command: str) -> bytes:

        sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        try:
            sock.connect((self.ip, self.port))
        except (ConnectionRefusedError, TimeoutError):
            logging.error(f"Could not execute {command} at server {self.ip}:{self.port} due to connection error.")
            return bytes()
        request = self.__prepare_packet(command)
        sock.sendall(request)
        response = sock.recv(16384)
        sock.close()
        return response

    def __decode_byond(self, data: bytes) -> dict:
        if not data:
            return {}
        return self.__decode_data(data)

    def __decode_data(self, data: bytes) -> dict:
        data = data[5:-1].decode()
        if not data:
            return {}
        data = json.loads(data)
        if isinstance(data, list):
            data = {"data": data}
        return data

    def __do_command(self, cmd: str) -> dict:
        if self.key:
            cmd = cmd + f"&key={self.key} + &format=json"
        logging.debug(f"Used command {cmd} at server {self.ip}:{self.port}.")
        return self.__decode_byond(self.__send_receive_data(cmd))

    def get_server_status(self) -> Status:
        return Status(self.__do_command("status"))

    def get_players_list(self) -> Who:
        return Who(self.__do_command("playerlist"))

    def get_admin_who(self) -> AWho:
        return AWho(self.__do_command("adminwho"))

    def send_host_announce(self, msg) -> Success:
        return Success(self.__do_command(f"announce={msg}"))

    def send_admin_msg(self, ckey, msg, admin_name) -> Success:
        return Success({"error": "Not implemented"})  # TODO


class Server13Skyrat(Server13):
    def __decode_data(self, data) -> dict:
        data = "".join([chr(x) for x in data[5:-1]])
        data = data.split("&")
        ready_data = {}
        for data_part in data:
            if "=" in data_part:
                data_parts = data_part.split("=")
                ready_data[data_parts[0]] = data_parts[1]
            else:
                ready_data[data_part] = "0"
        return ready_data


class Server14(Server):
    def __do_command(self, cmd: str) -> dict:
        data = {}
        try:
            res = requests.get(f'http://{self.ip}:{self.port}/{cmd}', timeout=5)
            data = res.json()
        except Exception as e:
            logging.error(f"Could not execute {cmd} at server {self.ip}:{self.port}. Error: {e}")
        return data

    def get_server_status(self) -> Status:
        return Status(self.__do_command("status"))

    def get_players_list(self) -> Who:
        return Who({})  # TODO

    def get_admin_who(self) -> AWho:
        return AWho({})  # TODO

    def send_host_announce(self, msg) -> Success:
        return Success({"error": "Not implemented"})  # TODO

    def send_admin_msg(self, ckey, msg, admin_name) -> Success:
        return Success({"error": "Not implemented"})  # TODO


def load_servers_config(config: dict) -> list[Server]:
    our_servers = config["our_servers"]
    container = []
    for SS in our_servers:
        for server in our_servers[SS]:
            server_info = our_servers[SS][server]
            server_obj = (
                Server13(server, server_info["build"], server_info["ip"], server_info["port"], server_info["key"])
                if SS == "SS13"
                else Server14(server, server_info["build"], server_info["ip"], server_info["port"], server_info["key"]))

            container.append(server_obj)
    return container


def main():
    with open("config.toml", "rb") as file:
        config = tomllib.load(file)
    our_servers = load_servers_config(config)
    print(our_servers[2].get_server_status())


if __name__ == "__main__":
    main()
