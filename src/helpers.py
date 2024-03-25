import types
import re
import copy
from typing import Literal

DEPARTMENT_TRANSLATIONS = {"├Призраком": "Ghost",
                      "└Живым": "Living",
                      " ├Спец роли": "Special",
                      " └Экипаж": "Crew",
                      "ᅟ├Команд.": "Command",
                      "ᅟ├Инженеры": "Engineering",
                      "ᅟ├Медики": "Medical",
                      "ᅟ├Учёные": "Science",
                      "ᅟ├Снабжение": "Supply",
                      "ᅟ├СБ": "Security",
                      "ᅟ├Сервис": "Service",
                           # "ᅟ├ВЛ": "Whitelist",
                           "ᅟ└Синты": "Silicon",
                           }

SERVERS_NICE = {"136.243.82.223:4002": ["Main", "https://cdn.discordapp.com/emojis/1098305756836663379.webp?size=64"],
                "141.95.72.94:4002": ["Green", "https://cdn.discordapp.com/emojis/1098305756836663379.webp?size=64"],
                "135.125.189.154:4001": ["Prime", "https://cdn.discordapp.com/emojis/1100109697744371852.webp?size=64"],
                "135.125.189.154:4000": ["Black", "https://cdn.discordapp.com/emojis/1098305756836663379.webp?size=64"]
                }  # TODO: To config

ALL_PLAYABLE_SPECIES = Literal["Human", "Diona", "Drask",
                                "Grey", "Kidan", "Machine", 
                                "Nian", "Plasmaman", "Skrell",
                                "Slime People", "Tajaran", 
                                "Unathi", "Vox", "Vulpkanin"]

CL_BODY = re.compile(r"(:cl:|🆑)[ \t]*(?P<author>.+?)?\s*\n(?P<content>(.|\n)*?)\n/(:cl:|🆑)", re.MULTILINE)
CL_SPLIT = re.compile(r"\s*((?P<tag>\w+)\s*:)?\s*(?P<message>.*)")

CL_NORMALIZED_TAG = {
    "fix": "fix",
    "fixes": "fix",
    "bugfix": "fix",
    "wip": "wip",
    "tweak": "tweak",
    "tweaks": "tweak",
    "rsctweak": "tweak",
    "soundadd": "soundadd",
    "sounddel": "sounddel",
    "imageadd": "imageadd",
    "imagedel": "imagedel",
    "add": "codeadd",
    "adds": "codeadd",
    "rscadd": "codeadd",
    "codeadd": "codeadd",
    "del": "codedel",
    "dels": "codedel",
    "rscdel": "codedel",
    "codedel": "codedel",
    "typo": "spellcheck",
    "spellcheck": "spellcheck",
    "experimental": "experiment",
    "experiment": "experiment"
}
DISCORD_TAG_EMOJI = {
    "soundadd": ":notes:",
    "sounddel": ":mute:",
    "imageadd": ":frame_photo:",
    "imagedel": ":scissors:",
    "codeadd": ":sparkles:",
    "codedel": ":wastebasket:",
    "tweak": ":screwdriver:",
    "fix": ":tools:",
    "wip": ":construction_site:",
    "spellcheck": ":pencil:",
    "experiment": ":microscope:"
}


def parse_changelog(message: str) -> dict:
    cl_parse_result = CL_BODY.search(message)
    if cl_parse_result is None:
        raise Exception("Failed to parse the changelog. Check changelog format.")
    cl_changes = []
    for cl_line in cl_parse_result.group("content").splitlines():
        if not cl_line:
            continue
        change_parse_result = CL_SPLIT.search(cl_line)
        if not change_parse_result:
            raise Exception(f"Invalid change: '{cl_line}'")
        tag = change_parse_result["tag"]
        message = change_parse_result["message"]
        if not message:
            raise Exception(f"No message for change: '{cl_line}'")
        if tag:
            if tag in CL_NORMALIZED_TAG:
                cl_changes.append({
                    "tag": CL_NORMALIZED_TAG[change_parse_result.group("tag")],
                    "message": change_parse_result.group("message")
                })
            else:
                raise Exception(f"Invalid tag: '{cl_line}'")
        # Append line without tag to the previous change
        else:
            if len(cl_changes):
                prev_change = cl_changes[-1]
                prev_change["message"] += f" {change_parse_result["message"]}"
            else:
                raise Exception(f"Change with no tag: {cl_line}")

    if len(cl_changes) == 0:
        raise Exception("No changes found in the changelog. Use special label if changelog is not expected.")
    return {"author": cl_parse_result.group("author"), "changes": cl_changes}


def emojify_changelog(changelog: dict):
    changelog_copy = copy.deepcopy(changelog)
    for change in changelog_copy["changes"]:
        if change["tag"] in DISCORD_TAG_EMOJI:
            change["tag"] = DISCORD_TAG_EMOJI[change["tag"]]
        else:
            raise Exception(f"Invalid tag for emoji: {change}")
    return changelog_copy


def gender_to_emoji(gender: str) -> str:
    match gender:
        case "male":
            return ":male_sign:"
        case "female":
            return ":female_sign:"
        case "plural":
            return ":regional_indicator_p:"
        case _:
            return ":helicopter:"


def sanitize_ckey(ckey: str) -> str:
    return ''.join(char for char in ckey if char.isalnum()).lower() if ckey else ""


ERRORS = types.SimpleNamespace()
ERRORS.ERR_BOUND = "err_bound"
ERRORS.ERR_404 = "err_404"