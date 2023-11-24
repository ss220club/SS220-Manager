from discord import Embed
from discord import Colour as Color
from datetime import timedelta

from db_paradise import *
from API import *

BYOND_ICON = "<:byond:1109845921904205874>"
SS14_ICON = "<:ss14:1109845956561735730>"


def embed_player_info(player: Paradise.Player, discord_link: Paradise.DiscordLink, chars: Sequence[Paradise.Character]):
    if not player:
        return Embed(
            title=f"Дискорд игрока не привязан к игре.",
            color=Color.red())
    embed = Embed(
        title=f"Информация об игроке {player.ckey}",
        description=
        f"**Дискорд:** <@{discord_link.discord_id}>\
        \n**Ранг:** {player.lastadminrank}\
        \n**Стаж:** {player.lastseen - player.firstseen}\
        \n**Первое появление:** {player.firstseen}\
        \n**Последнее появление: **{player.lastseen}",
        color=Color.blue()
        )

    # TODO
    # if player.mutual_ips or player.mutual_cids:
    #     embed.add_field(name="**Совпадения:**",
    #                     value=f"\
    #                     **IP:** {', '.join(player.mutual_ips)}\n\
    #                     **CID:** {', '.join(player.mutual_cids)}",
    #                     inline=False)
    #     embed.color = Color.dark_orange()

    if player.exp:
        exp = parse_player_exp(player)
        embed.add_field(
            name=f"Время игры: {round((int(exp['Living']) + int(exp['Ghost'])) / 60, 2)} ч.",
            value=get_nice_exp(exp),
            inline=True)
    if chars:
        embed.add_field(name="Персонажи",
                        value=get_nice_player_chars(chars), inline=True)
    embed.set_footer(
        text=f"Новый смешной футер для новой крутой версии бота. Все еще могут быть косяки.")

    return embed


def get_nice_player_chars(chars: Sequence[Paradise.Character]):
    res = ''
    for char in chars:
        res += f"`{char.slot}:` **{char.real_name}**\n{gender_to_emoji(char.gender)} {char.species} {char.age} лет\n"
    return res


def parse_player_exp(player: Paradise.Player):
    exp = player.exp
    departments = exp.split("&")
    result = {}
    for departments_exp_tuple in departments:
        dep, xp = departments_exp_tuple.split("=")
        result[dep] = xp
    return result


def get_nice_exp(exp: dict):
    res = ""
    for departament in DEPARTMENT_TRANSLATIONS:
        res += f"{departament}: {round(int(exp[DEPARTMENT_TRANSLATIONS[departament]]) / 60, 2)} ч.\n"
    return res


def get_nice_bans(bans: Sequence[Paradise.Ban]) -> list[Embed]:
    embeds = []
    if not bans:
        return []
    bans = convert_bans(bans)
    for ban in bans:
        match ban.bantype:
            case "PERMABAN":
                ban_color = Color.red()
                ban.bantype = "Пермабан"
            case "JOB_PERMABAN":
                ban_color = Color.purple()
                ban.bantype = "Пермаджоббан"
            case "TEMPBAN":
                ban_color = Color.orange()
                ban.bantype = "Темпбан"
            case "JOB_TEMPBAN":
                ban_color = Color.dark_blue()
                ban.bantype = "Джоббан"
            case "BAN":
                ban_color = Color.dark_red()
            case _:
                ban_color = Color.light_embed()
        if ban.unbanned or (ban.duration > 0 and ban.expiration_time < datetime.now()):
            ban_color = Color.green()
        embed = (Embed(title=f"**{ban.bantype} {ban.id}**",
                       description=
                       f"**Игрок:** {ban.ckey}\
                       \n**Админ:** {ban.a_ckey}\
                       \n**Длительность:** {str(ban.duration // 60)} ч. до {str(ban.expiration_time) if ban.duration > 0 else 'Навсегда'}\
                       \n**Причина:** {ban.reason}\
                       \n {'**Роль:**' + ban.job if ban.job else ''}",
                       color=ban_color
                       )
                )
        embed.set_footer(
            text=f"{SERVERS_NICE[ban.serverip][0]} - {ban.expiration_time - timedelta(minutes=ban.duration)}",
            icon_url=SERVERS_NICE[ban.serverip][1])
        embeds.append(embed)
    return embeds


def convert_bans(data: Sequence[Paradise.Ban]) -> Sequence[Paradise.Ban]:
    bans = list[Paradise.Ban]()
    for ban in data:
        sameban = False
        for nice_ban in bans:
            if ban.reason == nice_ban.reason and ban.duration == nice_ban.duration and (
                    ban.bantype == "JOB_TEMPBAN" or ban.bantype == "JOB_PERMABAN"):
                nice_ban.job = nice_ban.job + ", " + ban.job
                nice_ban.id = ban.id
                sameban = True
        if not sameban:
            bans.append(ban)
    return bans


def embed_notes(notes: Sequence[Paradise.Note]):
    embeds = []
    for note in notes:
        embed = Embed(title="Нотес",
                      description=f"**Игрок:** {note.ckey}\n**Админ:** {note.adminckey}\n**Текст:** {note.notetext}",
                      color=Color.light_embed())
        embed.set_footer(text=f"{note.server} - {note.timestamp}")
        embeds.append(embed)
    return embeds


def get_beautified_status(servers: list[Server]) -> str:
    res = ''
    for server in servers:
        server_info = server.get_server_status()
        res += f'{BYOND_ICON if server.build == "paradise" else SS14_ICON} \
        **{server.name}**: {server_info.players_num} [{server_info.round_duration}]  {server_info.admins_num} A/M\n'
    return res


def get_admins(servers: list[Server]) -> str:
    res = ''
    for server in servers:
        res += f"**{server.name}:**\n"
        admins = server.get_admin_who().admins
        if not admins:
            continue
        for admin in admins:
            if admin.get("stealth") != "STEALTH":
                res += f'  {admin["ckey"]} - {admin["rank"]}\n'
    return res


def get_players_online(server: Server) -> str:
    players_list = server.get_players_list()
    if not players_list.is_online:
        return f'**{server.name}:** OFFLINE'
    players_list = players_list.players
    res = f"**{server.name}:** {', '.join(players_list)}"
    return res
