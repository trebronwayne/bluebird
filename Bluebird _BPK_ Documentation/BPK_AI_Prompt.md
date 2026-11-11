# Bluebird BPK: AI Prompt

Copy everything inside the code block below into ChatGPT, Claude, Gemini or any AI chat, and replace
`[Describe the app you want here]` with your idea. (Bluebird's Settings > BPK apps page builds this
same prompt for you, with your idea filled in.)

````text
You are an expert web developer. Build an installable app for BLUEBIRD, a Windows-11-style launcher for Android. Apps for Bluebird are .bpk packages: a renamed ZIP archive that contains a web app (HTML, CSS, JavaScript). Follow the BPK reference at the end of this message exactly.

HOW TO WORK
1. If my idea is unclear, ask me at most 3 short questions first. Otherwise start building immediately.
2. Use plain HTML, CSS and vanilla JavaScript with no build step and no external CDN, fonts or libraries, so the app works offline. Only request the internet capability if the idea truly needs it.
3. Design for a phone screen AND a resizable desktop-style window: responsive layout, large touch targets, light and dark mode through prefers-color-scheme, rounded corners around 8px and a clean Windows 11 look.
4. Save the user's data with the Bluebird bridge (Bluebird.storageSet / Bluebird.storageGet) when it exists. Guard every bridge call with typeof Bluebird !== 'undefined' and fall back to localStorage so the app also runs in a normal browser for testing.
5. Request only the capabilities the app needs, and declare nothing the manifest format does not support.

WHAT TO OUTPUT (complete files only, never placeholders or "..." )
A. The final file tree.
B. Every file in full, each in its own code block labelled with its path.
C. One Python 3 script named build_bpk.py that uses ONLY the standard library. It must write all files into a folder, create icon/icon.png (a valid PNG, 256x256 or larger; you may use the helper below), and zip the folder CONTENTS into <app-id>.bpk so manifest.json sits at the archive root, with no extra parent folder.
D. Three short steps telling me how to run the script and install the result: run python3 build_bpk.py, copy the .bpk to my Android device, open it from Bluebird's File Explorer and follow the installer.

PNG HELPER YOU MAY USE IN build_bpk.py
import zlib, struct
def make_png(path, size=256, rgb=(0, 120, 212)):
    raw = b''.join(b'\x00' + bytes(rgb) * size for _ in range(size))
    def chunk(tag, data):
        body = tag + data
        return struct.pack('>I', len(data)) + body + struct.pack('>I', zlib.crc32(body) & 0xffffffff)
    png = b'\x89PNG\r\n\x1a\n' + chunk(b'IHDR', struct.pack('>IIBBBBB', size, size, 8, 2, 0, 0, 0)) + chunk(b'IDAT', zlib.compress(raw)) + chunk(b'IEND', b'')
    open(path, 'wb').write(png)

MY APP IDEA
[Describe the app you want here]

=== BPK REFERENCE ===
BLUEBIRD BPK DEVELOPER GUIDE — FORMAT V1

PURPOSE
A .bpk is a ZIP archive containing a web app for Bluebird. Build the app with HTML, CSS and JavaScript. Keep it self-contained where practical and design for small screens and resizable app windows.

REQUIRED ARCHIVE LAYOUT
manifest.json
icon/icon.png
app/index.html (or the entry path declared in manifest.json)
app/other files...
installer/ (optional custom installer assets)

MANIFEST FIELDS
{
  "id": "org.example.myapp",
  "name": "My App",
  "version": "1.0.0",
  "publisher": "Example Publisher",
  "entry": "app/index.html",
  "icon": "icon/icon.png",
  "description": "A short description",
  "homepage": "https://example.org/",
  "runtime": "web"
}

The required identity fields are id, name, version, publisher, entry and icon. Use a stable, unique id made from letters, numbers, dots, underscores or hyphens. Entry and icon paths must exist inside the package. The homepage and description are optional. Keep manifest.json at the archive root.

PACKAGING
1. Put manifest.json, icon/, app/ and optional installer/ at the top level of the ZIP. Do not zip an extra parent folder around them.
2. Ensure the icon is a valid PNG and the entry HTML exists.
3. Zip the contents and rename the resulting .zip to .bpk.
4. Install in Bluebird and test on the target device. A file extension alone does not make an invalid archive installable.

JAVASCRIPT BRIDGE
When Bluebird exposes the bridge to trusted local app content, the global object is named Bluebird. Calls return JSON strings unless otherwise noted:
- Bluebird.getAppInfo() — returns the current app id and bridge API version.
- Bluebird.capabilityGranted(name) — returns whether the named capability is granted.
- Bluebird.storageGet(key) — reads this app's private key/value data.
- Bluebird.storageSet(key, value) — writes this app's private key/value data.
- Bluebird.log(level, message) — writes a diagnostic message when Logs capability is granted.
- Bluebird.requestFilePicker() — asks the host to open the file picker if File Picker capability is granted.
- Bluebird.requestMediaPicker() — asks the host to open the media picker if Media Picker capability is granted.
- Bluebird.setBrightness(value) — requests brightness from 1 to 255 if granted and supported by the host and Android settings.

Example:
const info = JSON.parse(Bluebird.getAppInfo());
if (Bluebird.capabilityGranted('app_storage')) {
  const saved = JSON.parse(Bluebird.storageGet('display-name'));
  if (saved.ok && saved.value) console.log(saved.value);
  Bluebird.storageSet('display-name', 'Guest');
}
Bluebird.log('info', 'App started');

Storage keys may contain letters, numbers, dots, underscores, spaces and hyphens; they cannot contain slashes or backslashes. Each value is limited to approximately 256 KiB. The bridge returns objects with ok/value/error fields encoded as JSON for storage and picker/brightness methods. File and media picker selection events must be handled according to the host runtime's documented event integration; do not assume a picker result is synchronously returned by requestFilePicker().

CAPABILITIES
logs, app_storage, internet, file_picker, media_picker, audio_playback, video_playback, brightness_control. Request only the capabilities needed. Bluebird permission grants are not a substitute for safe coding or validation; actual enforcement can vary by host integration. Never expose the native JavaScript bridge to arbitrary remote websites. Restrict navigation and use safe local-content loading. Treat downloaded app content as untrusted until reviewed.

UPDATES
The update client expects an HTTPS JSON feed containing id, version, packageUrl and sha256. The app id must match the installed app, the package URL must use HTTPS, and sha256 must be a 64-character hexadecimal SHA-256 digest of the exact downloaded .bpk bytes. The package is staged and validated before install. Ask the user before applying an update. SHA-256 verifies file integrity against the supplied digest; it does not authenticate the publisher. For stronger publisher identity, add a signed manifest/package and verify it against a trusted public key.

TEST CHECKLIST
- Install a valid package and reject malformed packages.
- Confirm name, version, icon and entry file are correct.
- Test launch, window resizing, navigation and closing.
- Test every capability both granted and denied.
- Test missing files, invalid paths, oversized values and network failures.
- Verify logs are useful and do not include passwords, tokens or private user data.
- Test updates with correct and incorrect hashes and mismatched app ids.
- Test uninstall and confirm the expected behavior for retained app data.
- Never claim a capability is secure unless it is enforced in the actual runtime.
````
