import unittest

from common.helpers import build_changelog, parse_changelog
from common.discord_helpers import emojify_changelog

VALID_TAGS = {
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

EMOJIFIED_TAGS = {
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


class TestClHelpers(unittest.TestCase):

    def test_general(self):
        message = """
:cl: TestAuthor
add: Added new things
del: Removed old things
tweak: Tweaked a few things
fix: Fixed a few things
wip: Added a few works in progress
soundadd: Added a new sound thingy
sounddel: Removed an old sound thingy
imageadd: Added some icons and images
imagedel: Deleted some icons and images
spellcheck: Fixed a few typos
experiment: Added an experimental thingy
/:cl:
"""
        changelog = parse_changelog(message, VALID_TAGS)
        self.assertEqual(changelog["author"], "TestAuthor")
        self.assertIsNotNone(changelog["changes"])
        self.assertEqual(len(changelog["changes"]), 11)
        self.assertTrue(
            all(change["tag"] in VALID_TAGS for change in changelog["changes"]))
        self.assertTrue(all(not self.isBlank(
            change["message"]) for change in changelog["changes"]))

    def test_extended(self):
        message = """
:cl: TestAuthor1, TestAuthor2
add: Added new things
adds: Added new things
codeadd: Added new things
rscadd: Added new things
del: Removed old things
dels: Removed old things
codedel: Removed old things
rscdel: Removed old things
tweak: Tweaked a few things
tweaks: Tweaked a few things
rsctweak: Tweaked a few things
fix: Fixed a few things
fixes: Fixed a few things
bugfix: Fixed a few things
wip: Added a few works in progress
soundadd: Added a new sound thingy
sounddel: Removed an old sound thingy
imageadd: Added some icons and images
imagedel: Deleted some icons and images
typo: Fixed a few typos
spellcheck: Fixed a few typos
experiment: Added an experimental thingy
experimental: Added an experimental thingy
/:cl:
"""
        changelog = parse_changelog(message, VALID_TAGS)
        self.assertEqual("TestAuthor1, TestAuthor2", changelog["author"])
        self.assertIsNotNone(changelog["changes"])
        self.assertEqual(23, len(changelog["changes"]))
        self.assertTrue(
            all(change["tag"] in VALID_TAGS for change in changelog["changes"]))
        self.assertTrue(all(not self.isBlank(
            change["message"]) for change in changelog["changes"]))

    def test_no_author(self):
        message = """
:cl:
add: Added new things
/:cl:
"""
        changelog = parse_changelog(message, VALID_TAGS)
        self.assertIsNone(changelog["author"])

    def test_unicode(self):
        message = """
🆑 TestAuthor
add: Added new things
/🆑
"""
        changelog = parse_changelog(message, VALID_TAGS)
        self.assertEqual("TestAuthor", changelog["author"])

    def test_unicode_no_author(self):
        message = """
🆑
add: Added new things
/🆑
"""
        changelog = parse_changelog(message, VALID_TAGS)
        self.assertIsNone(changelog["author"])

    def test_indented_change(self):
        message = """
:cl:
    add: Added new things
/:cl:
"""
        changelog = parse_changelog(message, VALID_TAGS)
        self.assertIsNone(changelog["author"])
        self.assertIsNotNone(changelog["changes"])
        self.assertEqual(1, len(changelog["changes"]))
        self.assertEqual("codeadd", changelog["changes"][0]["tag"])
        self.assertEqual("Added new things",
                         changelog["changes"][0]["message"])

    def test_not_wrapped(self):
        message = """
:cl:
added: Added new things
"""
        with self.assertRaises(Exception):
            parse_changelog(message, VALID_TAGS)

    def test_no_changes(self):
        message = """
:cl:

/:cl:
"""
        with self.assertRaises(Exception):
            parse_changelog(message, VALID_TAGS)

    def test_invalid_tag(self):
        message = """
:cl:
added: Added new things
/:cl:
"""
        with self.assertRaises(Exception):
            parse_changelog(message, VALID_TAGS)

    def test_empty_message(self):
        message = (
            "\n:cl:\n"
            "\nadd: \n"
            "\n/:cl:"
            "\n"
        )
        with self.assertRaises(Exception):
            parse_changelog(message, VALID_TAGS)

    def test_link(self):
        message = """
:cl:
add: Added new things ([details](https://example.com)).
/:cl:
"""
        changelog = parse_changelog(message, VALID_TAGS)
        self.assertEqual(1, len(changelog["changes"]))
        self.assertEqual("codeadd", changelog["changes"][0]["tag"])
        self.assertEqual(
            "Added new things ([details](https://example.com)).", changelog["changes"][0]["message"])

    def test_comment(self):
        message = """
:cl:
add: Added new things. <!-- Comment. -->
/:cl:
"""
        changelog = parse_changelog(message, VALID_TAGS)
        self.assertEqual(1, len(changelog["changes"]))
        self.assertEqual("codeadd", changelog["changes"][0]["tag"])
        self.assertEqual("Added new things.",
                         changelog["changes"][0]["message"])

    def test_multiline(self):
        message = """
:cl:
add: Added new things.
Some more text.
fix: Fixed a few things
/:cl:
"""
        changelog = parse_changelog(message, VALID_TAGS)
        self.assertEqual(2, len(changelog["changes"]))
        self.assertEqual("codeadd", changelog["changes"][0]["tag"])
        self.assertEqual("fix", changelog["changes"][1]["tag"])
        self.assertEqual("Added new things. Some more text.",
                         changelog["changes"][0]["message"])

    def test_multiline_comment(self):
        message = """
:cl:
add: Added new things.
<!-- Comment. -->
fix: Fixed a few things.
<!-- Comment. -->
Not a comment.
<!--
fix: Commented change.
-->
/:cl:
"""
        changelog = parse_changelog(message, VALID_TAGS)
        self.assertEqual(2, len(changelog["changes"]))
        self.assertEqual("codeadd", changelog["changes"][0]["tag"])
        self.assertEqual("fix", changelog["changes"][1]["tag"])
        self.assertEqual("Added new things.",
                         changelog["changes"][0]["message"])
        self.assertEqual("Fixed a few things. Not a comment.",
                         changelog["changes"][1]["message"])

    def test_multiline_invalid(self):
        message = """
:cl:
Some more text.
add: Added new things.
/:cl:
"""
        with self.assertRaises(Exception):
            parse_changelog(message, VALID_TAGS)

    def test_emojify(self):
        changelog = {
            "author": "TestAuthor",
            "changes": [
                {"tag": "codeadd", "message": "Test message"},
                {"tag": "codedel", "message": "Test message"},
                {"tag": "imageadd", "message": "Test message"},
                {"tag": "imagedel", "message": "Test message"},
                {"tag": "soundadd", "message": "Test message"},
                {"tag": "sounddel", "message": "Test message"},
                {"tag": "tweak", "message": "Test message"},
                {"tag": "fix", "message": "Test message"},
                {"tag": "wip", "message": "Test message"},
                {"tag": "spellcheck", "message": "Test message"},
                {"tag": "experiment", "message": "Test message"}
            ]
        }
        expected_emoji = [
            ":sparkles:", ":wastebasket:", ":frame_photo:", ":scissors:", ":notes:", ":mute:",
            ":screwdriver:", ":tools:", ":construction_site:", ":pencil:", ":microscope:"
        ]
        modified_changelog = emojify_changelog(changelog, EMOJIFIED_TAGS)
        self.assertEqual(expected_emoji, list(
            map(lambda change: change["tag"], modified_changelog["changes"])))

    def test_emojify_invalid_tag(self):
        changelog = {
            "author": "TestAuthor",
            "changes": [{"tag": "add", "message": "Test message"}]
        }
        with self.assertRaises(Exception):
            emojify_changelog(changelog, EMOJIFIED_TAGS)

    def test_build_pr_labels(self):
        pr = {
            "user": {"login": "GitHubUser"},
            "labels": [{"name": ":page_with_curl: Требуется изменение WIKI"}],
            "body": """
:cl:
add: Added new things.
/:cl:
"""
        }
        changelog = build_changelog(pr, VALID_TAGS)
        self.assertEqual(1, len(changelog["labels"]))
        self.assertEqual(
            ":page_with_curl: Требуется изменение WIKI", changelog["labels"][0])

    def test_build_pr_author(self):
        pr = {
            "user": {"login": "GitHubUser"},
            "labels": [],
            "body": """
:cl:
add: Added new things.
/:cl:
"""
        }
        changelog = build_changelog(pr, VALID_TAGS)
        self.assertEqual("GitHubUser", changelog["author"])

    def test_build_cl_author(self):
        pr = {
            "user": {"login": "GitHubUser"},
            "labels": [],
            "body": """
:cl: TestAuthor
add: Added new things.
/:cl:
"""
        }
        changelog = build_changelog(pr, VALID_TAGS)
        self.assertEqual("TestAuthor", changelog["author"])

    def test_build_caret_return(self):
        pr = {
            "user": {"login": "GitHubUser"},
            "labels": [],
            "body": """
:cl: \r
add: Added new things.
/:cl:
"""
        }
        changelog = build_changelog(pr, VALID_TAGS)
        self.assertEqual("GitHubUser", changelog["author"])

    @staticmethod
    def isBlank(value: str):
        return not (value and value.strip())


if __name__ == '__main__':
    unittest.main()
