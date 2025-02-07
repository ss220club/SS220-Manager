from aiohttp import ClientSession 
from pydantic import BaseModel
from datetime import datetime

class Player(BaseModel):
    id: int
    ckey: str
    discord_id: int

class Whitelist(BaseModel):
    id: int
    ckey: str
    wl_type: str
    admin_id: int
    issue_time: datetime
    expire_time: str
    valid: bool

class Central:
    def __init__(self, endpoint: str, bearer_token: str) -> None:
        self.endpoint = endpoint
        self.bearer_token = bearer_token

    async def get_player(self, discord_id: int | None = None, ckey: str | None = None) -> Player | None:
        params = {}
        if discord_id:
            params["discord_id"] = discord_id
        if ckey:
            params["ckey"] = ckey
        endpoint = f"{self.endpoint}/v1/player"
        async with ClientSession() as session:
            async with session.get(endpoint, params=params, headers={"Authorization": f"Bearer {self.bearer_token}"}) as response:
                if response.status == 404:
                    return None
                elif response.status != 200:
                    raise Exception(f"Failed to get player: {response.status} - {await response.text()}")
                return Player.model_validate_json(await response.read())

    async def get_player_by_ckey(self, ckey: str) -> Player:
        return await self.get_player(ckey=ckey)
    
    async def get_player_by_discord(self, discord_id: int) -> Player:
        return await self.get_player(discord_id=discord_id)

    async def get_player_whitelists(self, ckey: str | None = None, discord_id: int | None = None, wl_type: str | None = None) -> list[Player]:
        params = {}
        if ckey:
            params["ckey"] = ckey
        if discord_id:
            params["discord_id"] = discord_id
        if wl_type:
            params["wl_type"] = wl_type
        endpoint = f"{self.endpoint}/v1/player/whitelists"
        async with ClientSession() as session:
            async with session.get(endpoint, params=params, headers={"Authorization": f"Bearer {self.bearer_token}"}) as response:
                if response.status != 200:
                    raise Exception(f"Failed to get player whitelists: {response.status} - {await response.text()}")
                return [Whitelist.model_validate_json(whitelist) for whitelist in await response.json()]