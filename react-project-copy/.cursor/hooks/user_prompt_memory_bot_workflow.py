import json
import re
import sys
from datetime import datetime, timezone
from pathlib import Path

STATE_PATH = Path(".cursor/hooks/.agent_workflow_state.json")
POLICY_PATH = Path(__file__).resolve().parent / "memory_bot_policy.txt"
# Same intent as in .claude/settings.local.json: UserPromptSubmit injects this text.
STRICT_PATTERNS = re.compile(
    r"memory-?bot|gbrain|gbip|g-brain|quest-?games",
    re.IGNORECASE,
)


def load_policy() -> str:
    if POLICY_PATH.is_file():
        return POLICY_PATH.read_text(encoding="utf-8").strip()
    return ""


def should_enforce_workflow(data: dict) -> bool:
    prompt = data.get("prompt", "")
    if isinstance(prompt, str) and STRICT_PATTERNS.search(prompt):
        return True
    for att in data.get("attachments", []) or []:
        if not isinstance(att, dict):
            continue
        p = att.get("file_path") or att.get("path")
        if isinstance(p, str) and "memory-bot" in p.replace("\\", "/").lower():
            return True
    return False


def main() -> None:
    _ = json.load(sys.stdin)
    additional = load_policy()
    enforce = should_enforce_workflow(_)
    state = {
        "docs_checked": False,
        "docs_sources": [],
        "touched_files": [],
        "ignore_memory_bot_workflow": not enforce,
        "last_reset_at": datetime.now(timezone.utc).isoformat(),
    }
    STATE_PATH.parent.mkdir(parents=True, exist_ok=True)
    STATE_PATH.write_text(json.dumps(state, ensure_ascii=True, indent=2), encoding="utf-8")
    out: dict = {"permission": "allow", "continue": True}
    if additional:
        out["additional_context"] = additional
    print(json.dumps(out, ensure_ascii=False))


if __name__ == "__main__":
    main()
