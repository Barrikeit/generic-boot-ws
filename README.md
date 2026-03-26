# WebSocket STOMP — Postman Testing Guide

## Prerequisites

- Postman v10+ (WebSocket support required)
- REST app running on `http://localhost:9891`
- WS app running on `http://localhost:9892`

---

## Step 1 — Get a JWT token

Call the REST app login endpoint to obtain an access token.

```
POST http://localhost:9891/generic/api/v1/auth/login
Content-Type: application/json

{"username": "keit", "password": "keit"}
```

Copy the `access-token` from the response. Save it as a Postman variable `{{authToken}}`.

---

## Step 2 — Open a WebSocket connection in Postman

1. Click **New** → **WebSocket**
2. Enter the URL: `ws://localhost:9892/generic-ws/api/v1/ws`
3. Click **Connect**
4. Status should show **101 Switching Protocols**

---

## Step 3 — Send STOMP frames

> **IMPORTANT — How to send messages in Postman:**
>
> - Switch the message input to **Binary (Hex)** mode
> - Every STOMP frame must end with a **NULL byte** (`00` in hex)
> - The blank line between the last header and the body is required
> - The blank line between the body and the NULL byte is required
> - Headers and command are separated by `\r\n` (`0D0A` in hex)

---

### Frame 1 — CONNECT

Must be the first frame sent. Authenticates the STOMP session using the JWT.

```
CONNECT
accept-version:1.1,1.0
heart-beat:10000,0
Authorization:Bearer {{authToken}}
app-called-service:generic-ws

[NUL]
```

**Expected server response:**

```
CONNECTED
version:1.1
heart-beat:10000,0
```

---

### Frame 2 — SUBSCRIBE to a topic or queue

Subscribe before sending to a destination so you can receive the response.

**Subscribe to pong broadcast:**

```
SUBSCRIBE
id:sub-pong
destination:/user/queue/pong

[NUL]
```

**Subscribe to hello broadcast:**

```
SUBSCRIBE
id:sub-0
destination:/topic/hello

[NUL]
```

**Subscribe to users topic:**

```
SUBSCRIBE
id:sub-1
destination:/topic/users

[NUL]
```

**Subscribe to rooms topic:**

```
SUBSCRIBE
id:sub-2
destination:/topic/rooms

[NUL]
```

---

### Frame 3 — SEND messages

**Send a ping** (broadcasts response to `/user/queue/pong`):

```
SEND
destination:/app/ping

[NUL]
```

**Send a hello message** (broadcasts response to `/topic/hello`):

```
SEND
destination:/app/hello

{"name":"barrikeit"}

[NUL]
```

**Expected message received on `/topic/hello`:**

```json
{
  "sender": "keit",
  "content": "Hello barrikeit"
}
```

---

### Frame 4 — DISCONNECT

Clean disconnect — always send this before closing Postman to avoid session leaks.

```
DISCONNECT

[NUL]
```

---

## Destination reference

| Direction       | Destination          | Description                    |
|-----------------|----------------------|--------------------------------|
| Client → Server | `/app/ping`          | Private ping to yourself       |
| Server → Client | `/user/queue/pong`   | Private pong reply             |
| Client → Server | `/app/hello`         | Send a hello message           |
| Server → Client | `/topic/hello`       | Hello broadcast                |
| Client → Server | `/app/echo`          | Send a message, broadcast back |
| Server → Client | `/topic/echo`        | Echo broadcast                 |
| Server → Client | `/user/queue/errors` | Auth / session errors          |

---

## Error reference

Errors are delivered to `/user/queue/errors` as JSON:

```json
{
  "code": "TOKEN_EXPIRED",
  "message": "...",
  "timestamp": 1234567890
}
```

| Code              | Meaning                   | Client action            |
|-------------------|---------------------------|--------------------------|
| `TOKEN_EXPIRED`   | JWT expired mid-session   | Refresh token, reconnect |
| `SESSION_REVOKED` | Session deleted by server | Redirect to login        |
| `AUTH_FAILED`     | Invalid token on CONNECT  | Redirect to login        |
| `SERVER_ERROR`    | Unexpected server error   | Retry with backoff       |

---

## Notes

- Messages **must** be sent as **Binary Hex** in Postman — plain text does not correctly encode the NULL terminator
- The STOMP `Authorization` and `app-called-service` headers are validated on the CONNECT frame only
- HTTP handshake headers (during the 101 upgrade) are not used for auth — only the STOMP CONNECT frame is
- Heartbeat: server sends every 10s, client response is optional for testing
- Token expiry is configured in the REST app (`security.jwt.expiration`) — set to `86400` in dev to avoid frequent
  re-logins