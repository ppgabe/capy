# Capy Backend Demo Chat

## Demo LLM chat (Cloudflare Workers AI)

This backend can simulate demo users that reply via a free Cloudflare Workers AI model.

### Setup: Cloudflare Workers AI

1) Create or log in to your Cloudflare account.
2) Enable Workers AI for your account (Dashboard → Workers & Pages → AI).
3) Copy your **Account ID** (Dashboard → Right sidebar → Account ID).
4) Create an API token (My Profile → API Tokens → Create Token) with Workers AI permissions.

### Configure the backend (environment variables)

You can set these in your shell, in your IDE run configuration, or as system environment variables. The backend does not auto-load a `.env` file.

```
DEMO_LLM_ENABLED=true
CLOUDFLARE_ACCOUNT_ID=your-account-id
CLOUDFLARE_API_TOKEN=your-api-token
DEMO_LLM_MODEL=@cf/meta/llama-3-8b-instruct
DEMO_LLM_SYSTEM_PROMPT=You are a friendly, concise study buddy in Capy, a peer learning app. Reply in 1-2 sentences, be encouraging, and ask a short follow-up question when helpful.
DEMO_LLM_CHATTER_INTERVAL_MS=12000
```

### PowerShell example

```
$env:DEMO_LLM_ENABLED="true"
$env:CLOUDFLARE_ACCOUNT_ID="your-account-id"
$env:CLOUDFLARE_API_TOKEN="your-api-token"
$env:DEMO_LLM_MODEL="@cf/meta/llama-3-8b-instruct"
$env:DEMO_LLM_SYSTEM_PROMPT="You are a friendly, concise study buddy in Capy, a peer learning app. Reply in 1-2 sentences, be encouraging, and ask a short follow-up question when helpful."
$env:DEMO_LLM_CHATTER_INTERVAL_MS="12000"
```

### Demo flow

1. Enable demo users in the dashboard.
2. Start matchmaking and open a chat.
3. Demo users reply automatically and may send periodic chatter.

### Verify Workers AI token

Once logged in, you can call the verification endpoint to test the token:

```
GET /api/demo/llm/verify
```

It returns a small JSON result with `ok`, `status`, and `detail` fields.
