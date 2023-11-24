#  smee -u https://smee.io/oVNJrsT2LnrjP6ys -P /event-handler -p 5000
import logging

logging.basicConfig(level=logging.INFO, filename="logs/clbot.log", filemode="a+",
                    format="%(asctime)s %(levelname)s %(message)s")

from github_bot_api import Event, Webhook
from github_bot_api.flask import create_flask_app
import os
import re
import requests
import datetime
from db_paradise import Paradise
import tomllib


CL_BODY = re.compile(r"(:cl:|🆑)(.+)?\n((.|\n|)+?)\n\/(:cl:|🆑)", re.MULTILINE)
CL_SPLIT = re.compile(r"(^\w+):\s+(\w.+)", re.MULTILINE)

WEBHOOK = "https://discord.com/api/webhooks/1129222640092057661/n0R0wwsAwf3aUkzt3v_T5408XLQjD7qRmNyQKtecRPExgjbwMb_4k9R87-Z3alZXAkZa"

CL_CHANGE = {"add": "codeadd",
             "del": "codedel"}
DISCORD_EMOJI = {
    "soundadd": ":notes:",
    "sounddel": ":mute:",
    "imageadd": ":frame_photo:",
    "imagedel": ":scissors:",
    "add": ":sparkles:",
    "del": ":wastebasket:",
    "tweak": ":screwdriver:",
    "fix": ":tools:",
    "wip": ":construction_site:",
    "spellcheck": ":pencil:",
    "experiment": ":microscope:"
}
config = {}
with open("config.toml", "rb") as file:
    config = tomllib.load(file)

DB = Paradise(
    engine=config["db"]["SS13"]["type"],
    user=config["db"]["SS13"]["user"],
    password=config["db"]["SS13"]["password"],
    ip=config["db"]["SS13"]["ip"],
    port=config["db"]["SS13"]["port"],
    dbname=config["db"]["SS13"]["name"]
)

def send_message(author: str, cl: tuple, number: int):
    data = {}
    data["username"] = "Paradise Changelog"
    data["embeds"] = []
    embed = {}
    embed["color"] = 16777215
    embed["description"] = ""
    for change in cl:
        change_type, change_text = change
        for cltp in DISCORD_EMOJI:
            change_type = change_type.replace(cltp, DISCORD_EMOJI[cltp])
        embed["description"] = embed["description"] + f"{change_type} {change_text}\n"
    footer = {}
    footer["text"] = f"#{number} - {author} - {datetime.datetime.now()}"
    embed["footer"] = footer
    data["embeds"].append(embed)
    result = requests.post(WEBHOOK, json=data, headers={"Content-Type": "application/json"})
    try:
        result.raise_for_status()
    except requests.exceptions.HTTPError as err:
        logging.error(err)
    else:
        logging.info(f"Payload delivered successfully, code {result.status_code}.")

def load_to_db(author: str, cl: tuple, number: int):
    with DB.Session() as session:
        for change in cl:
            cltype, clchange = change
            for cltp in CL_CHANGE:
                cltype = cltype.replace(cltp, CL_CHANGE[cltp])
            change_db = Paradise.Changelog(pr_number = number,
                                        author = author,
                                        cl_type = cltype,
                                        cl_entry = clchange
                                        )
            session.add(change_db)
        session.commit()



def on_any_event(event: Event) -> bool:
    match event.name:
        case "pull_request":
            return on_pr_event(event)


def on_pr_event(event: Event):
    data = event.payload
    if data["action"] != "closed":
        print("not closed pr")
        return True
    number = data["number"]
    pr = data["pull_request"]
    if pr["base"]["ref"] != "master":
        print("not master")
        return
    if not pr["merged"]:
        print("Not merged pr")
        return
    for label in pr["labels"]:
        if label["name"] == ":scroll: CL не требуется":
            print("CL ignore")
            return
    
    message = pr["body"]
    author = pr["user"]["login"]
    cl_list = ()
    try:
        cl = CL_BODY.search(message)
        cl_list = CL_SPLIT.findall(cl.group(3))
        logging.info(cl_list)
        author = author if cl.group(2) != "\r" and cl.group(2) != "\n" else author
    except Exception as e:
        logging.error("CL parsing error", e)
        return False
    try:
        send_message(author, cl_list, number)
    except Exception as e:
        logging.error("Discord error", e)
        return False
    try:
        load_to_db(author, cl_list, number)
    except Exception as e:
        logging.error("Database error", e)
        return False

def run_flask():
    webhook = Webhook(secret=config["gh_secret"])
    webhook.listen('*', on_any_event)

    os.environ['FLASK_ENV'] = 'development'
    flask_app = create_flask_app(__name__, webhook)
    flask_app.run()
    logging.info("Setted up flask app")


if __name__ == "__main__":
    run_flask()