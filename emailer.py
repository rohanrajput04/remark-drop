"""
Email sender for Kindle.
Sends articles as HTML attachments to your Kindle email address.
"""

import os
import smtplib
import requests
from email.mime.multipart import MIMEMultipart
from email.mime.text import MIMEText
from email.mime.base import MIMEBase
from email import encoders
from dotenv import load_dotenv

load_dotenv()


def get_email_config() -> dict:
    """Load email configuration from environment variables."""
    config = {
        "smtp_host": os.getenv("SMTP_HOST", "smtp.gmail.com"),
        "smtp_port": int(os.getenv("SMTP_PORT", "587")),
        "smtp_user": os.getenv("SMTP_USER"),
        "smtp_pass": os.getenv("SMTP_PASS"),
        "kindle_email": os.getenv("KINDLE_EMAIL"),
        "from_email": os.getenv("FROM_EMAIL"),
        "mailgun_api_key": os.getenv("MAILGUN_API_KEY"),
        "mailgun_domain": os.getenv("MAILGUN_DOMAIN"),
    }

    # Use SMTP_USER as FROM_EMAIL if not specified
    if not config["from_email"]:
        config["from_email"] = config["smtp_user"]

    # Check if we have Mailgun config OR SMTP config
    has_mailgun = config["mailgun_api_key"] and config["mailgun_domain"]
    has_smtp = config["smtp_user"] and config["smtp_pass"]

    if not has_mailgun and not has_smtp:
        raise ValueError("Missing email config: need either Mailgun API or SMTP credentials")

    if not config["kindle_email"]:
        raise ValueError("Missing KINDLE_EMAIL")

    return config


def _sanitize_filename(title: str) -> str:
    """Sanitize title for use as filename."""
    invalid_chars = '<>:"/\\|?*'
    filename = title.translate(str.maketrans("", "", invalid_chars))
    filename = filename[:100].strip()
    return filename or "article"


def send_via_mailgun(config: dict, title: str, html_content: str) -> bool:
    """Send email via Mailgun HTTP API (works when SMTP is blocked)."""
    url = f"https://api.mailgun.net/v3/{config['mailgun_domain']}/messages"

    # Sanitize filename
    filename = _sanitize_filename(title) + ".html"

    # Prepare the request
    auth = ("api", config["mailgun_api_key"])
    data = {
        "from": config["from_email"] or f"Ink Drop <inkdrop@{config['mailgun_domain']}>",
        "to": config["kindle_email"],
        "subject": title,
        "text": f"Article: {title}\n\nSent via Ink Drop",
    }
    files = {
        "attachment": (filename, html_content.encode("utf-8"), "text/html")
    }

    response = requests.post(url, auth=auth, data=data, files=files, timeout=30)

    if response.status_code == 200:
        print(f"✓ Email sent via Mailgun to {config['kindle_email']}")
        return True
    else:
        raise Exception(f"Mailgun API error: {response.status_code} - {response.text}")


def send_to_kindle(title: str, html_content: str) -> bool:
    """
    Send an article to Kindle as an HTML attachment.

    Args:
        title: Article title (used for filename and subject)
        html_content: The formatted HTML content

    Returns:
        True if sent successfully
    """
    try:
        config = get_email_config()
    except ValueError as e:
        print(f"Email config error: {e}")
        raise Exception(f"Email not configured: {e}")

    # Try Mailgun API first if configured (works when SMTP is blocked)
    if config.get("mailgun_api_key") and config.get("mailgun_domain"):
        try:
            return send_via_mailgun(config, title, html_content)
        except Exception as e:
            print(f"Mailgun failed: {e}")
            # Don't raise yet, try SMTP as fallback

    # Fall back to SMTP (may not work on DigitalOcean/cloud providers)
    if not (config.get("smtp_user") and config.get("smtp_pass")):
        raise Exception("No email method available - configure Mailgun API or SMTP")

    # Create message
    msg = MIMEMultipart()
    msg["From"] = config["from_email"]
    msg["To"] = config["kindle_email"]
    msg["Subject"] = title

    # Add a simple body
    body = f"Article: {title}\n\nSent via Ink Drop"
    msg.attach(MIMEText(body, "plain"))

    # Create HTML attachment
    # Kindle accepts .html files - use MIMEText for better compatibility
    filename = _sanitize_filename(title) + ".html"

    attachment = MIMEText(html_content, "html", "utf-8")
    attachment.add_header(
        "Content-Disposition",
        "attachment",
        filename=filename,
    )
    msg.attach(attachment)

    # Send email via SMTP
    try:
        # Use SMTP_SSL for port 465, regular SMTP with STARTTLS for other ports
        if config["smtp_port"] == 465:
            with smtplib.SMTP_SSL(config["smtp_host"], config["smtp_port"], timeout=10) as server:
                server.login(config["smtp_user"], config["smtp_pass"])
                server.send_message(msg)
        else:
            with smtplib.SMTP(config["smtp_host"], config["smtp_port"], timeout=10) as server:
                server.starttls(timeout=10)
                server.login(config["smtp_user"], config["smtp_pass"])
                server.send_message(msg)
        print(f"✓ Email sent successfully via SMTP to {config['kindle_email']}")
        return True
    except (OSError, ConnectionRefusedError, smtplib.SMTPException, TimeoutError) as e:
        # Network error - likely server restrictions
        error_msg = f"SMTP connection blocked: {e}"
        print(error_msg)
        raise Exception(
            "SMTP is blocked by your hosting provider. "
            "Please configure Mailgun (MAILGUN_API_KEY and MAILGUN_DOMAIN in .env). "
            "Sign up at https://www.mailgun.com/"
        )
    except Exception as e:
        print(f"Failed to send email: {e}")
        raise Exception(f"Email failed: {e}")


def send_alert(subject: str, message: str) -> bool:
    """
    Send an alert email to yourself (SMTP_USER).
    Used for notifying about issues like expired cookies.
    """
    try:
        config = get_email_config()
    except ValueError as e:
        print(f"Email config error: {e}")
        return True

    msg = MIMEMultipart()
    msg["From"] = config["from_email"]
    msg["To"] = config["smtp_user"]  # Send to yourself
    msg["Subject"] = f"[Ink Drop Alert] {subject}"

    msg.attach(MIMEText(message, "plain"))

    try:
        # Use SMTP_SSL for port 465, regular SMTP with STARTTLS for other ports
        if config["smtp_port"] == 465:
            with smtplib.SMTP_SSL(config["smtp_host"], config["smtp_port"], timeout=10) as server:
                server.login(config["smtp_user"], config["smtp_pass"])
                server.send_message(msg)
        else:
            with smtplib.SMTP(config["smtp_host"], config["smtp_port"], timeout=10) as server:
                server.starttls(timeout=10)
                server.login(config["smtp_user"], config["smtp_pass"])
                server.send_message(msg)
        return True
    except (OSError, ConnectionRefusedError) as e:
        # Network error
        print(f"Network error sending alert: {e}")
        return True
    except Exception as e:
        print(f"Failed to send alert: {e}")
        return True


if __name__ == "__main__":
    # Test email sending (requires valid config)
    test_html = """
    <!DOCTYPE html>
    <html>
    <head><title>Test Article</title></head>
    <body>
        <h1>Test Article</h1>
        <p>This is a test article sent to Kindle.</p>
    </body>
    </html>
    """

    try:
        send_to_kindle("Test Article from Ink Drop", test_html)
        print("Email sent successfully!")
    except ValueError as e:
        print(f"Config error: {e}")
    except Exception as e:
        print(f"Send error: {e}")
