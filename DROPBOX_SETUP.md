# Dropbox Setup Guide

## Why Dropbox?

Dropbox syncs directly with reMarkable:
- Connect reMarkable to Dropbox for automatic sync
- Articles saved as PDF, optimized for e-ink displays
- Free tier: 2GB storage (thousands of articles)
- Works on all cloud providers

## Setup Steps

### 1. Create Dropbox App

1. Go to: https://www.dropbox.com/developers/apps/create
2. Choose **"Scoped access"**
3. Choose **"Full Dropbox"** access
4. Name your app: `remark-drop`
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
DROPBOX_ACCESS_TOKEN=sl.your_actual_token_here
DROPBOX_UPLOAD_PATH=/reMarkable
```

Save (Ctrl+O, Enter) and exit (Ctrl+X).

### 4. Restart Service

```bash
systemctl restart remark-drop
systemctl status remark-drop
```

### 5. Test It

Share a Twitter/X article from your Android app. Check the logs:

```bash
journalctl -u remark-drop -f
```

You should see:
```
Converting 'Article Title' to PDF...
PDF generated (7385 bytes)
Uploaded to Dropbox: /reMarkable/Article_Title.pdf
```

Check your Dropbox: https://www.dropbox.com/home/reMarkable

## Connect reMarkable to Dropbox

1. On your reMarkable: **Settings → Storage → Connect cloud storage**
2. Or use the reMarkable mobile app to connect Dropbox
3. Articles will sync automatically

## Troubleshooting

### Error: "Dropbox not configured"
- Make sure `DROPBOX_ACCESS_TOKEN` is in .env
- Restart the service after updating .env

### Error: "Dropbox API error: 401"
- Token is incorrect or expired
- Generate a new token at https://www.dropbox.com/developers/apps

### Articles not appearing
- Check logs: `journalctl -u remark-drop -f`
- Verify the folder exists in Dropbox
- Try changing `DROPBOX_UPLOAD_PATH` to `/`

### Change upload folder
```env
DROPBOX_UPLOAD_PATH=/Articles
```

## Security

The access token gives full Dropbox access. Keep it secure:
- Don't share your .env file
- Don't commit it to git
- Revoke if compromised: https://www.dropbox.com/developers/apps
