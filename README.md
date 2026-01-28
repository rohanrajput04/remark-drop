# Remark Drop

Push Twitter/X threads to your Kindle or reMarkable with a single tap from your phone.

## How It Works

```
┌─────────────┐      POST /send-to-kindle      ┌─────────────┐
│ Android App │ ─────────────────────────────▶ │  VPS Server │
└─────────────┘                                 └──────┬──────┘
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
                                                │   Dropbox   │──────▶ E-Reader
                                                │   or Email  │
                                                └─────────────┘
```

1. Share a Twitter/X URL from your Android phone
2. Android app POSTs it to your server
3. Playwright fetches the page with your auth cookies
4. Readability extracts clean article content
5. PDF is uploaded to Dropbox (or emailed to Kindle)

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

See `.env.example` for all configuration options:
- **Dropbox** (recommended): Simple file upload, works everywhere
- **Mailgun**: Email API that bypasses cloud SMTP restrictions
- **SMTP**: Direct email (blocked on most cloud hosts)

### Run

```bash
# Development
uvicorn main:app --host 0.0.0.0 --port 3000

# Production - use systemd (see deploy/)
```

## API

### POST /send-to-kindle

```bash
curl -X POST http://localhost:3000/send-to-kindle \
  -H "Content-Type: application/json" \
  -d '{"url": "https://x.com/user/status/123"}'
```

```json
{
  "success": true,
  "title": "Article Title",
  "message": "Article sent to Kindle!"
}
```

Returns 409 if the article was already sent (dedup).

### GET /

Health check.

## Stack

- Python 3.11+
- FastAPI
- Playwright
- readability-lxml
- BeautifulSoup4
- WeasyPrint (PDF generation)
