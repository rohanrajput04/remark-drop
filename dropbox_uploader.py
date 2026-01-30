"""
Dropbox uploader for reMarkable.
Uploads articles as PDF files to your Dropbox.
Supports refresh tokens for long-lived access.
"""

import os
import io
import requests
from weasyprint import HTML
from dotenv import load_dotenv

load_dotenv()

# Cache for refreshed access token
_cached_access_token = None


def refresh_access_token() -> str:
    """
    Refresh the Dropbox access token using refresh token.
    Returns a new access token.
    """
    app_key = os.getenv("DROPBOX_APP_KEY")
    app_secret = os.getenv("DROPBOX_APP_SECRET")
    refresh_token = os.getenv("DROPBOX_REFRESH_TOKEN")

    if not all([app_key, app_secret, refresh_token]):
        raise ValueError(
            "Missing refresh token config. Need: DROPBOX_APP_KEY, DROPBOX_APP_SECRET, DROPBOX_REFRESH_TOKEN"
        )

    response = requests.post(
        "https://api.dropboxapi.com/oauth2/token",
        data={
            "grant_type": "refresh_token",
            "refresh_token": refresh_token,
        },
        auth=(app_key, app_secret),
        timeout=30,
    )

    if response.status_code == 200:
        data = response.json()
        return data["access_token"]
    else:
        raise Exception(f"Failed to refresh token: {response.status_code} - {response.text}")


def get_dropbox_config() -> dict:
    """Load Dropbox configuration from environment variables."""
    global _cached_access_token

    # Try refresh token first (preferred for long-lived access)
    refresh_token = os.getenv("DROPBOX_REFRESH_TOKEN")
    if refresh_token and not _cached_access_token:
        try:
            _cached_access_token = refresh_access_token()
            print("✓ Refreshed Dropbox access token")
        except Exception as e:
            print(f"Token refresh failed: {e}")
            # Fall back to static access token
            _cached_access_token = None

    access_token = _cached_access_token or os.getenv("DROPBOX_ACCESS_TOKEN")

    config = {
        "access_token": access_token,
        "upload_path": os.getenv("DROPBOX_UPLOAD_PATH", "/reMarkable"),
    }

    if not config["access_token"]:
        raise ValueError("Missing DROPBOX_ACCESS_TOKEN or DROPBOX_REFRESH_TOKEN in .env")

    return config


def upload_to_dropbox(title: str, html_content: str) -> bool:
    """
    Upload an article to Dropbox as a PDF file (reMarkable compatible).

    Args:
        title: Article title (used for filename)
        html_content: The formatted HTML content

    Returns:
        True if uploaded successfully
    """
    try:
        config = get_dropbox_config()
    except ValueError as e:
        print(f"Dropbox config error: {e}")
        raise Exception(f"Dropbox not configured: {e}")

    # Convert HTML to PDF
    try:
        print(f"Converting '{title}' to PDF...")

        # Add basic CSS for better reading on reMarkable
        styled_html = f"""
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8">
            <style>
                @page {{
                    size: A4;
                    margin: 2cm;
                }}
                body {{
                    font-family: 'Helvetica', 'Arial', sans-serif;
                    font-size: 12pt;
                    line-height: 1.6;
                    color: #000;
                    max-width: 800px;
                    margin: 0 auto;
                }}
                h1 {{
                    font-size: 20pt;
                    margin-bottom: 0.5em;
                    line-height: 1.2;
                }}
                h2 {{
                    font-size: 16pt;
                    margin-top: 1em;
                    margin-bottom: 0.5em;
                }}
                p {{
                    margin-bottom: 1em;
                    text-align: justify;
                }}
                a {{
                    color: #000;
                    text-decoration: underline;
                }}
                img {{
                    max-width: 100%;
                    height: auto;
                }}
            </style>
        </head>
        <body>
            {html_content}
        </body>
        </html>
        """

        # Generate PDF in memory
        pdf_buffer = io.BytesIO()
        HTML(string=styled_html).write_pdf(pdf_buffer)
        pdf_data = pdf_buffer.getvalue()

        print(f"PDF generated ({len(pdf_data)} bytes)")

    except Exception as e:
        print(f"Failed to convert HTML to PDF: {e}")
        raise Exception(f"PDF conversion failed: {e}")

    # Sanitize filename and use .pdf extension
    filename = _sanitize_filename(title) + ".pdf"
    dropbox_path = f"{config['upload_path']}/{filename}".replace("//", "/")

    # Dropbox API endpoint
    url = "https://content.dropboxapi.com/2/files/upload"

    headers = {
        "Authorization": f"Bearer {config['access_token']}",
        "Content-Type": "application/octet-stream",
        "Dropbox-API-Arg": f'{{"path": "{dropbox_path}", "mode": "add", "autorename": true}}',
    }

    try:
        response = requests.post(
            url,
            headers=headers,
            data=pdf_data,
            timeout=30,
        )

        if response.status_code == 200:
            result = response.json()
            print(f"✓ Uploaded to Dropbox: {result['path_display']}")
            return True
        elif response.status_code == 401:
            # Token expired - try to refresh and retry once
            global _cached_access_token
            refresh_token = os.getenv("DROPBOX_REFRESH_TOKEN")
            if refresh_token:
                print("Access token expired, refreshing...")
                _cached_access_token = refresh_access_token()
                headers["Authorization"] = f"Bearer {_cached_access_token}"
                retry_response = requests.post(url, headers=headers, data=pdf_data, timeout=30)
                if retry_response.status_code == 200:
                    result = retry_response.json()
                    print(f"✓ Uploaded to Dropbox: {result['path_display']}")
                    return True
                else:
                    error_msg = retry_response.text[:200]
                    print(f"Dropbox API error after refresh {retry_response.status_code}: {error_msg}")
                    raise Exception(f"Dropbox API error: {retry_response.status_code} - {error_msg}")
            else:
                print("Dropbox API error 401: Token expired. Add DROPBOX_REFRESH_TOKEN for auto-refresh.")
                raise Exception("Dropbox token expired. Configure refresh token for auto-renewal.")
        else:
            # Try to parse error as JSON, fallback to text
            try:
                error_data = response.json()
                error_msg = error_data.get("error_summary", str(error_data))
            except:
                error_msg = response.text[:200]  # First 200 chars of response

            print(f"Dropbox API error {response.status_code}: {error_msg}")
            raise Exception(f"Dropbox API error: {response.status_code} - {error_msg}")

    except requests.exceptions.RequestException as e:
        print(f"Failed to upload to Dropbox: {e}")
        raise Exception(f"Dropbox upload failed: {e}")
    except Exception as e:
        if "Dropbox API error" in str(e):
            raise
        print(f"Unexpected error: {e}")
        raise Exception(f"Dropbox upload failed: {e}")


def _sanitize_filename(title: str) -> str:
    """Sanitize title for use as filename."""
    invalid_chars = '<>:"/\\|?*'
    filename = title.translate(str.maketrans("", "", invalid_chars))
    filename = filename[:100].strip()
    return filename or "article"


if __name__ == "__main__":
    # Test Dropbox upload (requires valid config)
    test_html = """
    <h1>Test Article</h1>
    <p>This is a test article uploaded to Dropbox as a PDF for reMarkable.</p>
    <h2>Features</h2>
    <ul>
        <li>Clean formatting</li>
        <li>Optimized for e-ink displays</li>
        <li>reMarkable 2 compatible</li>
    </ul>
    """

    try:
        upload_to_dropbox("Test Article from Remark Drop", test_html)
        print("Upload successful!")
    except ValueError as e:
        print(f"Config error: {e}")
    except Exception as e:
        print(f"Upload error: {e}")
