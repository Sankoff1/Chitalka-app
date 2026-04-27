import json
import sys
from pathlib import Path
from typing import Any

STATE_PATH = Path(".cursor/hooks/.agent_workflow_state.json")
FILE_PATH_KEYS = {"path", "file_path", "target_directory", "target_notebook", "working_directory", "old_path", "new_path"}

# After docs phase, allow; before — only Read of memory-bot/docs, or tools that do not touch memory-bot.
PHASE1_DENY = {
    "Glob",
    "Grep",
    "rg",
    "SemanticSearch",
    "codebase_search",
    "Task",
    "Write",
    "StrReplace",
    "Delete",
    "Scaffold",
    "ApplyPatch",
    "NotebookEdit",
}
READ_NAMES = {"Read", "ReadFile", "TabRead"}


def load_state() -> dict[str, Any]:
    if not STATE_PATH.exists():
        return {
            "docs_checked": False,
            "docs_sources": [],
            "touched_files": [],
            "ignore_memory_bot_workflow": True,
        }
    try:
        return json.loads(STATE_PATH.read_text(encoding="utf-8"))
    except (OSError, json.JSONDecodeError):
        return {
            "docs_checked": False,
            "docs_sources": [],
            "touched_files": [],
            "ignore_memory_bot_workflow": True,
        }


def save_state(state: dict[str, Any]) -> None:
    STATE_PATH.parent.mkdir(parents=True, exist_ok=True)
    STATE_PATH.write_text(json.dumps(state, ensure_ascii=True, indent=2), encoding="utf-8")


def get_tool_name(payload: dict[str, Any]) -> str:
    for key in ("tool_name", "toolName", "name"):
        v = payload.get(key)
        if isinstance(v, str) and v:
            return v
    tool = payload.get("tool")
    if isinstance(tool, dict) and isinstance(tool.get("name"), str):
        return tool["name"]
    return ""


def get_tool_input(payload: dict[str, Any]) -> dict[str, Any]:
    for key in ("tool_input", "toolInput", "input", "arguments"):
        v = payload.get(key)
        if isinstance(v, dict):
            return v
    return {}


def iter_path_strings(value: Any) -> list[str]:
    out: list[str] = []
    if isinstance(value, str):
        out.append(value)
    elif isinstance(value, dict):
        for k, item in value.items():
            if k in FILE_PATH_KEYS or k.lower().endswith("path"):
                out.extend(iter_path_strings(item))
            else:
                out.extend(iter_path_strings(item))
    elif isinstance(value, list):
        for item in value:
            out.extend(iter_path_strings(item))
    return out


def path_under_memory_bot_docs(s: str) -> bool:
    p = s.replace("\\", "/").lower()
    if "memory-bot" not in p:
        return False
    return "/memory-bot/docs/" in p or p.rstrip("/").endswith("/memory-bot/docs")

def path_under_memory_bot(s: str) -> bool:
    return "memory-bot" in s.replace("\\", "/").lower()


def maybe_mark_docs_read(state: dict[str, Any], tool_name: str, tool_input: dict[str, Any]) -> None:
    if tool_name not in READ_NAMES:
        return
    for s in iter_path_strings(tool_input):
        if path_under_memory_bot_docs(s):
            state["docs_checked"] = True
            try:
                state.setdefault("docs_sources", [])
                if isinstance(state["docs_sources"], list) and s:
                    state["docs_sources"].append(s[:800])
            except OSError:
                pass
            return


def maybe_track_touched(state: dict[str, Any], tool_input: dict[str, Any]) -> None:
    touched = {x for x in state.get("touched_files", []) if isinstance(x, str)}
    for s in iter_path_strings(tool_input):
        if any(c in s for c in ("/", "\\", ".")) and s:
            touched.add(s.replace("\\", "/"))
    state["touched_files"] = sorted(touched)


def main() -> None:
    payload = json.load(sys.stdin)
    state = load_state()
    if state.get("ignore_memory_bot_workflow", True):
        print(json.dumps({"permission": "allow"}, ensure_ascii=True))
        return

    tool_name = get_tool_name(payload)
    tool_input = get_tool_input(payload)
    tnorm = (tool_name or "").strip()

    if not state.get("docs_checked", False):
        if tnorm in READ_NAMES:
            for s in iter_path_strings(tool_input):
                if not path_under_memory_bot(s):
                    continue
                if not path_under_memory_bot_docs(s):
                    print(
                        json.dumps(
                            {
                                "permission": "deny",
                                "agent_message": (
                                    "Сначала читай только `memory-bot/docs/` (см. modules-map.md и memory_bot_policy). "
                                    f"Сейчас путь: {s[:200]}. Не исходники — до этапа 1 ими не пользоваться."
                                ),
                            },
                            ensure_ascii=False,
                        )
                    )
                    return
            maybe_mark_docs_read(state, tnorm, tool_input)
            save_state(state)
            print(json.dumps({"permission": "allow"}, ensure_ascii=True))
            return
        if tnorm in PHASE1_DENY:
            print(
                json.dumps(
                    {
                        "permission": "deny",
                        "agent_message": (
                            "До чтения документации из `memory-bot/docs/` нельзя: Glob, Grep, поиск кода, "
                            "субагенты, правки. Сначала Read файлов карт/модулей из `memory-bot/docs/`, затем — этап 2."
                        ),
                    },
                    ensure_ascii=False,
                )
            )
            return
        # Other tools in phase 1: allow, then track
        save_state(state)
        print(json.dumps({"permission": "allow"}, ensure_ascii=True))
        return

    maybe_mark_docs_read(state, tnorm, tool_input)
    maybe_track_touched(state, tool_input)
    save_state(state)
    print(json.dumps({"permission": "allow"}, ensure_ascii=True))


if __name__ == "__main__":
    main()
