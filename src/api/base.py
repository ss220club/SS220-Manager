from abc import ABC, abstractmethod
from dataclasses import dataclass


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
