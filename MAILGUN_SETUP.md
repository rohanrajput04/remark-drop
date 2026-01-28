# Mailgun Setup Guide for Remark Drop

## Why Mailgun?

DigitalOcean (and most cloud providers) **block outbound SMTP ports** (25, 465, 587) to prevent spam. This means Gmail SMTP won't work on your server.

**Mailgun provides an HTTP API** that works over port 443 (HTTPS), which is not blocked. It's the recommended solution for sending emails from cloud servers.

## Setup Steps

### 1. Create Mailgun Account

1. Go to: https://signup.mailgun.com/new/signup
2. Sign up (free tier includes 5,000 emails/month - plenty for personal use)
3. Verify your email address

### 2. Get API Credentials

1. Log into Mailgun: https://login.mailgun.com/login/
2. Go to **Sending** → **Domain settings** → **API keys**
   - Or direct link: https://app.mailgun.com/settings/api_security
3. Copy your **Private API key** (starts with `key-...`)
4. Note your **domain** (usually `sandboxXXX.mailgun.org` for sandbox)

### 3. Add Authorized Recipient (Sandbox Only)

If using the sandbox domain, you MUST authorize your Kindle email:

1. Go to **Sending** → **Domain settings** → **Authorized Recipients**
2. Click **Add Recipient**
3. Enter your Kindle email (e.g., `username_abc123@kindle.com`)
4. Verify the email (check your Kindle's email or Amazon account)

**Note:** Once you verify a custom domain, this step is not needed.

### 4. Add to Server Configuration

SSH into your server:
```bash
ssh root@YOUR_SERVER_IP
nano /opt/remark-drop/.env
```

Add these lines (or update existing ones):
```env
# Mailgun Configuration
MAILGUN_API_KEY=key-your_actual_key_here
MAILGUN_DOMAIN=sandboxXXXXXXXXXXX.mailgun.org
FROM_EMAIL=your-email@example.com

# Your Kindle email
KINDLE_EMAIL=username_abc123@kindle.com
```

Save (Ctrl+O, Enter) and exit (Ctrl+X).

### 5. Authorize Email in Amazon

1. Go to: https://www.amazon.com/hz/mycd/myx#/home/settings/payment
2. Scroll to **Personal Document Settings**
3. Under **Approved Personal Document E-mail List**, click **Add a new approved e-mail address**
4. Add your `FROM_EMAIL` (the one you specified in .env)
5. Save

### 6. Restart Service

```bash
systemctl restart remark-drop
systemctl status remark-drop
```

### 7. Test It

Try sharing a Twitter/X article from your Android app. Check the logs to see if email was sent:

```bash
journalctl -u remark-drop -f
```

You should see: `Email sent via Mailgun to your_kindle@kindle.com`

## Troubleshooting

### Error: "Mailgun API error: 401"
- Your API key is incorrect
- Make sure you copied the **Private API key**, not the public key

### Error: "Mailgun API error: 400 - sandbox domain requires recipient"
- You need to add your Kindle email as an authorized recipient in Mailgun
- Or verify a custom domain to remove this restriction

### Email not arriving on Kindle
- Check if `FROM_EMAIL` is in your Amazon approved sender list
- Check Mailgun logs: https://app.mailgun.com/app/sending/logs
- Make sure your Kindle has Wi-Fi connection

### Want to use custom domain instead of sandbox?
1. In Mailgun, go to **Sending** → **Domains** → **Add New Domain**
2. Follow DNS verification steps
3. Update `MAILGUN_DOMAIN` in .env to your verified domain
4. No need to authorize recipients for verified domains

## Cost

- **Free tier**: 5,000 emails/month (first 3 months)
- **After trial**: $1.50 per 1,000 emails
- For personal Kindle use, the free tier is usually sufficient
