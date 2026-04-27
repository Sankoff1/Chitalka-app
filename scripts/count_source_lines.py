"""
Считает строки в исходниках Kotlin (.kt, .kts) в chitalka-kotlin.
Пропускает сборку, кэш IDE и прочий мусор (только «наш» код модулей).
"""
from __future__ import annotations

import csv
import os
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent

SKIP_DIR_NAMES = frozenset({
    ".git",
    ".gradle",
    ".idea",
    "build",
    "node_modules",
    ".kotlin",
    "captures",
    ".cxx",
    "bin",
    "out",
    ".claude",
})

CODE_SUFFIXES = (".kt", ".kts")

LINES_REPORT = "source-lines-report.txt"
LINES_TABLE_CSV = "source-lines-table.csv"


def should_skip_dir(name: str) -> bool:
    return name in SKIP_DIR_NAMES


def iter_code_files(root: Path):
    root_s = str(root)
    for dirpath, dirnames, filenames in os.walk(root_s, topdown=True):
        dirnames[:] = [d for d in dirnames if not should_skip_dir(d)]
        base = Path(dirpath)
        for fn in filenames:
            if fn.endswith(CODE_SUFFIXES):
                yield base / fn


def line_count(path: Path) -> int:
    try:
        text = path.read_text(encoding="utf-8", errors="replace")
    except OSError:
        return -1
    if not text:
        return 0
    return text.count("\n") + (0 if text.endswith("\n") else 1)


def rel(p: Path) -> Path:
    try:
        return p.relative_to(ROOT)
    except ValueError:
        return p


def main() -> int:
    rows: list[tuple[int, Path]] = []
    for f in iter_code_files(ROOT):
        n = line_count(f)
        if n < 0:
            continue
        rows.append((n, f))

    by_lines = sorted(rows, key=lambda t: (-t[0], str(t[1]).lower()))

    lines_out = [
        f"# chitalka-kotlin: строки в .kt / .kts (без build, .gradle, .idea, …)",
        f"# всего файлов: {len(by_lines)}",
        f"# сумма строк: {sum(t[0] for t in by_lines)}",
        "",
    ]
    for n, p in by_lines:
        lines_out.append(f"{rel(p).as_posix()}\t{n}")

    (ROOT / LINES_REPORT).write_text("\n".join(lines_out) + "\n", encoding="utf-8")
    print(f"Written: {ROOT / LINES_REPORT} ({len(by_lines)} files)")

    csv_path = ROOT / LINES_TABLE_CSV
    with csv_path.open("w", encoding="utf-8-sig", newline="") as fp:
        w = csv.writer(fp, delimiter=";")
        w.writerow(["path", "lines"])
        for n, p in by_lines:
            w.writerow([rel(p).as_posix(), n])
    print(f"Written: {csv_path} (таблица для Excel: path;lines)")

    return 0


if __name__ == "__main__":
    sys.exit(main())
