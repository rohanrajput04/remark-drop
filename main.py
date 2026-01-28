"""
Remark Drop - FastAPI server for saving Twitter articles to Dropbox.
"""

import os
import re
from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel, HttpUrl
from extractor import extract_article, AuthExpiredError
from dropbox_uploader import upload_to_dropbox

SENT_LOG = "sent_articles.txt"


def normalize_url(url: str) -> str:
    """Normalize Twitter URL to canonical form for dedup."""
    url = re.sub(r"https?://(www\.)?twitter\.com", "https://x.com", url)
    url = url.split("?")[0].rstrip("/")
    return url


def was_already_sent(url: str) -> bool:
    """Check if URL was already processed."""
    if not os.path.exists(SENT_LOG):
        return False
    normalized = normalize_url(url)
    with open(SENT_LOG, "r") as f:
        return normalized in {line.strip() for line in f}


def mark_as_sent(url: str) -> None:
    """Record URL as processed."""
    normalized = normalize_url(url)
    with open(SENT_LOG, "a") as f:
        f.write(normalized + "\n")


app = FastAPI(
    title="Remark Drop",
    description="Save Twitter/X articles to Dropbox for reMarkable",
    version="0.1.0",
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


class SendRequest(BaseModel):
    url: HttpUrl


class SendResponse(BaseModel):
    success: bool
    title: str
    message: str


@app.get("/")
def health_check() -> dict:
    """Health check endpoint."""
    return {"status": "ok", "service": "remark-drop"}


@app.post("/send", response_model=SendResponse)
def save_article(request: SendRequest):
    """
    Extract a Twitter/X article and save it to Dropbox.

    - Fetches the article using Playwright + cookies
    - Extracts clean, formatted content
    - Converts to PDF and uploads to Dropbox
    """
    url = str(request.url)

    if "twitter.com" not in url and "x.com" not in url:
        raise HTTPException(
            status_code=400,
            detail="URL must be a Twitter/X link",
        )

    if was_already_sent(url):
        raise HTTPException(
            status_code=409,
            detail="Article already saved",
        )

    try:
        article = extract_article(url)
        upload_to_dropbox(article["title"], article["html"])
        mark_as_sent(url)

        return SendResponse(
            success=True,
            title=article["title"],
            message="Article saved to Dropbox!",
        )

    except AuthExpiredError:
        raise HTTPException(
            status_code=401,
            detail="Twitter cookies expired. Update TWITTER_AUTH_TOKEN and TWITTER_CT0 in .env",
        )
    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e))
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Failed to process: {str(e)}")


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=3000)
