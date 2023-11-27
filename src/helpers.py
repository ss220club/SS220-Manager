import types
import string

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