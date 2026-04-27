import json
import sys
from pathlib import Path
from typing import Any


STATE_PATH = Path(".cursor/hooks/.agent_workflow_state.json")


def load_state() -> dict[str, Any]:
    if not STATE_PATH.exists():
        return {"touched_files": []}
    try:
        return json.loads(STATE_PATH.read_text(encoding="utf-8"))
    except (OSError, json.JSONDecodeError):
        return {"touched_files": []}


def infer_modules(files: list[str]) -> list[str]:
    modules: set[str] = set()
    for file_path in files:
        normalized = file_path.replace("\\", "/")
        parts = [p for p in normalized.split("/") if p]
        if "memory-bot" in parts:
            idx = parts.index("memory-bot")
            if idx + 1 < len(parts):
                modules.add(parts[idx + 1])
    return sorted(modules)


def main() -> None:
    _ = json.load(sys.stdin)
    state = load_state()
    touched_files = [f for f in state.get("touched_files", []) if isinstance(f, str)]
    modules = infer_modules(touched_files)

    if modules:
        modules_text = ", ".join(modules)
        followup = (
            f"Перед завершением: если в этой сессии ты реализовал/изменил модули memory-bot (затрагивал: {modules_text}) "
            "— обнови или создай соответствующую документацию в `memory-bot/docs/` "
            "(`docs/modules/*.md`, `docs/modules-map.md`, при необходимости `docs/submoduli/*.md` и `docs/moduli-detail/sec-*.md`). "
            "Если код модулей не менялся, ответь коротко, что документация актуальна, и завершай."
        )
    else:
        followup = (
            "Перед завершением: если в этой сессии ты реализовал/изменил модули memory-bot — "
            "обнови или создай соответствующую документацию в `memory-bot/docs/` "
            "(`docs/modules/*.md`, `docs/modules-map.md`, при необходимости `docs/submoduli/*.md` и `docs/moduli-detail/sec-*.md`). "
            "Если код модулей не менялся, ответь коротко, что документация актуальна, и завершай."
        )

    print(json.dumps({"followup_message": followup}, ensure_ascii=False))


if __name__ == "__main__":
    main()
