import os
import random
from typing import get_args
import asyncio

import discord
from discord import app_commands
from discord.ext import tasks
# Includes a lot of other internal libs
from common.discord_helpers import *
from db.connect import connect_database

from redis import asyncio as aioredis
from github import Github, GithubIntegration

# Setting up config
with open("config.toml", "rb") as file:
    config = tomllib.load(file)

os.makedirs("logs", exist_ok=True)
logging.basicConfig(level=config["log_level"], filename="logs/ss220.log", filemode="a+",
                    format="%(asctime)s %(levelname)s %(message)s", force=True)

HEAD_ADMIN_ROLES = config["discord"]["roles"]["heads"]
ADMIN_ROLES = [*HEAD_ADMIN_ROLES] + config["discord"]["roles"]["admins"]
MENTOR_ROLES = [*ADMIN_ROLES] + config["discord"]["roles"]["mentors"]
XENOMOD_ROLES = config["discord"]["roles"]["xenomod"]
DEV_ROLES = config["discord"]["roles"]["devs"]
MISC_ROLES = config["discord"]["roles"]["servers"]

CODER_ID = config["discord"]["coder"]

CHANNEL_CACHE: dict[str, discord.TextChannel] = {}

OUR_SERVERS = load_servers_config(config)

server_choices = [app_commands.Choice(
    name=server.name, value=i) for i, server in enumerate(OUR_SERVERS)]

NO_MENTIONS = discord.AllowedMentions(roles=False, users=False, everyone=False)
last_status_sever = 0

# Setting up db connection
DB = connect_database("paradise", config["db"]["paradise"])
REDIS = aioredis.from_url(config["redis"]["connection_string"])
REDIS_SUB = REDIS.pubsub(ignore_subscribe_messages=True)
REDIS_SUB_BINDINGS = {}


def run_bot():
    intents = discord.Intents.default()
    client = discord.Client(intents=intents)
    tree = app_commands.CommandTree(client)

    async def on_tree_error(interaction: discord.Interaction, error: app_commands.AppCommandError):
        if isinstance(error, discord.app_commands.errors.MissingAnyRole | discord.app_commands.errors.MissingRole):
            await interaction.response.send_message("Рано еще тебе такое использовать.")
        elif isinstance(error, discord.app_commands.errors.NoPrivateMessage):
            await interaction.response.send_message("Не работает в лс.")
        else:
            logging.error(f"{type(error)}: {error}")
            await interaction.followup.send(f"Что то явно пошло не так. Сообщите об ошибке кодеру(<@{CODER_ID}>)")

    tree.on_error = on_tree_error

    @tree.command(name="пинг", description="Проверить работоспособность бота.")
    async def ping(interaction: discord.Interaction):
        await interaction.response.defer()
        await interaction.followup.send("Понг!")

    @tree.command(name="онлайн", description="Показать онлайн серверов.")
    async def online(interaction: discord.Interaction):
        await interaction.response.defer()
        await interaction.followup.send(get_beautified_status(OUR_SERVERS))

    @tree.command(name="админы", description="Показать админов онлайн.")
    async def admins(interaction: discord.Interaction):
        await interaction.response.defer()
        await interaction.followup.send(get_admins(OUR_SERVERS))

    @tree.command(name="кто", description="Показать игроков онлайн.")
    @app_commands.describe(server="Игроков какого сервера показать.")
    @app_commands.choices(server=server_choices)
    async def who(interaction: discord.Interaction, server: app_commands.Choice[int]):
        await interaction.response.defer()
        await interaction.followup.send(get_players_online(OUR_SERVERS[server.value]))

    @tree.command(name="сообщение", description="Отправить ПМку игроку.")
    @app_commands.describe(server="На каком сервере игрок.")
    @app_commands.choices(server=server_choices)
    @app_commands.describe(ckey="Игрок, который получит сообщение.")
    @app_commands.describe(msg="Сообщение.")
    @app_commands.checks.has_any_role(*ADMIN_ROLES)
    async def send_admin_pm(interaction: discord.Interaction, server: app_commands.Choice[int], ckey: str, msg: str):
        await interaction.response.defer()
        await interaction.followup.send(str(OUR_SERVERS[server.value].send_admin_msg(ckey,
                                                                                     msg,
                                                                                     interaction.user.name)))

    @tree.command(name="анонс", description="Сделать анонс от имени хоста.")
    @app_commands.describe(server="Сервер для анонса.")
    @app_commands.choices(server=server_choices)
    @app_commands.describe(msg="Сообщение.")
    @app_commands.checks.has_any_role(*HEAD_ADMIN_ROLES)
    async def make_host_announce(interaction: discord.Interaction, server: app_commands.Choice[int], msg: str):
        await interaction.response.defer()
        OUR_SERVERS[server.value].send_host_announce(msg)
        await interaction.followup.send("Анонс был совершен~~, наверное.~~")

    @tree.command(name="дебаг", description="Получить сырые данные.")
    @app_commands.checks.has_any_role(*HEAD_ADMIN_ROLES)
    async def debug(interaction: discord.Interaction):
        await interaction.response.defer()
        await interaction.followup.send("Дебаг данные:")
        for server in OUR_SERVERS:
            await interaction.channel.send(f"**{server.name}:**\n{server.get_server_status().raw_data}")

    @tasks.loop(seconds=60)
    async def announce_loop():
        try:
            global last_status_sever
            server_info = OUR_SERVERS[last_status_sever].get_server_status()
            pres = f"{OUR_SERVERS[last_status_sever].name}: {server_info.players_num} [{server_info.round_duration}]"
            await client.change_presence(activity=discord.Game(name=pres))
            last_status_sever += 1
            if last_status_sever > len(OUR_SERVERS) - 1:
                last_status_sever = 0

        except Exception as error:
            logging.error(error)

        while True:
            message: dict[bytes] = await REDIS_SUB.get_message(timeout=1.0)
            if not message:
                break
            message_channel = message["channel"].decode()
            if message_channel not in REDIS_SUB_BINDINGS:
                logging.warning("Got redis event from a channel without handler: %s", message_channel)
                continue
            asyncio.create_task(REDIS_SUB_BINDINGS[message_channel](message))

    # DATABASE

    @tree.command(name="я", description="Посмотреть информацию о себе.")
    async def me(interaction: discord.Interaction):
        await interaction.response.defer()
        player_info, discord_link_info = DB.get_player_by_discord(
            interaction.user.id)
        chars = []
        if player_info and discord_link_info:
            chars = DB.get_characters(player_info.ckey)
        embed_msg = embed_player_info(player_info, discord_link_info, chars)
        await interaction.followup.send(embed=embed_msg)

    @tree.command(name="дискорд", description="Узнать дискорд айди игрока.")
    @app_commands.describe(discord_id="Дискорд айди игрока.")
    @app_commands.checks.has_any_role(*ADMIN_ROLES)
    async def player_by_discord(interaction: discord.Interaction, discord_id: discord.Member):
        await interaction.response.defer()
        player_info, discord_link_info = DB.get_player_by_discord(discord_id.id)
        chars = []
        if player_info and discord_link_info:
            chars = DB.get_characters(player_info.ckey)
        embed_msg = embed_player_info(player_info, discord_link_info, chars)
        await interaction.followup.send(embed=embed_msg, allowed_mentions=NO_MENTIONS)

    @tree.command(name="игрок", description="Посмотреть информацию об игроке.")
    @app_commands.describe(ckey="Сикей.")
    @app_commands.checks.has_any_role(*ADMIN_ROLES)
    async def player(interaction: discord.Interaction, ckey: str):
        await interaction.response.defer()
        player_info, discord_link_info = DB.get_player(ckey)
        chars = []
        if player_info and discord_link_info:
            chars = DB.get_characters(ckey)
        embed_msg = embed_player_info(player_info, discord_link_info, chars)
        await interaction.followup.send(embed=embed_msg, allowed_mentions=NO_MENTIONS)

    @tree.command(name="персонаж", description="Узнать сикей по персонажу.")
    @app_commands.describe(name="Имя.")
    @app_commands.checks.has_any_role(*ADMIN_ROLES)
    async def char(interaction: discord.Interaction, name: str):
        await interaction.response.defer()
        embed_msg = Embed(
            title=f"Персонажи по запросу {name}",
            color=Color.blue())
        characters = DB.get_characters_by_name(name)
        for character in characters[:24]:
            embed_msg.add_field(name=character.real_name, value=character.ckey)
        await interaction.followup.send(embed=embed_msg)

    @tree.command(name="баны", description="Баны по сикею.")
    @app_commands.describe(ckey="Сикей админа или игрока.")
    @app_commands.describe(num="Количество банов.")
    @app_commands.checks.has_any_role(*ADMIN_ROLES)
    async def bans(interaction: discord.Interaction, ckey: str, num: int):
        await interaction.response.defer()
        embeds = get_nice_bans(DB.get_bans(ckey))[:num]
        if not embeds:
            embeds = [Embed(title="**Отсутствуют баны, связанные с эти игроком.**",
                            color=Color.green())]
        for embed in embeds:
            await interaction.channel.send(embed=embed)
        await interaction.followup.send(f"Список банов **{ckey}**:")

    @tasks.loop(seconds=360)
    async def announceloop_long():
        logging.debug("Starting sending bans.")
        embeds = get_nice_bans(DB.get_recent_bans())
        if not embeds:
            logging.debug("No new bans")
            return
        logging.debug(f"Sending {len(embeds)} bans to banned.")
        for embed in embeds:
            await CHANNEL_CACHE.get("ban").send(embed=embed)
        logging.info(f"Sent {len(embeds)} bans to discord.")

    # noinspection PyDunderSlots
    @tree.command(name="привязать", description="Привязка аккаунта.")
    @app_commands.describe(token="Код, который вы получили в игре.")
    async def link_account(interaction: discord.Interaction, token: str):
        await interaction.response.defer()
        result = DB.link_account(interaction.user.id, token)
        embed = Embed()

        if isinstance(result, Paradise.DiscordLink):
            embed.title = "**Аккаунт успешно привязан.**"
            embed.add_field(name=result.ckey, value=result.discord_id)
            embed.color = Color.green()

        else:
            match result:
                case ERRORS.ERR_BOUND:
                    embed.title = "**Аккаунт уже привязан!**"
                    embed.color = Color.red()
                case ERRORS.ERR_404:
                    embed.title = "**Токен не найден. Попробуйте сгенерировать новый.**"
                    embed.color = Color.red()
                    embed.set_footer(
                        text="Если совсем не получается, обратитесь в help чат.")

        if embed:
            await interaction.followup.send(embed=embed)
        else:
            logging.error("Account linkage error.", result)
            await interaction.followup.send("Что то пошло не так.")

    @tree.command(name="нотесы", description="Нотесы по сикею.")
    @app_commands.describe(ckey="Сикей игрока.")
    @app_commands.describe(num="Количество нотесов.")
    @app_commands.checks.has_any_role(*ADMIN_ROLES)
    async def show_notes(interaction: discord.Interaction, ckey: str, num: int):
        await interaction.response.defer()
        notes = DB.get_notes(ckey, num)
        embeds = embed_notes(notes)
        if not embeds:
            embeds = [Embed(title="**Отсутствуют нотесы, связанные с эти игроком.**",
                            color=Color.green())]
        await interaction.followup.send(f"Список нотесов **{ckey}**:")
        for embed in embeds:
            await interaction.channel.send(embed=embed)

    # MISC

    @tree.command(name="ролл", description="Бросить кость.")
    @app_commands.describe(d="Количество граней.")
    @app_commands.describe(action="Действие.")
    async def roll(interaction: discord.Interaction, d: int, action: str):
        await interaction.response.defer()
        if d < 1:
            await interaction.followup.send("<:facepalm:1098305470017589309>")
            return
        result = (
            f"@{interaction.user.display_name} бросает {d}-гранную кость на '{action}',"
            f" и выпадает {random.randint(1, d)}!"
        )
        await interaction.followup.send(result)

    @tree.command(name="добавить_вайтлист_на_ксенорасу", description="Разрешить игроку играть на указанной ксенорасе")
    @app_commands.describe(ckey="Сикей.")
    @app_commands.describe(specie="Ксенораса.")
    @app_commands.checks.has_any_role(*XENOMOD_ROLES)
    async def add_specie_to_whitelist(interaction: discord.Interaction, ckey: str, specie: ALL_PLAYABLE_SPECIES):
        await interaction.response.defer()
        result = ""
        species_whitelist_response = DB.get_player_species_whitelist(ckey)
        if not species_whitelist_response:
            result = f"Не найден игрок с сикеем {ckey}"
        else:
            species_whitelist = json.loads(species_whitelist_response[0])

            if specie not in species_whitelist:
                species_whitelist.append(specie)
                result = f"Игрок с сикеем {ckey} получил вайтлист на расу {specie}"
                match DB.set_player_species_whitelist(ckey, json.dumps(species_whitelist)):
                    case ERRORS.ERR_404:
                        result = "Что-то пошло не так"
            else:
                result = f"У игрока {ckey} уже есть вайтлист на расу {specie}"

        await interaction.followup.send(result)

    @tree.command(name="убрать_вайтлист_на_ксенорасу", description="Отобрать у игрока вайтлист к указанной ксенорасе")
    @app_commands.describe(ckey="Сикей.")
    @app_commands.describe(specie="Ксенораса.")
    @app_commands.checks.has_any_role(*XENOMOD_ROLES)
    async def remove_specie_from_whitelist(interaction: discord.Interaction, ckey: str, specie: ALL_PLAYABLE_SPECIES):
        await interaction.response.defer()
        result = ""
        species_whitelist_response = DB.get_player_species_whitelist(ckey)
        if not species_whitelist_response:
            result = f"Не найден игрок с сикеем {ckey}"
        else:
            species_whitelist = json.loads(species_whitelist_response[0])

            if specie in species_whitelist:
                species_whitelist.remove(specie)
                result = f"Игрок с сикеем {ckey} потерял вайтлист на расу {specie}"
                match DB.set_player_species_whitelist(ckey, json.dumps(species_whitelist)):
                    case ERRORS.ERR_404:
                        result = "Что-то пошло не так"
            else:
                result = f"У игрока {ckey} уже нет вайтлиста на расу {specie}"

        await interaction.followup.send(result)

    @tree.command(name="очистить_вайтлист", description="Отобрать у игрока все вайтлисты на расы")
    @app_commands.describe(ckey="Сикей.")
    @app_commands.checks.has_any_role(*XENOMOD_ROLES)
    async def remove_all_species_from_whitelist(interaction: discord.Interaction, ckey: str):
        await interaction.response.defer()
        result = ""
        species_whitelist_response = DB.get_player_species_whitelist(ckey)
        if not species_whitelist_response:
            result = f"Не найден игрок с сикеем {ckey}"
        else:
            result = f"Игрок {ckey} потерял вайтлист на все расы, кроме человека"
            match DB.set_player_species_whitelist(ckey, "[\"Human\"]"):
                case ERRORS.ERR_404:
                    result = "Что-то пошло не так"

        await interaction.followup.send(result)

    @tree.command(name="дать_вайтлист_на_все_расы", description="Дать игроку вайтлист на все расы")
    @app_commands.describe(ckey="Сикей.")
    @app_commands.checks.has_any_role(*XENOMOD_ROLES)
    async def grant_all_species_to_player(interaction: discord.Interaction, ckey: str):
        await interaction.response.defer()
        result = ""
        species_whitelist_response = DB.get_player_species_whitelist(ckey)
        if not species_whitelist_response:
            result = f"Не найден игрок с сикеем {ckey}"
        else:
            result = f"Игрок {ckey} получил вайтлист на все расы"
            all_species = ", ".join(
                f'"{specie}"' for specie in get_args(ALL_PLAYABLE_SPECIES))
            match DB.set_player_species_whitelist(ckey, f"[{all_species}]"):
                case ERRORS.ERR_404:
                    result = "Что-то пошло не так"

        await interaction.followup.send(result)

    @tree.command(name="мерж", description="Инициировать мерж апстрима")
    @app_commands.describe(build="Билд")
    @app_commands.choices(build=[app_commands.Choice(name=build, value=build) for build in config["workflow"].keys()])
    @app_commands.checks.has_any_role(*HEAD_ADMIN_ROLES)
    async def merge_upstream(interaction: discord.Interaction, build: str):
        await interaction.response.defer()
        workflow_config = config["workflow"][build]

        try:
            with open(workflow_config["private_key_source"], "r") as key_file:
                integration = GithubIntegration(workflow_config["app_id"], key_file.read())
                token = integration.get_access_token(workflow_config["installation_id"]).token
                github = Github(token)
            repo = github.get_repo(workflow_config["repo_id"])
            merge_workflow = repo.get_workflow(workflow_config["merge_upstream"])
            if merge_workflow.create_dispatch(workflow_config["ref"]):
                result = (
                    f"Инициирован мерж апстрима {CHECKMARK_ICON}"
                    f"\n-# {build}"
                )
            else:
                result = (
                    f"Что-то пошло не так {MISTAKE_ICON}"
                    f"\n-# {build} - status code error"
                )
        except Exception as e:
            logging.error(e)
            result = (
                f"Что-то пошло не так {MISTAKE_ICON}"
                f"\n-# {build} - exception occurred"
            )

        await interaction.followup.send(result)

    async def publish_news(entry: dict[bytes]):
        logging.info("Got news from redis")
        await asyncio.sleep(config["discord"]["redis"]["news_delay"])
        article = json.loads(entry["data"].decode())
        embed = Embed(title=article["title"], color=Color.random())
        embed.add_field(name=f"{article['channel_name']} сообщает", value=article["body"])
        embed.set_footer(
            text=(
                f"{article['author']}\n"
                f"Код - {article['security_level']}, {article['publish_time']} с начала смены\n"
                "\n"
                f"{SERVERS_NICE[article['server']][0]} - {article['round_id']} - {article['author_ckey']}"
            )
        )
        img_file = None
        if article["img"]:
            img_b64 = article["img"]
            img_file = base64_to_discord_image(img_b64)
            embed.set_image(url="attachment://article_photo.png")
        channel = CHANNEL_CACHE.get("news")
        await channel.send(embed=embed, file=img_file, allowed_mentions=NO_MENTIONS)

    @client.event
    async def on_ready():
        await tree.sync()
        await client.change_presence(activity=discord.Game(name="Поднятие TTS с нуля"))
        for channel in config["discord"]["channels"]:
            CHANNEL_CACHE[channel] = client.get_partial_messageable(config["discord"]["channels"][channel])
        await REDIS_SUB.subscribe("byond.news")
        REDIS_SUB_BINDINGS["byond.news"] = publish_news
        announce_loop.start()
        announceloop_long.start()
        logging.info("Set up SS220 Manager")

    client.run(config["token"])


if __name__ == '__main__':
    run_bot()
