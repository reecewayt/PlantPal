# PlantPal ESP32 Project
\\

## HiveMQ
### Setup
Requirements:
- Follow [this guide](https://docs.hivemq.com/hivemq-google-cloud-pubsub-extension/latest/index.html) to get setup with HiveMQ Enterprise Edition and Extension for Google Cloud Pub/Sub. We are using Google pub/sub as a bridge for our Firebase backend.

### Manual testing (quick and reliable)

Why use CLI clients for testing?
- Fast, scriptable, and easy to reproduce test cases.
- Works without waiting for embedded hardware — test the broker, topics, and message formats first.
- Good for automated checks (CI or local scripts) and for debugging connection/permission issues.

Before you start
- If you have the mosquitto broker installed as a macOS service, stop it to avoid port conflicts with HiveMQ:

```bash
brew services stop mosquitto
```

Two commonly used CLI tools:

- `mqtt` (part of the MQTT CLI tools) — modern, prints JSON nicely and convenient flags.
- `mosquitto_pub` / `mosquitto_sub` — lightweight, widely available (part of mosquitto clients).

Subscribe examples

Using `mqtt` (prints topic + payload):

```bash
# basic subscribe
mqtt sub -h localhost -p 1883 -t 'test/topic' -v

# json output with debug and timestamps
mqtt sub -h localhost -p 1883 -t 'test/topic' -T -J -d
```

Using `mosquitto_sub`:

```bash
# subscribe and print topic + payload
mosquitto_sub -h localhost -p 1883 -t 'test/topic' -v
```

Publish examples

Using `mqtt`:

```bash
# publish a simple text message
mqtt pub -h localhost -p 1883 -t 'test/topic' -m 'hello-from-mqtt-cli'

# publish with QoS 1 and verbose output
mqtt pub -h localhost -p 1883 -t 'test/topic' -m 'hello-from-mqtt-cli' -q 1 -v
```

Using `mosquitto_pub`:

```bash
# publish a message
mosquitto_pub -h localhost -p 1883 -t 'test/topic' -m 'hello-hivemq'
```

Tips
- Use `-h <host>` with your machine IP when testing from a different device (e.g. `-h 10.0.0.25`).
- Topics are case-sensitive. Double-check spelling.
- If you don't see messages, confirm HiveMQ is running (`docker ps`) and port 1883 is exposed.

When to use the Web UI
- The HiveMQ Web UI (http://localhost:8080) is great for quick manual exploration, visual subscription, and publishing, but CLI tools are better for repeatable tests and scripting.

# Schemas

Moisture Data Schema
Define in google pub/sub schemas
```json
{
  "type": "record",
  "name": "PlantPalSoilTelemtry",
  "fields": [
    {
      "name": "percentage",
      "type": "double"
    },
    {
      "name": "timestamp",
      "type": "long",
      "logicalType": "timestamp-millis"
    }
  ]
}
```

Example message test
```json
{
"percentage": 42.5,
"timestamp": 1697666103000
}
```
