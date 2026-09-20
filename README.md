# DeviceBase Java SDK

Java SDK for the [DeviceBase](https://devicebase.cn) device automation platform. Control three device platforms programmatically over HTTP:

| Platform | Covers | Path family |
|----------|--------|-------------|
| **mobile** | Android, HarmonyOS, iOS | `/v1/{action}/{serialno}` |
| **browser** | Chrome / Chromium / Edge over CDP | `/api/browser/{serialno}/{action...}` |
| **computer** | macOS / Windows / Linux desktops | `/api/computer/{serialno}/{action}` |

## Requirements

- Java 11 or later

## Installation

### Maven

```xml
<dependency>
    <groupId>cn.devicebase</groupId>
    <artifactId>devicebase-sdk</artifactId>
    <version>0.1.0</version>
</dependency>
```

### Gradle

```groovy
implementation 'cn.devicebase:devicebase-sdk:0.1.0'
```

## Quick Start

```java
import cn.devicebase.client.DeviceBaseClient;
import cn.devicebase.model.DeviceInfo;

try (DeviceBaseClient client = new DeviceBaseClient("your-api-key", "db-mttul4i41di8")) {
    DeviceInfo info = client.getDeviceInfo();

    client.tap(100, 200);
    client.launchApp("com.tencent.mm");

    byte[] screenshot = client.getScreenshot();
    Files.write(Path.of("screen.jpg"), screenshot);
}
```

## Configuration

```java
DeviceBaseClient client = new DeviceBaseClient(
    "your-api-key",               // or null to read DEVICEBASE_API_KEY
    "db-mttul4i41di8",            // the device serialno
    "https://api.devicebase.cn",  // optional, default shown
    Duration.ofSeconds(30)        // optional, default shown
);
```

Environment variables are used as fallbacks:

| Variable | Required | Description |
|----------|----------|-------------|
| `DEVICEBASE_API_KEY` | yes | API key for authentication |
| `DEVICEBASE_BASE_URL` | no | API base URL (default: `https://api.devicebase.cn`) |

A missing API key throws `AuthenticationException` from the constructor.

The client is bound to one device for the **mobile** methods. The browser and computer APIs take the serialno per call, so one client can drive many devices:

```java
client.browser().navigate("db-mtsi49bf0mqb", "https://example.com");
client.computer().click("db-mtthisv311f1", 640, 360, null);
```

## Finding a device

`listDevices` is the entry point: it is the only call that needs no serialno, and it is how a serialno is discovered.

```java
for (String category : new String[] {"mobile", "browser", "computer"}) {
    for (DeviceResponse device : client.listDevices(category)) {
        System.out.println(device.getSerialno() + "  " + device.getType() + "  " + device.getOsType());
    }
}

// With filters
List<DeviceResponse> free = client.listDevices("Pixel", "mobile", "free", 50);
```

| Parameter | Description |
|-----------|-------------|
| `keyword` | Case-insensitive substring match across name/alias_name/brand/model/serialno/device_sn/type/os_type/os_version/location/operator |
| `type` | Category (`mobile`/`browser`/`computer`) or system type (`android`/`harmonyos`/`ios`/`macos`/`windows`/`linux`/`chrome`/`chromium`/`edge`/`other`) |
| `state` | `busy` / `free` / `offline` |
| `limit` | Maximum number of devices (the server defaults to 10) |

System types match against the device's `os_type`, because a device row only carries the coarse `type` — a Chrome browser is `type=browser` with `os_type=Chrome`. Two are defined by exclusion: `linux` is every computer that is neither macOS nor Windows, and `other` is every browser that is not Chrome/Chromium/Edge.

### Serial numbers

The device identifier is **`serialno`** — the platform-issued key (e.g. `db-mttul4i41di8`) that every control method takes. Its physical counterpart is `device_sn`; the gateway resolves either, but the serialno is primary.

`DeviceResponse` also accepts **`serial`** as a second key for the same column:
the older Python service spells it that way. When a row carries both, `serialno`
wins.

### Renamed from `serial`

`getSerial()` is now `getSerialno()`, and the constructor parameters are named
`serialno`. The old accessor is kept as a `@Deprecated` alias and is removed in
the next major release. Constructor arguments are positional, so existing calls
are unaffected.

## Mobile

The serialno is bound at construction. Reach the API through `client` directly or `client.getHttpClient()`.

```java
// Touch
client.tap(100, 200);
client.tap(new Point(100, 200));
client.doubleTap(100, 200);
client.longPress(100, 200);
client.swipe(0, 500, 500, 500);
client.swipe(new Bounds(0, 500, 500, 500));

// Navigation
client.back();
client.home();

// Apps
client.launchApp("com.tencent.mm");
client.stopApp("com.tencent.mm");
client.stopCurrentApp();
AppInfo app = client.getCurrentApp();

// Text
client.inputText("Hello World");
client.clearText();

// Inspection
DeviceInfo info = client.getDeviceInfo();
HierarchyInfo hierarchy = client.dumpHierarchy();

// Shell (adb/hdc platforms only). The command's own exit status arrives in
// data.exitCode — a non-zero value is not an API error.
OperationResult ran = client.bash("ls -la /sdcard");
System.out.println(ran.getData().get("exitCode"));

// Install — the path is on the agent host, not local.
OperationResult install = client.installApp("/tmp/app.apk");
client.installStatus((String) install.getData().get("installId"));

// Screenshot
byte[] jpeg = client.getScreenshot();
```

## Browser

The serialno of a registered browser device, from `client.listDevices("browser")`. Selectors are CSS selectors.

```java
BrowserApi browser = client.browser();
String serialno = "db-mtsi49bf0mqb";

browser.navigate(serialno, "https://example.com");
browser.refresh(serialno);
browser.goBack(serialno);
browser.goForward(serialno);

browser.click(serialno, "button#submit");
browser.fill(serialno, "#search", "devicebase");
browser.select(serialno, "#country", "CN");
browser.text(serialno, "#search");
browser.attribute(serialno, "a.logo", "href");
browser.exists(serialno, ".modal");
browser.execute(serialno, "document.title");   // danger tier: runs in the page
browser.input(serialno, "hello 世界");          // CDP insertText, reliable for CJK
browser.hotkey(serialno, List.of("Meta", "a"));

browser.state(serialno);
browser.tabs(serialno);
browser.tabOpen(serialno, "https://example.com");
browser.tabSwitch(serialno, tabId);
browser.tabClose(serialno, tabId);
browser.tabCloseAll(serialno);

browser.launch(serialno);
browser.close(serialno);
```

Editing shortcuts (`Meta a`, `Backspace`) act on the page. Browser-chrome shortcuts such as `Control+t` are **not reachable** — CDP drives the page, not the browser UI.

## Computer

The serialno of a registered computer device, from `client.listDevices("computer")`. Coordinates are absolute screen pixels.

```java
ComputerApi computer = client.computer();
String serialno = "db-mtthisv311f1";

computer.click(serialno, 640, 360, null);                 // null → server default "left"
computer.click(serialno, 640, 360, ComputerApi.BUTTON_RIGHT);
computer.doubleClick(serialno, 640, 360);
computer.longClick(serialno, 640, 360, 2);                 // seconds; 0 → driver default
computer.move(serialno, 100, 100);
computer.drag(serialno, 100, 100, 800, 600);
computer.scroll(serialno, ComputerApi.SCROLL_DOWN, 5);     // amount 0 → driver default

computer.typeText(serialno, "hello");
computer.press(serialno, "Enter");
computer.hotkey(serialno, List.of("Control", "Shift", "Escape"));

computer.position(serialno);
computer.screenSize(serialno);
computer.permissions(serialno);
computer.launchApp(serialno, "Visual Studio Code");

// wait takes MILLISECONDS; bash's timeout takes SECONDS.
computer.waitFor(serialno, 2000);
computer.bash(serialno, "ls -la", 30);
```

`bash` runs on the host machine as the desktop user, unsandboxed, under the platform default shell — treat it as shell access. A non-zero command exit is reported in `data.exitCode`, not as an exception, because the API call itself succeeded:

```java
OperationResult ran = computer.bash(serialno, "exit 3", 10);
// No exception; ran.getData().get("exitCode") is 3.
```

`waitFor` and `bash` widen their HTTP deadline per call to cover however long they were asked to block, so a wait past the default 30s is not aborted client-side.

## Screenshots

`getScreenshot(serialno)` posts to `/v1/screen/{serialno}`, which is a **cross-family** route: the server dispatches it by device type, so it works for mobile, browser and computer serials alike. The server decides the format (JPEG) regardless of any file extension you choose.

```java
byte[] data = client.getHttpClient().getScreenshot("db-mtsi49bf0mqb");
```

`downloadScreenshot(serialno)` is an SDK-only extra with no CLI equivalent — it hits `GET /v1/screenshot/{serialno}` instead.

## Error Handling

Two failure layers are surfaced, and the second is easy to miss:

| Layer | Exception | Meaning |
|-------|-----------|---------|
| Gateway | `AuthenticationException` (401), `DeviceNotFoundException` (404), `ValidationException` (400/422), `DeviceBaseException` (other non-2xx) | The HTTP status was non-2xx |
| Business | `BusinessException` | HTTP 200, but the envelope carried a non-2xx `code` |

All of them extend `DeviceBaseException`, which is unchecked.

```java
try {
    client.browser().click(serialno, "#missing");
} catch (BusinessException e) {
    // The action failed in the driver, e.g. no element matched.
    System.out.println("action failed: " + e.getCode() + " " + e.getBody());
} catch (AuthenticationException e) {
    System.out.println("authentication failed: " + e.getMessage());
} catch (DeviceNotFoundException e) {
    System.out.println("device not found: " + e.getMessage());
} catch (DeviceBaseException e) {
    System.out.println("error: " + e.getMessage());
}
```

The control API reports action failures **inside an otherwise successful response** — a selector that matches nothing comes back as HTTP 200 with `{"code":502,...}`. Guarding on the HTTP status alone would treat that as success, which is why the envelope is inspected too.

## WebSocket clients

Real-time screen streaming and touch control:

```java
try (MinicapClient minicap = new MinicapClient(baseUrl, apiKey, serialno)) {
    minicap.connect();
    byte[] frame = minicap.readFrame();
}

try (MinitouchClient minitouch = new MinitouchClient(baseUrl, apiKey, serialno)) {
    minitouch.connect();
    minitouch.tap(100, 200);
    minitouch.swipe(100, 500, 100, 100, 300, 10);
}
```

## Deprecated API

The following methods target the `/api/v1/devices/*` route family, which the current gateway **does not serve** — every one of them answers 404. They are kept so existing callers keep compiling, and are marked `@Deprecated`:

| Method | Replacement |
|--------|-------------|
| `listDevices(String keyword, String type, String state, int page, int pageSize)` | `listDevices(keyword, type, state, limit)` — reads `/v1/devices` |
| `getDevice(int)`, `getDeviceStats(boolean)` | `listDevices(...)` |
| `startDeviceUsage`, `sendHeartbeat`, `stopDeviceUsage`, `getActiveSession` | none — that session API is not served |

On `DeviceResponse`, the accessors `getSerial()`, `getDescription()`, `getServerUrl()`, `getUserId()`, `isShared()` and `getCreatedAt()` are also deprecated: the list route does not return those fields. Use `getSerialno()`, `getDeviceSn()`, `getOsType()` and the other accessors that reflect the live payload.

## Building

```bash
./gradlew build     # compile + test + jar
./gradlew test      # tests only
```

The test suite runs against an in-process HTTP server (the JDK's built-in `com.sun.net.httpserver`), so it needs no network and no mocking library.

## License

MIT
