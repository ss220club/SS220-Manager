from typing import Callable

from db.db_base import SSDatabase
from db.db_paradise import Paradise

DB_CONNECTORS: dict[str, Callable[[dict], SSDatabase]] = {
    "paradise": lambda db_credentials: Paradise(
        db_credentials["type"],
        db_credentials["user"],
        db_credentials["password"],
        db_credentials["ip"],
        db_credentials["port"],
        db_credentials["name"]
    )
}


def connect_database(name: str, db_credentials: dict) -> SSDatabase:
    if name not in DB_CONNECTORS or not DB_CONNECTORS[name]:
        raise Exception("No database provided for name: ", name)
    return DB_CONNECTORS[name](db_credentials)
