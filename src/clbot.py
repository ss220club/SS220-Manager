#  smee -u https://smee.io/oVNJrsT2LnrjP6ys -P /event-handler -p 5000
import logging
import os
import time

import requests
import datetime
import tomllib

from discord import Color
from github_bot_api import Event, Webhook
from github_bot_api.flask import create_flask_app

from db.db_base import SSDatabase
from db.connect import connect_database
from common.helpers import build_changelog
from common.discord_helpers import emojify_changelog


def create_discord_sender(build: str, cl_config: dict):
    def sender(cl, number, repo_url):
        return send_message(build, cl_config, cl, number, repo_url)
    return sender


os.makedirs("logs", exist_ok=True)
logging.basicConfig(level=logging.INFO, filename="logs/clbot.log", filemode="a+",
                    format="%(asctime)s %(levelname)s %(message)s")

config = {}
with open("config.toml", "rb") as file:
    config.update(tomllib.load(file))

WIKI_IDS = config["discord"]["mentions"]["wiki"]
CL_EMBED_COLOR = Color.from_str("#48739e")
WIKI_LABEL = ":page_with_curl: Требуется изменение WIKI"

MAX_DESCRIPTION_LENGTH = 4096

databases: dict[int, SSDatabase | None] = {
    cl_config["repo_id"]: connect_database(
        build, config["db"][build]) if build in config["db"] else None
    for build, cl_config in config["changelog"].items()
}
cl_configs = {
    cl_config["repo_id"]: cl_config
    for _, cl_config in config["changelog"].items()
}
discord_senders = {
    cl_config["repo_id"]: create_discord_sender(build, cl_config)
    for build, cl_config in config["changelog"].items()
}


def send_message(build: str, cl_config: dict, cl: dict, number: int, repo_url: str):
    cl_emoji = emojify_changelog(cl, cl_config["emojified_tags"])
    requires_wiki_update = WIKI_LABEL in cl["labels"]

    cl_fragments = []
    cl_fragment_builder = []
    current_length = 0

    for change in cl_emoji["changes"]:
        change_text = f"{change['tag']} {change['message']}"
        if current_length + len(change_text) + 1 > MAX_DESCRIPTION_LENGTH:
            cl_fragments.append("\n".join(cl_fragment_builder))
            cl_fragment_builder = []
            current_length = 0
        cl_fragment_builder.append(change_text)
        current_length += len(change_text) + 1

    if cl_fragment_builder:
        cl_fragments.append("\n".join(cl_fragment_builder))

    footer_text = f"{cl['author']} - {datetime.datetime.now().strftime('%H:%M %d.%m.%Y')}"
    headers = {"Content-Type": "application/json"}
    base_message_content = " ".join(
        [f"<@&{role}>" for role in WIKI_IDS]) if requires_wiki_update else ""

    for i, cl_fragment in enumerate(cl_fragments):
        data = {
            "username": f"{build.capitalize()} Changelog",
            "content": base_message_content if i == 0 else "",
            "allowed_mentions": {"parse": ["roles"]} if requires_wiki_update and i == 0 else {},
            "embeds": [
                {
                    "title": f"#{number}" if i == 0 else None,
                    "url": f"{repo_url}/pull/{number}" if i == 0 else None,
                    "color": CL_EMBED_COLOR.value,
                    "description": cl_fragment,
                    "footer": {"text": footer_text} if i == len(cl_fragments) - 1 else None,
                }
            ],
        }

        try:
            result = requests.post(
                cl_config["discord_webhook"], json=data, headers=headers)
            result.raise_for_status()
            logging.info("Message %d/%d sent successfully, code %d.",
                         i + 1, len(cl_fragments), result.status_code)
        except requests.exceptions.HTTPError as err:
            logging.error("Error sending message %d: %s\n%s",
                          i + 1, err, err.response.text)
            break

        # Add a delay between messages to ensure proper ordering and avoid rate limits
        time.sleep(2)


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
    if repo_id not in cl_configs or not cl_configs[repo_id]:
        logging.error(
            "No changelog config provided for repo - id: %s, url: %s", repo_id, repo['html_url'])
        return False
    try:
        changelog = build_changelog(pr, cl_configs[repo_id]["valid_tags"])
        logging.info(changelog)
    except Exception as e:
        logging.error("CL parsing error", e)
        return False

    try:
        if repo_id not in discord_senders or not discord_senders[repo_id]:
            logging.error(
                "No discord sender provided for repo - id: %s, url: %s", repo_id, repo['html_url'])
            return False
        discord_sender = discord_senders[repo_id]
        discord_sender(changelog, number, repo["html_url"])
    except Exception as e:
        logging.error("Discord error", e)
        return False

    try:
        if repo_id not in databases or not databases[repo_id]:
            logging.warning(
                "No database provided for repo - id: %s, url: %s", repo_id, repo['html_url'])
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
