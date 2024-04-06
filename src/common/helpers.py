import types
import re
from typing import Literal

ALL_PLAYABLE_SPECIES = Literal[
    "Human", "Diona", "Drask",
    "Grey", "Kidan", "Machine",
    "Nian", "Plasmaman", "Skrell",
    "Slime People", "Tajaran",
    "Unathi", "Vox", "Vulpkanin"
]

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


def build_changelog(pr: dict) -> dict:
    changelog = parse_changelog(pr["body"])
    changelog["author"] = changelog["author"] or pr["user"]["login"]
    return changelog


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
                prev_change["message"] += f" {change_parse_result['message']}"
            else:
                raise Exception(f"Change with no tag: {cl_line}")

    if len(cl_changes) == 0:
        raise Exception("No changes found in the changelog. Use special label if changelog is not expected.")
    return {
        "author": str.strip(cl_parse_result.group("author") or "") or None,  # I want this to be None, not empty
        "changes": cl_changes
    }


def sanitize_ckey(ckey: str) -> str:
    return ''.join(char for char in ckey if char.isalnum()).lower() if ckey else ""


ERRORS = types.SimpleNamespace()
ERRORS.ERR_BOUND = "err_bound"
ERRORS.ERR_404 = "err_404"
