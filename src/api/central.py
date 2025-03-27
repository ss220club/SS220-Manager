import logging
from aiohttp import ClientSession
from pydantic import BaseModel
from datetime import datetime

from common.helpers import sanitize_ckey


class Player(BaseModel):
    id: int
    ckey: str
    discord_id: int


class Whitelist(BaseModel):
    id: int
    player_id: int
    server_type: str
    admin_id: int
    issue_time: datetime
    expiration_time: datetime
    valid: bool


class WhitelistBan(Whitelist):
    reason: str | None


class Donation(BaseModel):
    id: int
    player_id: int
    tier: int
    issue_time: datetime
    expiration_time: datetime
    valid: bool


class Central:
    def __init__(self, endpoint: str, bearer_token: str, donation_manager_discord_id: int) -> None:
        self.endpoint = endpoint
        self.bearer_token = bearer_token
        self.donation_manager_discord_id = str(donation_manager_discord_id)

    async def get_player(self, param: str, param_value: str | int) -> Player | None:
        endpoint = f"{self.endpoint}/v1/players/{param}/{param_value}"

        async with ClientSession() as session:
            async with session.get(endpoint, headers={"Authorization": f"Bearer {self.bearer_token}"}) as response:
                if response.status == 404:
                    return None
                elif response.status != 200:
                    raise Exception(f"Failed to get player: {response.status} - {await response.text()}")
                return Player.model_validate(await response.json())

    async def get_player_by_ckey(self, ckey: str) -> Player | None:
        return await self.get_player(param="ckey", param_value=sanitize_ckey(ckey))

    async def get_player_by_discord(self, discord_id: int) -> Player | None:
        return await self.get_player(param="discord", param_value=discord_id)

    async def get_player_whitelists(self, ckey: str | None = None, discord_id: int | None = None, admin_discord_id: int | None = None, server_type: str | None = None, active_only: bool = False) -> list[Player]:
        params = {}
        if ckey:
            params["ckey"] = sanitize_ckey(ckey)
        if discord_id:
            params["discord_id"] = discord_id
        if admin_discord_id:
            params["admin_discord_id"] = admin_discord_id
        if server_type:
            params["server_type"] = server_type
        # I hate this, but for some reason aiohttp doesn't support bool params
        params["active_only"] = "true" if active_only else "false"

        endpoint = f"{self.endpoint}/v1/whitelists"
        async with ClientSession() as session:
            async with session.get(endpoint, params=params, headers={"Authorization": f"Bearer {self.bearer_token}"}) as response:
                if response.status != 200:
                    raise Exception(f"Failed to get player whitelists: {response.status} - {await response.text()}")
                whitelists = (await response.json())["items"]
                return [Whitelist.model_validate(whitelist) for whitelist in whitelists]

    async def give_donate_tier(self, discord_id: int, tier: int, duration_days: int):
        endpoint = f"{self.endpoint}/v1/donates"
        body = {
            "discord_id": str(discord_id),
            "tier": tier,
            "duration_days": duration_days  # forever
        }
        async with ClientSession() as session:
            async with session.post(endpoint, json=body, headers={"Authorization": f"Bearer {self.bearer_token}"}) as response:
                if response.status != 201:
                    raise Exception(f"Failed to give donate tier: {response.status} - {await response.text()}")

    async def get_player_active_donates(self, discord_id: int) -> list[Donation]:

        endpoint = f"{self.endpoint}/v1/donates"
        params = {
            "discord_id": discord_id,
            "active_only": "true"
        }

        async with ClientSession() as session:
            async with session.get(endpoint, params=params, headers={"Authorization": f"Bearer {self.bearer_token}"}) as response:
                if response.status not in [200, 404]:
                    raise Exception(f"Failed to get player whitelists: {response.status} - {await response.text()}")
                donates = (await response.json())["items"]
                return [Donation.model_validate(donate) for donate in donates]

    async def remove_donate_tiers(self, discord_id: int):
        current_donations = await self.get_player_active_donates(discord_id)
        endpoint = f"{self.endpoint}/v1/donates"
        async with ClientSession() as session:
            for donation in current_donations:
                body = {
                    "expiration_time": datetime.now().isoformat()
                }
                async with session.patch(f"{endpoint}/{donation.id}", json=body, headers={"Authorization": f"Bearer {self.bearer_token}"}) as response:
                    if response.status != 200:
                        raise Exception(f"Failed to remove donate tier: {response.status} - {await response.text()}")
                    logging.info(
                        f"Removed donate tiers {donation.tier} for {discord_id}")

    async def remove_donate_wls(self, discord_id: int):
        current_donate_wls = await self.get_player_whitelists(
            discord_id=discord_id,
            admin_discord_id=self.donation_manager_discord_id,
            active_only=True
        )
        # TODO: probably should handle different donate tiers and roles and etc
        endpoint = f"{self.endpoint}/v1/whitelists"
        async with ClientSession() as session:
            for donate_wl in current_donate_wls:
                body = {
                    "expiration_time": datetime.now().isoformat()
                }
                async with session.patch(f"{endpoint}/{donate_wl.id}", json=body, headers={"Authorization": f"Bearer {self.bearer_token}"}) as response:
                    if response.status != 200:
                        raise Exception(f"Failed to remove donate wl: {response.status} - {await response.text()}")
                    logging.info(f"Removed donate wl for {discord_id}")

    async def give_whitelist_discord(self, player_discord_id: int, admin_discord_id: int, server_type: str, duration_days: int) -> tuple[int, Whitelist]:
        endpoint = f"{self.endpoint}/v1/whitelists"
        body = {
            "server_type": server_type,
            "player_discord_id": str(player_discord_id),
            "admin_discord_id": str(admin_discord_id),
            "duration_days": duration_days
        }
        async with ClientSession() as session:
            async with session.post(endpoint, json=body, headers={"Authorization": f"Bearer {self.bearer_token}"}) as response:
                if response.status not in [201, 409]:
                    raise Exception(f"Failed to give whitelist: {response.status} - {await response.text()}")
                if response.status == 201:
                    logging.info(
                        f"Whitelist given to {player_discord_id} by {admin_discord_id}")
                return (response.status, Whitelist.model_validate(await response.json()) if response.status == 201 else None)

    async def ban_whitelist_discord(self, player_discord_id: int, admin_discord_id: int, server_type: str, duration_days: int, reason: str | None = None) -> WhitelistBan:
        endpoint = f"{self.endpoint}/v1/whitelist_bans"
        body = {
            "server_type": server_type,
            "player_discord_id": str(player_discord_id),
            "admin_discord_id": str(admin_discord_id),
            "duration_days": duration_days,
            "reason": reason
        }
        async with ClientSession() as session:
            async with session.post(endpoint, json=body, headers={"Authorization": f"Bearer {self.bearer_token}"}) as response:
                if response.status != 201:
                    raise Exception(f"Failed to whitelist ban: {response.status} - {await response.text()}")
                logging.info(
                    f"Whitelist ban given to {player_discord_id} by {admin_discord_id}")
                return WhitelistBan.model_validate(await response.json())

    async def get_whitelist_bans(self, player_discord_id: int | None = None, admin_discord_id: int | None = None, server_type: str | None = None, active_only: bool = False, amount: int | None = None) -> list[WhitelistBan]:
        endpoint = f"{self.endpoint}/v1/whitelist_bans"
        params = {}
        if player_discord_id:
            params["discord_id"] = player_discord_id
        if admin_discord_id:
            params["admin_discord_id"] = admin_discord_id
        if server_type:
            params["server_type"] = server_type
        if amount:
            params["page_size"] = amount
        # I hate this, but for some reason aiohttp doesn't support bool params
        params["active_only"] = "true" if active_only else "false"

        async with ClientSession() as session:
            async with session.get(endpoint, params=params, headers={"Authorization": f"Bearer {self.bearer_token}"}) as response:
                if response.status != 200:
                    raise Exception(f"Failed to get whitelist bans: {response.status} - {await response.text()}")
                whitelist_bans = (await response.json())["items"]
                return [WhitelistBan.model_validate(whitelist_ban) for whitelist_ban in whitelist_bans]

    async def pardon_whitelist_ban(self, whitelist_ban_id: int) -> tuple[int, WhitelistBan]:
        endpoint = f"{self.endpoint}/v1/whitelist_bans/{whitelist_ban_id}"
        body = {
            "valid": False
        }
        async with ClientSession() as session:
            async with session.patch(endpoint, json=body, headers={"Authorization": f"Bearer {self.bearer_token}"}) as response:
                if response.status not in [200, 404]:
                    raise Exception(f"Failed to pardon whitelist ban: {response.status} - {await response.text()}")

                return (response.status, WhitelistBan.model_validate(await response.json()) if response.status == 200 else None)

    async def get_whitelisted_discord_ids(self, server_type: str, active_only: bool) -> list[int]:
        endpoint = f"{self.endpoint}/v1/whitelists/discord_ids"
        params = {
            "server_type": server_type,
            "active_only": "true" if active_only else "false",
            "page_size": 999999
        }
        async with ClientSession() as session:
            async with session.get(endpoint, params=params, headers={"Authorization": f"Bearer {self.bearer_token}"}) as response:
                if response.status != 200:
                    raise Exception(f"Failed to get whitelisted discord ids: {response.status} - {await response.text()}")
                return list(map(int, (await response.json())["items"]))
