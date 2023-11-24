import discord
from discord import app_commands
from discord.ext import tasks

import random

# Includes a lot of other internal libs
from discord_helpers import *
from helpers import *

# Setting up config
with open("config.toml", "rb") as file:
    config = tomllib.load(file)

logging.basicConfig(level=config["log_level"], filename="logs/ss220.log", filemode="a+",
                    format="%(asctime)s %(levelname)s %(message)s", force=True)

HEAD_ADMIN_ROLES = config["discord"]["roles"]["heads"]
ADMIN_ROLES = [*HEAD_ADMIN_ROLES] + config["discord"]["roles"]["admins"]
MENTOR_ROLES = [*ADMIN_ROLES] + config["discord"]["roles"]["mentors"]
DEV_ROLES = config["discord"]["roles"]["devs"]
MISC_ROLES = config["discord"]["roles"]["servers"]

CODER_ID = config["discord"]["coder"]

global BAN_CHANNEL

OUR_SERVERS = load_servers_config(config)

server_choices = [app_commands.Choice(
    name=server.name, value=i) for i, server in enumerate(OUR_SERVERS)]

NO_MENTIONS = discord.AllowedMentions(roles=False, users=False, everyone=False)
last_status_sever = 0

# Setting up db connection
DB = Paradise(
    engine=config["db"]["SS13"]["type"],
    user=config["db"]["SS13"]["user"],
    password=config["db"]["SS13"]["password"],
    ip=config["db"]["SS13"]["ip"],
    port=config["db"]["SS13"]["port"],
    dbname=config["db"]["SS13"]["name"]
)


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
    async def online(interaction: discord.Interaction):
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
            pres = f'{OUR_SERVERS[last_status_sever].name}: {server_info.players_num} [{server_info.round_duration}]'
            await client.change_presence(activity=discord.Game(name=pres))
            last_status_sever += 1
            if last_status_sever > len(OUR_SERVERS) - 1:
                last_status_sever = 0

        except Exception as error:
            logging.error(error)

    # DATABASE

    @tree.command(name="я", description="Посмотреть информацию о себе.")
    async def me(interaction: discord.Interaction):
        await interaction.response.defer()
        player_info, discord_link_info = DB.get_player_by_discord(interaction.user.id)
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
            await BAN_CHANNEL.send(embed=embed)
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
        result = f"{interaction.user.display_name} \
        бросает {d} гранную кость на {action} \
        и выпадает {random.randint(1, d)}!"
        await interaction.followup.send(result)

    @client.event
    async def on_ready():
        await tree.sync()
        await client.change_presence(activity=discord.Game(name="Поднятие TTS с нуля"))
        global BAN_CHANNEL
        BAN_CHANNEL = client.get_partial_messageable(
            config["discord"]["channels"]["ban"])
        announce_loop.start()
        announceloop_long.start()
        logging.info("Set up SS220 Manager")

    client.run(config["token"])


if __name__ == '__main__':
    run_bot()
