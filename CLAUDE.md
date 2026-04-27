# Chitalka Kotlin — правила работы для агента

Рабочий корень: `chitalka-kotlin/`. Все пути ниже от него.

## Перед изменением кода

1. Сначала открой `docs/modules/README.md` — карта Gradle-модулей и таблица
   ответственности. Из неё определи целевой модуль.
2. Затем — соответствующий файл из `docs/modules/`:
   - `app.md` — Android-приложение, Compose UI, NavHost, WebView-читалка.
   - `library-kotlin.md` — чистый JVM/Kotlin: домен, ScreenSpec, ReaderBridge, i18n.
   - `library-android.md` — SQLite, EPUB, file picker, SharedPreferences.
   - `library-compose.md` — шаблонный модуль, **не подключён к `app`**, не трогать
     для продуктовых задач.
3. Только после этого — точечный поиск (Glob/Grep) по символам и путям из доки.
   Не читай весь репозиторий.

При сомнениях, какой файл — начни с `docs/modules/README.md`.

## Границы модулей (не нарушать)

| Что | Куда |
|-----|------|
| Доменные типы, `*ScreenSpec`, `ReaderBridge*`, навигация-контракты, i18n | **library-kotlin** |
| SQLite, EPUB, file picker, prefs, координатор открытия читалки | **library-android** |
| Compose-экраны, тема, `NavHost`, `WebView` | **app** |

`library-kotlin` не зависит от Android API. `library-android` зависит от
`library-kotlin`. `app` — от обоих. `library-compose` ни от чего рабочего не
зависит и в `app` не подключается.

## Стиль кода

- **Размер файла Kotlin ≤ 300 строк.** Превысил — режь на модули по
  ответственности. Прецедент в коммите `89f7f42`.
- **Без AI-следов и port-артефактов.** Когда правишь код, прогоняй чек-лист
  skill `human-code-review` (устанавливается на уровне пользователя — см.
  раздел «Установка на новой машине» ниже): комментарии «как в RN», молчаливые
  `catch`, мёртвые константы из старой платформы, локализованные строки в
  data-слое.
- **Комментарии — только когда WHY неочевидно.** Не описывай, что делает
  функция, если имя уже это говорит. Не пиши «added for…», «used by…»,
  «после рефакторинга…» — это место PR-описания, не кода.
- **Язык:** комментарии и сообщения коммитов — русский. Идентификаторы — английский.
- **Без `catch (_: Exception) {}`.** Либо логируй с контекстом, либо не лови.
  См. Category 2 в `human-code-review`.
- **Без feature-flag и backwards-compat прослоек, если задача их не требует.**
  Меняй код напрямую.

## Перед коммитом

- Если менял публичный API модуля, состав файлов или связи между модулями —
  обнови соответствующий `docs/modules/*.md` в том же коммите.
- Если удалил/переименовал файл — проверь упоминания в `docs/`.

## Skills

**Проектные** (`.claude/skills/`, версионируются вместе с кодом):
- `chitalka-modules` — карта модулей, выбор места для нового кода.
- `chitalka-library-kotlin` — детали JVM/Kotlin-слоя.
- `chitalka-android-data` — SQLite, EPUB, prefs, picker.
- `chitalka-compose-ui` — Compose, навигация, тема, WebView.

**Машинные** (`~/.claude/skills/`, общие для всех проектов на машине):
- `human-code-review` — чек-лист ревью на «человеческий стиль» и чистку
  AI/port-артефактов. Исходник в шаблоне: `setup/skills/human-code-review/`.

## Установка на новой машине

Skill `human-code-review` должен лежать на уровне пользователя, чтобы работал
во всех проектах, а не только в этом. После клонирования репозитория — один раз
установить:

```bash
# Linux / macOS / Git Bash на Windows
mkdir -p ~/.claude/skills/human-code-review
cp setup/skills/human-code-review/SKILL.md ~/.claude/skills/human-code-review/SKILL.md
```

```powershell
# Windows PowerShell
$dst = "$HOME\.claude\skills\human-code-review"
New-Item -ItemType Directory -Force -Path $dst | Out-Null
Copy-Item setup\skills\human-code-review\SKILL.md "$dst\SKILL.md" -Force
```

Источник правды — `setup/skills/human-code-review/SKILL.md` в этом репозитории.
При обновлении правь его и заново копируй на машину (или используй
`mklink` / `ln -s` вместо `cp` для симлинка). **Не клади skill в
`.claude/skills/`** — это создаст дубликат с user-level-копией, и в списке
доступных skill'ов он будет показан дважды.

## Что не делать

- Не плодить файлы документации (`*.md`) без явной просьбы пользователя.
- Не трогать `library-compose` для продуктовых задач — он не подключён.
- Не запускать сборку/тесты без необходимости (Gradle здесь долгий).
- Не использовать `--no-verify`, `--force` и прочие обходы без явной просьбы.
