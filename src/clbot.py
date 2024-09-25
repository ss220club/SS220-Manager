#  smee -u https://smee.io/oVNJrsT2LnrjP6ys -P /event-handler -p 5000
import logging
from github_bot_api import Event, Webhook
from github_bot_api.flask import create_flask_app
import os
import requests
import datetime
import tomllib

from db.db_base import SSDatabase
from db.connect import connect_database
from common.helpers import build_changelog
from common.discord_helpers import emojify_changelog

os.makedirs("logs", exist_ok=True)
logging.basicConfig(level=logging.INFO, filename="logs/clbot.log", filemode="a+",
                    format="%(asctime)s %(levelname)s %(message)s")

config = {}
with open("config.toml", "rb") as file:
    config.update(tomllib.load(file))

databases: dict[int, SSDatabase] = {
    build["repo_id"]: connect_database(build["build"], config["db"][build["build"]]) for build in config["changelog"]
}
discord_senders = {
    build["repo_id"]: lambda cl, number, repo_url: send_message(build, cl, number, repo_url)
    for build in config["changelog"]
}


def send_message(cl_config: dict, cl: dict, number: int, repo_url: str):
    data = {"username": f"{cl_config['build'].capitalize()} Changelog", "embeds": []}
    embed = {"color": 16777215, "title": f"#{number}", "description": ""}
    cl_emoji = emojify_changelog(cl)
    for change in cl_emoji["changes"]:
        embed["description"] += f"{change['tag']} {change['message']}\n"
    footer = {"text": f"{cl['author']} - {datetime.datetime.now().strftime('%H:%M %d.%m.%Y')}"}
    embed["footer"] = footer
    embed["url"] = f"{repo_url}/pull/{number}"
    data["embeds"].append(embed)
    result = requests.post(cl_config["discord_webhook"], json=data, headers={"Content-Type": "application/json"})
    try:
        result.raise_for_status()
    except requests.exceptions.HTTPError as err:
        logging.error(err)
    else:
        logging.info(f"Payload delivered successfully, code {result.status_code}.")


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
    repo = data["repository"]
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

    repo_id = repo["id"]
    try:
        changelog = build_changelog(pr)
        logging.info(changelog)
    except Exception as e:
        logging.error("CL parsing error", e)
        return False

    try:
        if repo_id not in discord_senders or not discord_senders[repo_id]:
            logging.error(f"No discord sender provided for repo - id: {repo_id}, url: {repo['html_url']}")
            return False
        discord_sender = discord_senders[repo_id]
        discord_sender(changelog, number, repo["html_url"])
    except Exception as e:
        logging.error("Discord error", e)
        return False

    try:
        if repo_id not in databases or not databases[repo_id]:
            logging.error(f"No database provided for repo - id: {repo_id}, url: {repo['html_url']}")
            return False
        db = databases[repo_id]
        db.push_changelog(changelog, number)
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
