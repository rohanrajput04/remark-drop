# Remark Drop

Save Twitter/X threads to your reMarkable with a single tap from your phone.

## How It Works

```
┌─────────────┐        POST /send        ┌─────────────┐
│ Android App │ ───────────────────────▶ │  VPS Server │
└─────────────┘                          └──────┬──────┘
                                                │
                                                ▼
                                         ┌─────────────┐
                                         │  Playwright │
                                         │  + Cookies  │
                                         └──────┬──────┘
                                                │
                                                ▼
                                         ┌─────────────┐
                                         │ Readability │
                                         │  (Extract)  │
                                         └──────┬──────┘
                                                │
                                                ▼
                                         ┌─────────────┐
                                         │   Dropbox   │──▶ reMarkable
                                         │    (PDF)    │
                                         └─────────────┘
```

1. Share a Twitter/X URL from your Android phone
2. Android app POSTs it to your server
3. Playwright fetches the page with your auth cookies
4. Readability extracts clean article content
5. PDF is uploaded to Dropbox and syncs to reMarkable

## Setup

### Server

```bash
git clone https://github.com/YOUR_USERNAME/remark-drop.git
cd remark-drop
python3 -m venv .venv
source .venv/bin/activate
pip install uv
uv pip install -e .
playwright install chromium
```

### Environment Variables

Copy `.env.example` to `.env` and fill in your values:

```bash
cp .env.example .env
```

You need:
- **Twitter cookies**: Get from browser DevTools > Application > Cookies > x.com
- **Dropbox token**: Create app at https://www.dropbox.com/developers/apps

### Run

```bash
# Development
uvicorn main:app --host 0.0.0.0 --port 3000

# Production - use systemd (see deploy/)
```

## API

### POST /send

```bash
curl -X POST http://localhost:3000/send \
  -H "Content-Type: application/json" \
  -d '{"url": "https://x.com/user/status/123"}'
```

```json
{
  "success": true,
  "title": "Article Title",
  "message": "Article saved to Dropbox!"
}
```

Returns 409 if the article was already saved (dedup).

### GET /

Health check.

## Stack

- Python 3.11+
- FastAPI
- Playwright
- readability-lxml
- WeasyPrint (PDF generation)
