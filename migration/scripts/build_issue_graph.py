#!/usr/bin/env python3
"""Build the migration issue graph (foundation + epics + per-view) with dependencies.

Tracker-agnostic. Reads the view inventory (from enumerate_views.py) and, when present,
per-view behavior specs (migration/specs/<slug>.json), and emits:
  <out>/graph.json          the issues + dependency edges + topological order
  <out>/drafts/NNN-<slug>.md a ready-to-read issue body per issue

The adapter (e.g. forgejo_issues.sh) consumes graph.json to create real issues.

Dependency rules:
  - foundation blocks every issue
  - login (public 'index' view) blocks every secured view; dashboard depends on login
  - within a view folder, the list* view blocks that folder's form*/detail*/statistics/balance views
  - each module epic depends on foundation; each view issue belongs to its module epic

Usage:
    build_issue_graph.py --views <enumerate.json> [--specs <dir>] [--out <dir>]

`--views` is the JSON array produced by jsf-screenshot-tour/scripts/enumerate_views.py
(items: {url, file, kind, needs_id}). `--out` defaults to migration/issues.

Pure stdlib.
"""
import argparse
import json
import os
import re
import sys
import uuid

# Fixed namespace so each issue's UUID is DETERMINISTIC from its stable key — regenerating
# the graph yields the same UUIDs, which is what lets adapters dedupe against the tracker.
UUID_NAMESPACE = uuid.UUID("a3f1c2e4-5b6d-47a8-9c0e-1f2a3b4c5d6e")


def issue_uuid(key):
    return str(uuid.uuid5(UUID_NAMESPACE, key))


def slug_of(view_file):
    """secured/registration/wallet/formWallet.xhtml -> secured__registration__wallet__formWallet"""
    return view_file[:-len(".xhtml")].replace("/", "__") if view_file.endswith(".xhtml") \
        else view_file.replace("/", "__")


def module_of(view_file):
    parts = view_file.split("/")
    if parts[0] == "secured" and len(parts) > 2:
        return parts[1]
    if view_file.startswith("error/"):
        return "core"
    return "core"  # index.xhtml, dashboard, etc.


def folder_of(view_file):
    return "/".join(view_file.split("/")[:-1])


# Common auth/layout wrapper segments to strip when deriving the feature (generic — not
# tied to any one app's folder names).
WRAPPER_SEGMENTS = {"secured", "protected", "app", "pages", "views", "main", "private"}


def feature_of(view_file):
    """The entity-feature a view belongs to, derived from its folder (app-agnostic).
    e.g. secured/registration/wallet/formWallet.xhtml -> 'registration/wallet'.
    Returns None for top-level/non-feature views (login, dashboard, error pages)."""
    parts = view_file.split("/")[:-1]  # drop filename
    while parts and parts[0] in WRAPPER_SEGMENTS:
        parts = parts[1:]
    # need at least <module>/<entity> to be a real feature
    return "/".join(parts) if len(parts) >= 2 else None


def leaf(view_file):
    return view_file.split("/")[-1][:-len(".xhtml")]


def kind_of(item):
    """Normalize enumerate_views kinds into issue kinds."""
    k = item.get("kind", "other")
    f = leaf(item["file"]).lower()
    if item["file"] == "index.xhtml":
        return "login"
    if k == "error":
        return "error"
    if f == "dashboard":
        return "dashboard"
    if f.startswith("list"):
        return "list"
    if f.startswith("form"):
        return "form"
    if f.startswith("detail") or f.endswith("statistics") or f.startswith("balance") or item.get("needs_id"):
        return "detail"
    return "other"


def proposed_route(view_file, kind):
    """A clean Vaadin @Route suggestion."""
    if kind == "login":
        return "login"
    if kind == "dashboard":
        return "dashboard"
    base = leaf(view_file)
    entity = re.sub(r"^(list|form|detail|balance)", "", base)
    entity = re.sub(r"Statistics$", "", entity)
    entity = (entity[0].lower() + entity[1:]) if entity else base.lower()
    # pluralize-ish for lists
    if kind == "list":
        return entity.lower()
    if kind == "form":
        return f"{entity.lower()}/edit"
    if kind == "detail":
        return f"{entity.lower()}/:id"
    return entity.lower()


def load_spec(specs_dir, slug):
    if not specs_dir:
        return None
    p = os.path.join(specs_dir, slug + ".json")
    if os.path.isfile(p):
        try:
            return json.load(open(p))
        except (OSError, ValueError):
            return None
    return None


def load_architecture(path):
    """The decided target architecture (migration/architecture.json from migration-architecture)."""
    if not path or not os.path.isfile(path):
        return None
    try:
        return json.load(open(path))
    except (OSError, ValueError):
        return None


def render_architecture(arch):
    """Markdown summary of architecture.json for the foundation ticket body."""
    if not arch:
        return ["_No `migration/architecture.json` found — run `/migration-architecture` to decide "
                "authentication, theme, layout, etc. The scaffold will otherwise use defaults._", ""]
    lines = ["The foundation must scaffold the app per these decided choices "
             "(`migration/architecture.json`):", ""]
    for category in sorted(arch):
        vals = arch[category]
        if not isinstance(vals, dict):
            lines.append(f"- **{category}**: {vals}")
            continue
        parts = [f"{k}={v}" for k, v in vals.items()
                 if k != "rationale" and v not in (None, "", [], {})]
        line = f"- **{category}**: " + ", ".join(parts)
        if vals.get("rationale"):
            line += f"  — _{vals['rationale']}_"
        lines.append(line)
    lines.append("")
    return lines


def acceptance_criteria(kind, spec):
    ac = []
    if kind == "port":
        return [
            "Entity compiles under jakarta.persistence with Bean Validation + Envers intact",
            "Repository extends JpaRepository (+ JpaSpecificationExecutor); derived finders preserved",
            "Criteria/getRestrictions filtering reimplemented as Spring Data Specifications",
            "Service is a @Service with constructor injection and Spring @Transactional",
            "Business logics injected as List<T>/ObjectProvider<T>; events via @EventListener",
            "Feature compiles in migration/vaadin and loads in the Spring context",
        ]
    if kind == "list":
        ac += ["Grid shows all columns from the original list (header, sort, filter as applicable)",
               "Lazy/paged data loading is wired to the repository",
               "Row actions (edit/detail/delete) navigate to the correct routes",
               "Global 'Add' action navigates to the new-record form"]
    elif kind == "form":
        ac += ["All form fields are present with the correct Vaadin component",
               "Field validation matches the entity Bean Validation + xhtml required flags",
               "Save/Update calls the correct service method and navigates back on success",
               "Cancel/Back returns to the list without saving"]
    elif kind == "detail":
        ac += ["All fields render read-only, loaded by the route id parameter",
               "Navigation back to the list works"]
    elif kind == "login":
        ac += ["Login authenticates against the ported security layer",
               "Successful login redirects to the dashboard; failure shows an error"]
    elif kind == "dashboard":
        ac += ["Dashboard widgets/data render via the ported services"]
    elif kind == "error":
        ac += ["Error page renders with the correct status and message"]
    else:
        ac += ["View renders and its primary interactions work"]
    ac += ["Written test-first (TDD): browserless Flow tests derived from this spec fail first, "
           "then pass",
           "View + menu entry are gated by the original permission",
           "Labels resolve via i18n (correct language)",
           "Visual parity with the baseline screenshot (Lumo theme differences allowed)",
           "App builds and the view passes a manual smoke test"]
    if spec and spec.get("services"):
        ac.insert(0, "Service port complete for this feature (check_service_port.py passes / "
                     "the blocked-by [port] issue is done)")
        for s in spec["services"]:
            methods = ", ".join(str(m) for m in (s.get("methods") or []))
            ac.append(f"Calls `{s.get('type') or '?'}` methods: {methods}")
    return ac


def render_what(kind, item, spec, route):
    lines = [f"- **View:** `{item['file']}`",
             f"- **Kind:** {kind}",
             f"- **Proposed Vaadin route:** `{route}`"]
    if spec:
        if spec.get("title"):
            lines.append(f"- **Title:** {spec['title']}")
        form = spec.get("form") or {}
        if form.get("fields"):
            lines.append("\n**Fields**\n")
            lines.append("| Label | Component | Required | Validation |")
            lines.append("|-------|-----------|----------|------------|")
            for fld in form["fields"]:
                lines.append("| {} | {} | {} | {} |".format(
                    fld.get("label") or "", fld.get("component") or "",
                    "yes" if fld.get("required") else "no",
                    ", ".join(str(v) for v in (fld.get("validation") or []))))
        lst = spec.get("list") or {}
        if lst.get("columns"):
            cols = ", ".join(str(c.get("header") or "?") for c in lst["columns"])
            lines.append(f"\n**List columns:** {cols}")
        if spec.get("actions"):
            lines.append("\n**Actions → service calls**\n")
            for a in spec["actions"]:
                call = a.get("serviceCall") or "—"
                nxt = a.get("then") or ""
                lines.append(f"- **{a.get('label') or '?'}** → `{call}`" + (f" → {nxt}" if nxt else ""))
        if spec.get("permissions"):
            perms = ", ".join(str(p) for p in spec["permissions"] if p)
            lines.append(f"\n**Permissions:** {perms}")
        if spec.get("i18nKeys"):
            keys = [str(k) for k in spec["i18nKeys"] if k]
            lines.append(f"\n**i18n keys:** {', '.join(keys[:12])}"
                         + (" …" if len(keys) > 12 else ""))
    else:
        lines.append("\n_No behavior spec found — run `jsf-view-analyze` on this view to enrich this "
                     "issue with fields, actions→service calls, validations, and permissions._")
    return "\n".join(lines)


def main(argv):
    ap = argparse.ArgumentParser()
    ap.add_argument("--views", required=True, help="enumerate_views.py JSON output")
    ap.add_argument("--specs", default=None, help="dir with <slug>.json behavior specs")
    ap.add_argument("--out", default="migration/issues")
    ap.add_argument("--screenshots", default="screenshots", help="screenshots base dir for embeds")
    ap.add_argument("--architecture", default="migration/architecture.json",
                    help="decided architecture (migration-architecture); embedded in the foundation ticket")
    ap.add_argument("--no-port-issues", action="store_true",
                    help="do not generate per-feature service-layer port issues")
    args = ap.parse_args(argv[1:])

    try:
        views = json.load(open(args.views))
    except (OSError, ValueError) as e:
        sys.stderr.write(f"error reading --views: {e}\n")
        return 1

    drafts_dir = os.path.join(args.out, "drafts")
    os.makedirs(drafts_dir, exist_ok=True)

    issues = []         # each: {key, kind, title, module, view, slug, route, labels, milestone, dependsOn:[keys], epic:key}
    key_index = {}

    def add(issue):
        key_index[issue["key"]] = issue
        issues.append(issue)

    # 1) foundation
    add({"key": "foundation", "kind": "foundation",
         "title": "[foundation] Scaffold Vaadin 25 app + port service layer",
         "module": "core", "view": None, "slug": "foundation",
         "route": None, "labels": ["migration", "kind:foundation"],
         "milestone": "core", "dependsOn": [], "epic": None})

    # 2) module epics (discover modules from views)
    modules = []
    for it in views:
        m = module_of(it["file"])
        if m not in modules:
            modules.append(m)
    for m in modules:
        add({"key": f"epic:{m}", "kind": "epic",
             "title": f"[{m}] Epic: migrate module to Vaadin",
             "module": m, "view": None, "slug": f"epic-{m}",
             "route": None, "labels": ["migration", "epic", f"module:{m}"],
             "milestone": m, "dependsOn": ["foundation"], "epic": None})

    # 3) per-view issues
    login_key = None
    list_by_folder = {}
    view_issues = []
    for it in views:
        vf = it["file"]
        kind = kind_of(it)
        m = module_of(vf)
        slug = slug_of(vf)
        route = proposed_route(vf, kind)
        key = f"view:{slug}"
        issue = {"key": key, "kind": kind,
                 "title": f"[{m}] {kind}: {route} ({leaf(vf)})",
                 "module": m, "view": vf, "slug": slug, "route": route,
                 "labels": ["migration", f"module:{m}", f"kind:{kind}"],
                 "milestone": m, "dependsOn": ["foundation"], "epic": f"epic:{m}"}
        add(issue)
        view_issues.append(issue)
        if kind == "login":
            login_key = key
        if kind == "list":
            list_by_folder[folder_of(vf)] = key

    # 3b) per-feature service-layer port issues (+ link views to them)
    if not args.no_port_issues:
        features = []
        for it in views:
            f = feature_of(it["file"])
            if f and f not in features:
                features.append(f)
        if features:
            add({"key": "epic:port", "kind": "epic",
                 "title": "[port] Epic: port service layer to Spring",
                 "module": "port", "view": None, "slug": "epic-port",
                 "route": None, "labels": ["migration", "epic", "service-port"],
                 "milestone": "port", "dependsOn": ["foundation"], "epic": None})
            for f in features:
                fmod = f.split("/")[0]
                add({"key": f"port:{f}", "kind": "port",
                     "title": f"[port] {f}: entity/repo/service/logics -> Spring",
                     "module": fmod, "view": None, "slug": "port-" + f.replace("/", "__"),
                     "route": None, "labels": ["migration", "kind:port", f"module:{fmod}"],
                     "milestone": "port", "dependsOn": ["foundation"],
                     "epic": "epic:port", "feature": f})
            # each view depends on its feature's port issue (UI needs the backend ported)
            for issue in view_issues:
                f = feature_of(issue["view"])
                if f and f"port:{f}" in key_index:
                    issue["dependsOn"].append(f"port:{f}")

    # 4) dependency edges among views
    for issue in view_issues:
        if issue["kind"] == "login":
            continue
        # everything secured depends on login
        if login_key and issue["view"] != "index.xhtml" and not issue["view"].startswith("error/"):
            issue["dependsOn"].append(login_key)
        # form/detail depend on their folder's list
        if issue["kind"] in ("form", "detail"):
            lk = list_by_folder.get(folder_of(issue["view"]))
            if lk and lk != issue["key"]:
                issue["dependsOn"].append(lk)

    # de-dup dependsOn
    for issue in issues:
        seen, uniq = set(), []
        for d in issue["dependsOn"]:
            if d not in seen and d != issue["key"]:
                seen.add(d)
                uniq.append(d)
        issue["dependsOn"] = uniq

    # 5) topological order (Kahn); stable-ish by category weight
    weight = {"foundation": 0, "epic": 1, "port": 2, "login": 3, "dashboard": 4, "list": 5,
              "form": 6, "detail": 6, "other": 7, "error": 8}
    indeg = {i["key"]: 0 for i in issues}
    adj = {i["key"]: [] for i in issues}
    for i in issues:
        for d in i["dependsOn"]:
            if d in adj:
                adj[d].append(i["key"])
                indeg[i["key"]] += 1
    ready = sorted([k for k, v in indeg.items() if v == 0],
                   key=lambda k: (weight.get(key_index[k]["kind"], 9), k))
    order = []
    while ready:
        k = ready.pop(0)
        order.append(k)
        for nb in adj[k]:
            indeg[nb] -= 1
            if indeg[nb] == 0:
                ready.append(nb)
        ready.sort(key=lambda k: (weight.get(key_index[k]["kind"], 9), k))
    if len(order) != len(issues):
        sys.stderr.write("warning: dependency cycle detected; emitting partial order\n")
        order += [k for k in key_index if k not in order]

    # stable, deterministic UUID per issue (from its key) — used to dedupe against the tracker
    for i in issues:
        i["uuid"] = issue_uuid(i["key"])

    n_ports = sum(1 for i in issues if i["kind"] == "port")
    graph = {"issues": issues, "order": order, "modules": modules,
             "counts": {"total": len(issues), "views": len(view_issues),
                        "epics": len(modules), "foundation": 1, "ports": n_ports}}
    with open(os.path.join(args.out, "graph.json"), "w") as fh:
        json.dump(graph, fh, indent=2)

    # 6) drafts (numbered by topo order so filenames sort like creation order)
    keynum = {k: n + 1 for n, k in enumerate(order)}
    architecture = load_architecture(args.architecture)
    for issue in issues:
        spec = load_spec(args.specs, issue["slug"]) if issue["view"] else None
        body = [f"# {issue['title']}", ""]
        # screenshot
        if issue["view"]:
            shot = f"{args.screenshots}/<stamp>/{issue['slug']}.png"
            body += ["## Screenshot", f"![{issue['slug']}]({shot})",
                     "_(adapter replaces this with the uploaded asset URL)_", ""]
        # what to implement
        body += ["## What to implement", ""]
        if issue["kind"] == "foundation":
            body += ["Scaffold the Vaadin 25 + Spring Boot app and port the service/business/data layer.",
                     "See `vaadin-scaffold` and its `references/service-port-playbook.md`.",
                     "**Blocks every other migration issue.**", "",
                     "### Target architecture", ""]
            body += render_architecture(architecture)
        elif issue["kind"] == "epic":
            body += [f"Tracking epic for the **{issue['module']}** module. Child view issues are listed "
                     "in the task list below; close this when they're all done.", "", "### Views", "",
                     "_(adapter fills the task list with child issue references)_", ""]
        elif issue["kind"] == "port":
            body += [f"Port the **{issue.get('feature')}** feature's service layer from Java EE/CDI to "
                     "Spring, in `migration/vaadin` (originals untouched). Rewrite this feature's "
                     "classes per `vaadin-scaffold/references/service-port-playbook.md` — keep the "
                     "logic, change only the plumbing (javax->jakarta included):", "",
                     "- entity: keep `@Entity`/relations/Bean Validation/Envers (jakarta namespace)",
                     "- repository: Deltaspike `@Repository` -> `JpaRepository<T,ID>` "
                     "(+`JpaSpecificationExecutor`; derived finders kept; `getRestrictions`/criteria "
                     "-> `Specification`)",
                     "- service: `@ApplicationScoped`/`@Stateless` -> `@Service`, field `@Inject` -> "
                     "constructor injection, `@Transactional` (Spring)",
                     "- business logics: `Instance<T>`/`@Any` -> `List<T>`/`ObjectProvider<T>`",
                     "- events: `Event`/`@Observes` -> `ApplicationEventPublisher`/`@EventListener`", ""]
        else:
            body += [render_what(issue["kind"], {"file": issue["view"]}, spec, issue["route"]), ""]
        # acceptance criteria
        if issue["kind"] not in ("foundation", "epic"):
            body += ["## Acceptance criteria", ""]
            body += [f"- [ ] {c}" for c in acceptance_criteria(issue["kind"], spec)]
            body += [""]
        # dependencies
        if issue["dependsOn"]:
            body += ["## Dependencies", ""]
            for d in issue["dependsOn"]:
                body.append(f"- Depends on **#{keynum.get(d, '?')}** "
                            f"(`{key_index[d]['title']}`)")
            body += [""]
        # meta + hidden dedupe marker (stable UUID; invisible in rendered markdown)
        body += ["---",
                 f"_labels:_ {', '.join(issue['labels'])} · _milestone:_ {issue['milestone']}"
                 + (f" · _epic:_ {issue['epic']}" if issue.get("epic") else ""),
                 "",
                 f"<!-- migration-id: {issue['uuid']} -->"]
        fname = f"{keynum[issue['key']]:03d}-{issue['slug']}.md"
        with open(os.path.join(drafts_dir, fname), "w") as fh:
            fh.write("\n".join(body) + "\n")

    print(f"issues: {graph['counts']['total']} "
          f"(1 foundation + {graph['counts']['epics']} epics + {graph['counts']['ports']} port + "
          f"{graph['counts']['views']} views)")
    print(f"graph:  {os.path.join(args.out, 'graph.json')}")
    print(f"drafts: {drafts_dir}/ ({len(issues)} files)")
    return 0


if __name__ == "__main__":
    raise SystemExit(main(sys.argv))
