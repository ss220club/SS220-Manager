from aiohttp import ClientSession
from pydantic import BaseModel
from datetime import datetime


class Player(BaseModel):
    id: int
    ckey: str
    discord_id: int


class Whitelist(BaseModel):
    id: int
    player_id: int
    wl_type: str
    admin_id: int
    issue_time: datetime
    expiration_time: datetime
    valid: bool


class Central:
    def __init__(self, endpoint: str, bearer_token: str) -> None:
        self.endpoint = endpoint
        self.bearer_token = bearer_token

    async def get_player(self, param: str, param_value: str) -> Player | None:
        endpoint = f"{self.endpoint}/v1/players/{param}/{param_value}"

        async with ClientSession() as session:
            async with session.get(endpoint, headers={"Authorization": f"Bearer {self.bearer_token}"}) as response:
                if response.status == 404:
                    return None
                elif response.status != 200:
                    raise Exception(f"Failed to get player: {response.status} - {await response.text()}")
                return Player.model_validate(await response.json())

    async def get_player_by_ckey(self, ckey: str) -> Player:
        return await self.get_player(param="ckey", param_value=ckey)

    async def get_player_by_discord(self, discord_id: int) -> Player:
        return await self.get_player(param="discord", param_value=discord_id)

    async def get_player_whitelists(self, ckey: str | None = None, discord_id: int | None = None, wl_type: str | None = None) -> list[Player]:
        params = {}
        if ckey:
            params["ckey"] = ckey
        if discord_id:
            params["discord_id"] = discord_id
        if wl_type:
            params["wl_type"] = wl_type
        endpoint = f"{self.endpoint}/v1/whitelists"
        async with ClientSession() as session:
            async with session.get(endpoint, params=params, headers={"Authorization": f"Bearer {self.bearer_token}"}) as response:
                if response.status != 200:
                    raise Exception(f"Failed to get player whitelists: {response.status} - {await response.text()}")
                whitelists = (await response.json())["items"]
                return [Whitelist.model_validate(whitelist) for whitelist in whitelists]

    async def give_donate_tier(self, discord_id: int, tier: int):
        endpoint = f"{self.endpoint}/v1/donates"
        body = {
            "discord_id": discord_id,
            "tier": tier
        }
        async with ClientSession() as session:
            async with session.post(endpoint, json=body, headers={"Authorization": f"Bearer {self.bearer_token}"}) as response:
                if response.status != 201:
                    raise Exception(f"Failed to give donate tier: {response.status} - {await response.text()}")
                # TODO: handle player not found