# Dropbox Setup Guide for Remark Drop

## Why Dropbox?

Dropbox is the **simplest way** to save articles from your Android phone:
- No email authorization needed
- Works on all cloud providers (no port blocking issues)
- Free tier: 2GB storage (plenty for articles)
- Access files from any device
- **Articles saved as PDF** - perfect for reMarkable 2, Kindle, and other e-readers
- Optimized formatting for e-ink displays

## Setup Steps

### 1. Create Dropbox App

1. Go to: https://www.dropbox.com/developers/apps/create
2. Choose **"Scoped access"**
3. Choose **"Full Dropbox"** access
4. Name your app: `remark-drop` (or any name you like)
5. Click **"Create app"**

### 2. Generate Access Token

1. On your app's page, scroll to **"OAuth 2"** section
2. Under **"Generated access token"**, click **"Generate"**
3. Copy the access token (starts with `sl.`)
4. **Important:** Save this token - you won't be able to see it again!

### 3. Configure Server

SSH into your server and update the .env file:

```bash
ssh root@YOUR_SERVER_IP
nano /opt/remark-drop/.env
```

Add these lines:
```env
# Dropbox Configuration
DROPBOX_ACCESS_TOKEN=sl.your_actual_token_here
DROPBOX_UPLOAD_PATH=/Kindle
```

**Note:** Articles will be saved to `/Kindle/` folder in your Dropbox. You can change this path if you want.

Save (Ctrl+O, Enter) and exit (Ctrl+X).

### 4. Restart Service

```bash
systemctl restart remark-drop
systemctl status remark-drop
```

### 5. Test It

Try sharing a Twitter/X article from your Android app. Check the logs:

```bash
journalctl -u remark-drop -f
```

You should see:
```
Converting 'Article Title' to PDF...
PDF generated (7385 bytes)
Uploaded to Dropbox: /Kindle/Article_Title.pdf
```

Then check your Dropbox at: https://www.dropbox.com/home/Kindle

You should see the PDF files there!

## Reading Articles on Your Devices

Once articles are in Dropbox as PDFs, you have multiple options:

### Option A: reMarkable 2 (Direct Sync)
1. **Connect reMarkable to Dropbox**:
   - On your reMarkable: Settings → Storage → Connect cloud storage
   - Or use the reMarkable mobile app to connect Dropbox
2. **Auto-sync**: Articles will appear automatically in your reMarkable
3. **Perfect reading experience**: PDFs are optimized for e-ink displays

### Option B: Kindle
1. **Send to Kindle**: Use Amazon's "Send to Kindle"
   - Email: https://www.amazon.com/sendtokindle
   - App: Download Send to Kindle app for desktop/mobile
2. **From Dropbox**: Download PDF and email/upload via Send to Kindle

### Option C: Any Device
PDFs can be opened on:
- iPad/iPhone (native Files app)
- Android (Google Drive, Dropbox app)
- Computer (any PDF reader)
- Other e-readers that support PDF

## Troubleshooting

### Error: "Dropbox not configured"
- Make sure you added `DROPBOX_ACCESS_TOKEN` to .env
- Make sure you restarted the service after updating .env

### Error: "Dropbox API error: 401 - invalid_access_token"
- Your access token is incorrect or expired
- Generate a new access token from: https://www.dropbox.com/developers/apps
- Update .env and restart service

### Error: "Dropbox API error: 403 - disallowed_shared_link_policy"
- This shouldn't happen with Full Dropbox access
- Check your app's permissions at https://www.dropbox.com/developers/apps
- Make sure it has "Full Dropbox" access

### Articles not appearing in Dropbox
- Check the logs: `journalctl -u remark-drop -f`
- Look for "PDF generated" and "Uploaded to Dropbox" messages
- Check the correct folder in Dropbox (default is `/Kindle/`)
- Try changing `DROPBOX_UPLOAD_PATH` to `/` to save in root

### PDF generation errors
- Check logs: `journalctl -u remark-drop -n 50`
- WeasyPrint requires system libraries - they should be auto-installed
- If issues persist, check WeasyPrint installation: `cd /opt/remark-drop && .venv/bin/python -c "import weasyprint; print('OK')"`

### Want to change the upload folder?
Update `DROPBOX_UPLOAD_PATH` in .env:
```env
DROPBOX_UPLOAD_PATH=/Articles
# or
DROPBOX_UPLOAD_PATH=/My Documents/Kindle
```

## Cost

- **Free tier**: 2GB storage
- Articles are typically 50-500KB each
- **Estimate**: Can store thousands of articles for free
- No monthly email limits like Mailgun

## Security Note

The access token gives full access to your Dropbox. Keep it secure:
- Don't share your .env file
- Don't commit it to git (already in .gitignore)
- If compromised, revoke the token at: https://www.dropbox.com/developers/apps
