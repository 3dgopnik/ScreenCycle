# ScreenCycle Guide

Welcome to the ScreenCycle user manual. The guide collects everything you need to install, launch, and operate the app. ScreenCycle blocks selected applications and categories during the rest phase and helps kids transition smoothly between play and downtime.

The synchronization service requires the `FOREGROUND_SERVICE_DATA_SYNC` permission to run properly.

- [Concept](../CONCEPT.md)
- [Quickstart](../quickstart.md)
- [UI guide](../ui-guide.md)
- [CLI](../cli.md)
- [Configuration](../config.md)
- [Workflows](../workflows.md)
- [Troubleshooting](../troubleshooting.md)
- [FAQ](../faq.md)

## Child-friendly countdown timer

The cycle timer uses oversized digits, high-contrast colors, and friendly illustrations so kids always understand how much time remains before the phase changes. The screen shows the active phase ("Play" or "Rest") and a short helper message for parents.

### How to read the timer

1. The background color signals urgency: green means more than 5 minutes left, yellow means between 1 and 5 minutes, and red highlights the final 60 seconds.
2. A rainbow progress bar illustrates how much of the interval has passed.
3. During the last 10 seconds the timer gently vibrates and fades to warn the child to wrap up.
4. Once the countdown reaches zero the label switches to the next phase and displays a short reminder on what to do next.

### Setup tips

- Make sure the device is not in Do Not Disturb mode so the haptic cue is delivered.
- Use **Customize rest screen** to tweak the background and captions if your child needs additional context.

## Smart mode

Smart mode observes how quickly the child transitions between phases and auto-adjusts play and rest durations to avoid abrupt stops and overload. It also watches for repeated attempts to open blocked apps and can trigger a short pause to reinforce the rules.

### How to enable smart mode

1. Open ScreenCycle and confirm the cycle is stopped.
2. Tap **Settings** on the home screen.
3. Scroll to the **Cycle behavior** section.
4. Toggle **Smart mode** on.
5. Confirm the change with the PIN if requested.
6. Return to the home screen — the start button now shows a "Smart" badge.

### How smart mode behaves

- Smart mode can extend the current phase for up to 2 minutes so the child can finish a match or prepare for rest.
- If the child voluntarily stops playing early, the unused minutes roll over to the next rest block.
- After three consecutive attempts to open a blocked app, the mode shows a transition screen with coaching tips and locks input for 30 seconds.
- The timer on the home screen and in the notification displays the adjusted time while keeping the same color scheme so the child stays oriented.

### How to confirm smart mode is active

- A "Smart mode on" caption appears under the timer on the home screen.
- The foreground service notification displays a lightbulb icon.
- The toggle in **Cycle behavior** remains enabled and highlighted.
