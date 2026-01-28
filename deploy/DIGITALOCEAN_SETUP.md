# Deploying Remark Drop on DigitalOcean

## Prerequisites
- DigitalOcean account
- SSH key pair (or create one)
- Your environment variables ready

## Step 1: Create a Droplet

1. Go to [DigitalOcean Console](https://cloud.digitalocean.com)
2. Click "Create" → "Droplets"
3. **Choose Image:** Ubuntu 22.04 LTS x64
4. **Choose Plan:** Basic ($6/month or higher)
5. **Choose Region:** Pick closest to your location
6. **Authentication:** Select your SSH key (or create new)
7. **Hostname:** `remark-drop` or your preferred name
8. Click "Create Droplet"

## Step 2: Connect via SSH

```bash
ssh root@your_droplet_ip
```

## Step 3: Run Deployment Script

```bash
cd /tmp
curl -O https://raw.githubusercontent.com/YOUR_USERNAME/remark-drop/main/deploy/setup.sh
chmod +x setup.sh
sudo ./setup.sh
```

Or manually:

```bash
sudo apt update && sudo apt upgrade -y
sudo apt install -y python3-pip python3-venv git

# Install Playwright system dependencies
sudo apt install -y libnss3 libatk1.0-0 libatk-bridge2.0-0 libcups2 libxcomposite1 \
    libxrandr2 libxdamage1 libpangocairo-1.0-0 libpango-1.0-0 libgbm1 libasound2t64 \
    libxshmfence1 libglu1-mesa

mkdir -p /opt/remark-drop
cd /opt/remark-drop
sudo chown $USER /opt/remark-drop
```

## Step 4: Clone Repository

```bash
cd /opt/remark-drop
git clone https://github.com/YOUR_USERNAME/remark-drop.git .
```

## Step 5: Setup Virtual Environment

```bash
python3 -m venv .venv
source .venv/bin/activate
pip install uv
uv pip install -e .
playwright install chromium
```

## Step 6: Create .env File

```bash
sudo nano /opt/remark-drop/.env
```

Add your environment variables:
```
TWITTER_AUTH_TOKEN=your_auth_token
TWITTER_CT0=your_ct0_token
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USER=your_email@gmail.com
SMTP_PASS=your_app_password
KINDLE_EMAIL=your_kindle@kindle.com
```

Save with `Ctrl+X`, then `Y`, then `Enter`

## Step 7: Setup Systemd Service

```bash
sudo nano /etc/systemd/system/remark-drop.service
```

Paste this:
```ini
[Unit]
Description=Remark Drop - Twitter to Kindle/reMarkable
After=network.target

[Service]
Type=notify
User=root
WorkingDirectory=/opt/remark-drop
Environment="PATH=/opt/remark-drop/.venv/bin"
EnvironmentFile=/opt/remark-drop/.env
ExecStart=/opt/remark-drop/.venv/bin/uvicorn main:app --host 0.0.0.0 --port 3000
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
```

Save and exit.

## Step 8: Enable and Start Service

```bash
sudo systemctl daemon-reload
sudo systemctl enable remark-drop
sudo systemctl start remark-drop
```

## Step 9: Check Status

```bash
sudo systemctl status remark-drop
```

View logs:
```bash
sudo journalctl -u remark-drop -f
```

## Step 10: Setup Firewall (Optional but Recommended)

```bash
sudo ufw allow 22/tcp    # SSH
sudo ufw allow 80/tcp    # HTTP
sudo ufw allow 443/tcp   # HTTPS
sudo ufw enable
```

## Step 11: Get Your Public IP

```bash
curl ifconfig.me
```

Your app is now available at: `http://your_droplet_ip:3000`

## For HTTPS (Optional)

Install Let's Encrypt and Certbot:

```bash
sudo apt install -y certbot python3-certbot-nginx
```

Or use Nginx as reverse proxy for better production setup.

## Testing

From your Android device:
```
URL: http://your_droplet_ip:3000/send-to-kindle
Method: POST
Headers: Content-Type: application/json
Body: {"url": "https://x.com/user/status/123"}
```

## Troubleshooting

**Service won't start:**
```bash
sudo journalctl -u remark-drop -n 50
```

**Check if port 3000 is open:**
```bash
sudo netstat -tlnp | grep 3000
```

**Restart service:**
```bash
sudo systemctl restart remark-drop
```

**View real-time logs:**
```bash
sudo journalctl -u remark-drop -f
```
