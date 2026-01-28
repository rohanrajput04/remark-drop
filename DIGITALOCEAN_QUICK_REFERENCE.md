# Remark Drop Quick Deployment Reference

## DigitalOcean Setup

### 1. Create Droplet
- Image: **Ubuntu 22.04 LTS**
- Plan: **$6/month (1GB RAM)**
- Add SSH key
- Create

### 2. SSH into Your Droplet
```bash
ssh root@YOUR_DROPLET_IP
```

### 3. Run Automated Deploy
```bash
# Download and run deployment script
curl -O https://raw.githubusercontent.com/YOUR_USERNAME/remark-drop/main/deploy/digitalocean-deploy.sh
chmod +x digitalocean-deploy.sh
./digitalocean-deploy.sh
```

### 4. Setup Mailgun (Required - SMTP is blocked on DigitalOcean)

DigitalOcean blocks SMTP ports to prevent spam. You must use Mailgun API instead:

1. **Sign up at Mailgun**: https://www.mailgun.com/ (free tier: 5,000 emails/month)
2. **Verify your domain** OR **use sandbox domain** for testing
3. **Get API credentials**:
   - Go to: https://app.mailgun.com/settings/api_security
   - Copy your **Private API key**
   - Note your **domain** (e.g., `sandboxXXX.mailgun.org` or your verified domain)
4. **Add Kindle to approved recipients** (if using sandbox):
   - Go to Sending → Authorized Recipients
   - Add your Kindle email address

### 5. Configure Environment
```bash
nano /opt/remark-drop/.env
```

Add these lines:
```env
# Twitter cookies
TWITTER_AUTH_TOKEN=your_auth_token_here
TWITTER_CT0=your_ct0_token_here

# Email to Kindle (REQUIRED)
KINDLE_EMAIL=your_kindle@kindle.com

# Mailgun API (REQUIRED for DigitalOcean)
MAILGUN_API_KEY=your_mailgun_api_key
MAILGUN_DOMAIN=sandboxXXX.mailgun.org
FROM_EMAIL=your-email@example.com
```

### 6. Authorize Kindle Email

Add `FROM_EMAIL` to your Kindle's approved email list:
1. Go to: https://www.amazon.com/hz/mycd/myx#/home/settings/payment
2. Scroll to **Personal Document Settings**
3. Under **Approved Personal Document E-mail List**, add your `FROM_EMAIL`

### 7. Start Service
```bash
systemctl enable remark-drop
systemctl start remark-drop
systemctl status remark-drop
```

### 8. Test It
```bash
# From anywhere
curl http://YOUR_DROPLET_IP:3000/

# You should see:
# {"status":"ok","service":"remark-drop"}
```

## Android App Configuration

In your Android app, set the API endpoint to:
```
http://YOUR_DROPLET_IP:3000/send-to-kindle
```

## Useful Commands

**View logs:**
```bash
journalctl -u remark-drop -f
```

**Restart service:**
```bash
systemctl restart remark-drop
```

**Stop service:**
```bash
systemctl stop remark-drop
```

**Check if running:**
```bash
netstat -tlnp | grep 3000
```

**Update code:**
```bash
cd /opt/remark-drop
git pull
systemctl restart remark-drop
```

## Troubleshooting

**Service won't start?**
```bash
journalctl -u remark-drop -n 50 -p err
```

**Port already in use?**
```bash
sudo lsof -i :3000
```

**Need to reinstall dependencies?**
```bash
cd /opt/remark-drop
source .venv/bin/activate
uv pip install -e .
```
