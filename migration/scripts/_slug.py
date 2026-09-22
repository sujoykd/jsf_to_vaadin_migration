#!/usr/bin/env python3
"""Canonical view-slug / screenshot-filename rules — one definition, reused everywhere.

The slug is the stable identity of a view across the pipeline (screenshots, spec
filenames, issue keys). Keeping the rule here means jsf-screenshot-tour,
vaadin-migration-verify and the spec scripts can't drift apart.

Rule: take the URL path, drop a leading '/', drop the '.xhtml' suffix, replace each
'/' with '__'. For an id-bearing view append '__id<n>'.

  secured/registration/card/detailCard.xhtml?id=7  ->  secured__registration__card__detailCard__id7

CLI:
  _slug.py <url-or-path> [id]      print the slug
  _slug.py --png <url-or-path> [id]  print "<slug>.png"

Pure stdlib; importable (`from _slug import slug, screenshot_name`).
"""
import re
import sys
import urllib.parse


def slug(url, view_id=None):
    """Slug for a view URL/path (query string and host are ignored)."""
    path = urllib.parse.urlsplit(url).path or url
    path = path.split("?", 1)[0]
    path = path.lstrip("/")
    if path.endswith(".xhtml"):
        path = path[: -len(".xhtml")]
    s = path.replace("/", "__")
    if view_id not in (None, "", []):
        s += f"__id{view_id}"
    return s


def screenshot_name(url, view_id=None):
    """PNG filename for a view's baseline screenshot."""
    return slug(url, view_id) + ".png"


def _id_in_url(url):
    q = urllib.parse.urlsplit(url).query
    if not q:
        return None
    vals = urllib.parse.parse_qs(q).get("id")
    return vals[0] if vals else None


def main(argv):
    png = False
    args = argv[1:]
    if args and args[0] == "--png":
        png, args = True, args[1:]
    if not args:
        sys.stderr.write("usage: _slug.py [--png] <url-or-path> [id]\n")
        return 2
    url = args[0]
    view_id = args[1] if len(args) > 1 else _id_in_url(url)
    print(screenshot_name(url, view_id) if png else slug(url, view_id))
    return 0


if __name__ == "__main__":
    raise SystemExit(main(sys.argv))
