# ScreenCycle — Concept (Co-Pilot for Family Link)

## Idea
Split Family Link’s allowed windows into child-friendly cycles: PLAY for N minutes → REST for M minutes, repeating until the daily budget is consumed or the sleep window starts.

## Architecture (MVP)
- `CycleService` — foreground service that toggles PLAY/REST and broadcasts state.
- `AppAccessibilityService` — watches current foreground app.
- `BlockOverlayService` — draws full-screen overlay during REST for selected packages.
- `Prefs` — stores durations, selected packages, running state.
- `MainActivity`, `AppSelectionActivity` — UI.

## Permissions
- Overlay (draw over other apps)
- Accessibility Service
- Usage Stats (optional telemetry for robustness)
- Notifications (foreground service)

## Limitations
No public API for Family Link → Co-Pilot mode infers availability via device state; it never expands access, only tightens it within Family Link windows.

---

# ScreenCycle — Концепция (Со-пилот для Family Link)

## Идея
Разделяет разрешённые Family Link окна на дружелюбные к ребёнку циклы: ИГРА N минут → ОТДЫХ M минут, повторяя до исчерпания дневного бюджета или начала окна сна.

## Архитектура (MVP)
- `CycleService` — foreground-служба, переключающая ИГРУ/ОТДЫХ и транслирующая состояние.
- `AppAccessibilityService` — отслеживает текущее активное приложение.
- `BlockOverlayService` — рисует полноэкранный оверлей во время ОТДЫХА для выбранных пакетов.
- `Prefs` — сохраняет длительности, выбранные пакеты и состояние работы.
- `MainActivity`, `AppSelectionActivity` — интерфейс.

## Разрешения
- Наложение поверх других приложений
- Служба доступности
- Статистика использования (необязательная телеметрия для устойчивости)
- Уведомления (foreground-служба)

## Ограничения
Нет публичного API для Family Link → режим Со-пилота определяет доступность по состоянию устройства; он никогда не расширяет доступ, а только ужесточает его внутри окон Family Link.
