# Build a Bluebird app with AI (5 minutes)

You do not need to know how to code. An AI assistant writes the app, and one script packages it.

## 1. Get the prompt

- On your device: **Settings > BPK apps**, type your idea, tap **Copy AI prompt**.
- Or open [`BPK_AI_Prompt.md`](BPK_AI_Prompt.md) and copy the prompt manually.

## 2. Paste it into an AI

Any assistant works (ChatGPT, Claude, Gemini, Copilot). Answer its questions if it asks any (at most three).

## 3. Get your files

The AI replies with:

1. the file tree,
2. every file in full (`manifest.json`, `app/index.html`, styles, scripts),
3. a Python script, `build_bpk.py`, that writes the files, creates the icon, and zips everything into `your-app.bpk`.

## 4. Build the package

Save `build_bpk.py` and run it on a computer, or in Termux on Android:

```bash
python3 build_bpk.py
```

It needs only Python 3 (standard library), so there is nothing to install.

## 5. Install and test

Copy the `.bpk` to your device, open it from Bluebird File Explorer and follow the installer. Launch the
app from Start, resize the window, and check light and dark mode.

## Improving your app

Paste your current files back into the same AI chat and describe the change ("add a dark theme",
"save my data between launches"). Increase `version` in `manifest.json` each time you reinstall.

## If something goes wrong

| Problem | Fix |
| --- | --- |
| Installer rejects the package | `manifest.json` must be at the **root** of the zip, not inside a folder. Re-run the script. |
| Icon missing | `icon/icon.png` must be a real PNG and match `icon` in the manifest. |
| White screen | The `entry` path in the manifest must point to an existing HTML file. |
| Data not saved | Request the `app_storage` capability and call `Bluebird.storageSet`. |
| Needs the internet | Add the `internet` capability. Only grant it to apps that need it. |

Next: the full [`BPK_Developer_Guide.md`](BPK_Developer_Guide.md) and the formal
[`BPS_v1_Formal_Specification.md`](BPS_v1_Formal_Specification.md).
