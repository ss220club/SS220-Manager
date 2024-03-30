import logging

import enum
from datetime import datetime
from typing import Sequence
from sqlalchemy import String
from sqlalchemy import Integer
from sqlalchemy import DateTime
from sqlalchemy import Enum

from sqlalchemy import Select
from sqlalchemy import create_engine
from sqlalchemy import select
from sqlalchemy.orm import DeclarativeBase
from sqlalchemy.orm import Mapped
from sqlalchemy.orm import mapped_column
from sqlalchemy.orm import sessionmaker

from helpers import *

logging.debug("STARTED UP")


class Base(DeclarativeBase):
    pass


class ChangelogTypes(enum.Enum):
    FIX = 1
    WIP = 2
    TWEAK = 3
    SOUNDADD = 4
    SOUNDDEL = 5
    CODEADD = 6
    CODEDEL = 7
    IMAGEADD = 8
    IMAGEDEL = 9
    SPELLCHECK = 10
    EXPERIMENT = 11


class DBSchema:
    def __init__(self, engine, user, password, ip, port, dbname) -> None:
        self.engine = create_engine(
            f"{engine}://{user}:{password}@{ip}:{port}/{dbname}",
            pool_size=10,
            max_overflow=0,
            pool_recycle=1800,
            pool_pre_ping=True,
        )
        self.Session = sessionmaker(self.engine)

    def execute_req(self, req: Select) -> Sequence:
        with self.Session() as session:
            return session.scalars(req).all()


class Paradise(DBSchema):
    class Player(Base):
        __tablename__ = "player"
        id: Mapped[int] = mapped_column(primary_key=True)
        ckey: Mapped[str] = mapped_column(String(32))
        firstseen: Mapped[datetime] = mapped_column(DateTime)
        lastseen: Mapped[datetime] = mapped_column(DateTime)
        ip: Mapped[str] = mapped_column(String(18))
        computerid: Mapped[str] = mapped_column(String(32))
        lastadminrank: Mapped[str] = mapped_column(String(32))
        exp: Mapped[str] = mapped_column(String())
        species_whitelist: Mapped[str] = mapped_column(String())

        def __repr__(self) -> str:
            return f"Player(id={self.id!r}, \
            ckey={self.ckey!r})"

    class Character(Base):
        __tablename__ = "characters"
        id: Mapped[int] = mapped_column(primary_key=True)
        ckey: Mapped[str] = mapped_column(String(32))
        slot: Mapped[int] = mapped_column(Integer())
        real_name: Mapped[str] = mapped_column(String(55))
        gender: Mapped[str] = mapped_column(String(11))
        age: Mapped[int] = mapped_column(Integer())
        species: Mapped[str] = mapped_column(String(45))

        def __repr__(self) -> str:
            return f"Character(id={self.id!r}, \
            ckey={self.ckey!r}, \
            real_name={self.real_name!r})"

    class Changelog(Base):
        __tablename__ = "changelog"
        id: Mapped[int] = mapped_column(primary_key=True)
        pr_number: Mapped[int] = mapped_column(Integer())
        date_merged: Mapped[DateTime] = mapped_column(DateTime)
        author: Mapped[str] = mapped_column(String(32))
        cl_type: Mapped[ChangelogTypes] = mapped_column(Enum(ChangelogTypes))
        cl_entry: Mapped[str] = mapped_column(String())

    class Ban(Base):
        __tablename__ = "ban"
        id: Mapped[int] = mapped_column(primary_key=True)
        bantime: Mapped[DateTime] = mapped_column(DateTime)
        ban_round_id: Mapped[int] = mapped_column(Integer())
        serverip: Mapped[str] = mapped_column(String(32))
        server_id: Mapped[str] = mapped_column(String(50))
        bantype: Mapped[str] = mapped_column(String(32))
        reason: Mapped[str] = mapped_column(String())
        job: Mapped[str] = mapped_column(String(32))
        duration: Mapped[int] = mapped_column(Integer())
        rounds: Mapped[int] = mapped_column(Integer())
        expiration_time: Mapped[DateTime] = mapped_column(DateTime)
        ckey: Mapped[str] = mapped_column(String(32))
        computerid: Mapped[str] = mapped_column(String(32))
        ip: Mapped[str] = mapped_column(String(32))
        a_ckey: Mapped[str] = mapped_column(String(32))
        unbanned: Mapped[int] = mapped_column(Integer())
        unbanned_ckey: Mapped[str] = mapped_column(String(32))
        exportable: Mapped[int] = mapped_column(Integer())

        def __repr__(self) -> str:
            return f"Ban(id={self.id!r}, \
            ckey={self.ckey!r}, \
            reason={self.reason!r}, \
            a_ckey={self.a_ckey!r})"

    class Note(Base):
        __tablename__ = "notes"
        id: Mapped[int] = mapped_column(primary_key=True)
        ckey: Mapped[str] = mapped_column(String(32))
        notetext: Mapped[str] = mapped_column(String())
        timestamp: Mapped[DateTime] = mapped_column(DateTime)
        round_id: Mapped[int] = mapped_column(Integer())
        adminckey: Mapped[str] = mapped_column(String(32))
        server: Mapped[str] = mapped_column(String(50))

        def __repr__(self) -> str:
            return f"Note(id={self.id!r}, \
            ckey={self.ckey!r}, \
            reason={self.notetext!r}, \
            a_ckey={self.adminckey!r})"

    class Watch(Base):
        __tablename__ = "watch"
        ckey: Mapped[str] = mapped_column(primary_key=True)
        reason: Mapped[str] = mapped_column(String())
        timestamp: Mapped[DateTime] = mapped_column(DateTime)
        adminckey: Mapped[str] = mapped_column(String(32))

        def __repr__(self) -> str:
            return f"Watch(ckey={self.ckey!r}, \
            reason={self.reason!r}, \
            a_ckey={self.adminckey!r})"

    class DiscordLink(Base):
        __tablename__ = "discord_links"
        id: Mapped[int] = mapped_column(primary_key=True)
        ckey: Mapped[int] = mapped_column(Integer())
        discord_id: Mapped[int] = mapped_column(Integer())
        timestamp: Mapped[DateTime] = mapped_column(DateTime)
        one_time_token: Mapped[str] = mapped_column(String(100))
        valid: Mapped[int] = mapped_column(Integer())

        def __repr__(self) -> str:
            return f"Discord_Link(id={self.id!r}, \
            ckey={self.ckey!r}, \
            reason={self.discord_id!r}, \
            a_ckey={self.valid!r})"

    def get_player(self, ckey: str) -> tuple[Player, DiscordLink]:
        ckey = sanitize_ckey(ckey)
        with self.Session() as session:
            result = session.query(self.Player, self.DiscordLink).join(
                self.Player, self.Player.ckey == self.DiscordLink.ckey
            ).where(self.Player.ckey == ckey).first()
            player, discord_link = result if result else (False, False)
            return player, discord_link

    def get_player_by_discord(self, discord_id: int) -> tuple[Player, DiscordLink]:
        with self.Session() as session:
            result = session.query(self.Player, self.DiscordLink).join(
                self.Player, self.Player.ckey == self.DiscordLink.ckey
            ).where(self.DiscordLink.discord_id == discord_id).first()
            player, discord_link = result if result else (False, False)
            return player, discord_link

    def get_characters(self, ckey: str) -> Sequence[Character]:
        ckey = sanitize_ckey(ckey)
        req = select(self.Character).where(self.Character.ckey == ckey)
        return self.execute_req(req)

    def get_characters_by_name(self, name: str) -> Sequence[Character]:
        req = select(self.Character).where(
            self.Character.real_name.regexp_match(name))
        return self.execute_req(req)

    def get_recent_bans(self) -> Sequence[Ban]:
        req = select(self.Ban).where(self.Ban.exportable).order_by(
            self.Ban.id.desc()).limit(50)
        with self.Session() as session:
            session.expire_on_commit = False
            with session.begin():
                result = session.scalars(req).all()
                for ban in result:
                    ban.exportable = 0
        return result

    def get_bans(self, ckey: str) -> Sequence[Ban]:
        ckey = sanitize_ckey(ckey)
        req = select(self.Ban).where((self.Ban.ckey == ckey)
                                     | (self.Ban.a_ckey == ckey)).order_by(self.Ban.id.desc())
        return self.execute_req(req)

    def get_notes(self, ckey: str, amount: int) -> Sequence[Note]:
        ckey = sanitize_ckey(ckey)
        req = select(self.Note).where((self.Note.ckey == ckey)
                                      | (self.Note.adminckey == ckey)).order_by(self.Note.id.desc()).limit(amount)
        return self.execute_req(req)

    def link_account(self, discord_id: int, token: str) -> DiscordLink:
        collisions_req = select(self.DiscordLink).where(
            self.DiscordLink.discord_id == discord_id)
        coll = self.execute_req(collisions_req)
        if coll:
            return ERRORS.ERR_BOUND

        req = select(self.DiscordLink).where(
            self.DiscordLink.one_time_token == token)
        with self.Session() as session:
            session.expire_on_commit = False
            with session.begin():
                result = session.scalars(req).one_or_none()
                if not result:
                    return ERRORS.ERR_404
                result.discord_id = discord_id
                result.valid = 1
        return result

    def get_player_species_whitelist(self, ckey: str) -> Player.species_whitelist:
        ckey = sanitize_ckey(ckey)
        species_whitelist_req = select(self.Player.species_whitelist).where(
            self.Player.ckey == ckey)
        species_whitelist = self.execute_req(species_whitelist_req)
        return species_whitelist

    def set_player_species_whitelist(self, ckey: str, species_whitelist: Player.species_whitelist) -> Player:
        ckey = sanitize_ckey(ckey)

        req = select(self.Player).where(
            self.Player.ckey == ckey)

        with self.Session() as session:
            session.expire_on_commit = False
            with session.begin():
                result = session.scalars(req).one_or_none()
                if not result:
                    return ERRORS.ERR_404
                result.species_whitelist = species_whitelist

        return result


def main():
    pass


if __name__ == "__main__":
    main()
