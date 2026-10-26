# STEM Plugin API

Since version 1.2.0 STEM ships a dedicated plugin API in the `stem-api` artifact. Plugins should
compile against this artifact only. Everything a plugin registers through the API is owned by that
plugin and removed automatically when the plugin is disabled.

## Project layout

| Module      | Artifact             | Content                                                        |
|-------------|----------------------|----------------------------------------------------------------|
| `stem-api`  | `de.linzn:stem-api`  | Plugin API: service interfaces and the types plugins use        |
| `stem-core` | `de.linzn:stem`      | STEM runtime (kernel, modules, API implementation)              |

The runtime jar `stem-<version>.jar` is built in `stem-core/target/`, its dependencies (including
`stem-api`) in `stem-core/target/core/dependencies/`.

## Plugin dependency

```xml
<dependency>
    <groupId>de.linzn</groupId>
    <artifactId>stem-api</artifactId>
    <version>1.2.0-SNAPSHOT</version>
    <scope>provided</scope>
</dependency>
```

Existing plugins that depend on `de.linzn:stem` keep working unchanged; `stem-api` comes with it transitively.

## Getting started

```java
public class MyPlugin extends STEMPlugin {

    @Override
    public void onEnable() {
        StemApi api = getStemApi();

        api.scheduler().runRepeating(this::poll, 0, 30, TimeUnit.SECONDS);
        api.scheduler().runDaily(this::cleanup, LocalTime.of(3, 30));

        api.events().register(new MyListener());
        api.commands().register("mycommand", args -> {
            getPluginLogger().info("Called with " + args.length + " arguments");
            return true;
        });

        api.mqtt().subscribe("home/livingroom/temperature", (topic, payload) ->
                getPluginLogger().info(topic + " = " + new String(payload, StandardCharsets.UTF_8)));
    }

    @Override
    public void onDisable() {
        // Nothing to clean up: tasks, listeners, commands and subscriptions
        // registered through getStemApi() are removed by STEM.
    }
}
```

## Services

| Service                    | Purpose                                                                     |
|----------------------------|-----------------------------------------------------------------------------|
| `scheduler()`              | Async, core-thread, delayed, repeating, daily and cron tasks; callbacks      |
| `events()`                 | Register `@StemEventHandler` listeners, fire events                         |
| `commands()`               | Console commands (`ICommand`), case-insensitive; `execute(...)` runs a command and returns its log output |
| `mqtt()`                   | Publish, subscribe; subscriptions survive reconnects                        |
| `notifications()`          | Push notifications to StemLink clients and the console                      |
| `information()`            | Information blocks for displays, AI text engine                             |
| `health()`                 | Register `HealthCheck`s, run checks on demand                               |
| `data()`                   | JSON key/value storage, `withConnection(...)` for raw SQL                   |
| `scripts()`                | Run `.stemsh` scripts with parameters and timeout                           |
| `cloud()`                  | Nextcloud upload and share links                                            |
| `plugins()`                | Loaded plugins and their state                                              |
| `system()`                 | Version, uptime, time zone, temp folder                                     |
| `logger()`                 | Logger prefixed with the plugin name (also `STEMPlugin#getPluginLogger()`)  |

Callback operations `ShellOperation` and `ScriptOperation` are part of `stem-api` as well.

## Events

Event handlers are methods annotated with `@StemEventHandler` that take one event parameter. A handler receives
events of exactly that class. A handler whose parameter type is `StemEvent` itself receives every event; it runs
after the handlers of the same priority registered for the concrete class. Use it for logging or for automations
that react to events of other plugins without depending on them.

```java
@StemEventHandler(priority = StemEventPriority.LOW)
public void onAnyEvent(StemEvent event) {
    // event.getName() is the simple class name, e.g. MQTTSnapmakerPrintEndEvent
}
```

## Threading rules

- `runAsync`, `runLater`, `runRepeating`, `runDaily`, `runOnceAt` and `runCron` run on virtual threads.
  Blocking I/O is fine there.
- `...InCore` variants run one after another on the single core thread. Keep them short.
- A repeating task never overlaps itself: if a run is still active when the next is due, that run is skipped.
- Exceptions thrown by tasks, event handlers and MQTT handlers are logged; they never stop STEM.
- MQTT handlers run on one ordered dispatch thread. Hand long work to the scheduler.
- Times of day and cron patterns use `Europe/Berlin`.

## Legacy access

`STEMApp.getInstance()` and the module getters (`getMqttModule()`, `getScheduler()`, ...) remain
available for existing plugins but are deprecated for plugin use. Resources registered the old way are
still cleaned up on disable when STEM can attribute them to a plugin (registration inside `onEnable` or a
class in the plugin's package).

## Plugin lifecycle

- Plugins are loaded once at startup, in dependency order (`depend` in `plugin.yml`).
- Loading plugins at runtime is not supported.
- Plugins are disabled in reverse load order before the core modules shut down.
