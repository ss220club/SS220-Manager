#  smee -u https://smee.io/oVNJrsT2LnrjP6ys -P /event-handler -p 5000
import logging
from github_bot_api import Event, Webhook
from github_bot_api.flask import create_flask_app
import os
import requests
import datetime
from db_paradise import Paradise
from helpers import parse_changelog, emojify_changelog
import tomllib

logging.basicConfig(level=logging.INFO, filename="logs/clbot.log", filemode="a+",
                    format="%(asctime)s %(levelname)s %(message)s")

WEBHOOK = "https://discord.com/api/webhooks/1129222640092057661/n0R0wwsAwf3aUkzt3v_T5408XLQjD7qRmNyQKtecRPExgjbwMb_4k9R87-Z3alZXAkZa"

config = {}
with open("config.toml", "rb") as file:
    config.update(tomllib.load(file))

DB = Paradise(
    engine=config["db"]["SS13"]["type"],
    user=config["db"]["SS13"]["user"],
    password=config["db"]["SS13"]["password"],
    ip=config["db"]["SS13"]["ip"],
    port=config["db"]["SS13"]["port"],
    dbname=config["db"]["SS13"]["name"]
)


def send_message(cl: dict, number: int):
    data = {"username": "Paradise Changelog", "embeds": []}
    embed = {"color": 16777215, "description": ""}
    cl_emoji = emojify_changelog(cl)
    for change in cl_emoji["changes"]:
        embed["description"] += f"{change["tag"]} {change["message"]}\n"
    footer = {"text": f"#{number} - {cl["author"]} - {datetime.datetime.now()}"}
    embed["footer"] = footer
    data["embeds"].append(embed)
    result = requests.post(WEBHOOK, json=data, headers={"Content-Type": "application/json"})
    try:
        result.raise_for_status()
    except requests.exceptions.HTTPError as err:
        logging.error(err)
    else:
        logging.info(f"Payload delivered successfully, code {result.status_code}.")


def load_to_db(cl: dict, number: int):
    with DB.Session() as session:
        for change in cl:
            change_db = Paradise.Changelog(
                pr_number=number,
                author=cl["author"],
                cl_type=change["tag"],
                cl_entry=change["message"]
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
    try:
        changelog = parse_changelog(message)
        changelog["author"] = changelog["author"] or author
        logging.info(changelog)
    except Exception as e:
        logging.error("CL parsing error", e)
        return False
    try:
        send_message(changelog, number)
    except Exception as e:
        logging.error("Discord error", e)
        return False
    try:
        load_to_db(changelog, number)
    except Exception as e:
        logging.error("Database error", e)
        return False


def run_flask():
    webhook = Webhook(secret=config["gh_secret"])
    webhook.listen('*', on_any_event)

    os.environ['FLASK_ENV'] = 'development'
    flask_app = create_flask_app(__name__, webhook)
    flask_app.run()
    logging.info("Set up flask app")


if __name__ == "__main__":
    run_flask()
