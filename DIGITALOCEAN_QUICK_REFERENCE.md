# Remark Drop - DigitalOcean Deployment

## Quick Setup

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
curl -O https://raw.githubusercontent.com/YOUR_USERNAME/remark-drop/main/deploy/digitalocean-deploy.sh
chmod +x digitalocean-deploy.sh
./digitalocean-deploy.sh
```

### 4. Configure Environment
```bash
nano /opt/remark-drop/.env
```

Add:
```env
# Twitter cookies (from browser DevTools > Application > Cookies > x.com)
TWITTER_AUTH_TOKEN=your_auth_token_here
TWITTER_CT0=your_ct0_token_here

# Dropbox (see DROPBOX_SETUP.md)
DROPBOX_ACCESS_TOKEN=your_dropbox_token
DROPBOX_UPLOAD_PATH=/reMarkable
```

### 5. Start Service
```bash
systemctl enable remark-drop
systemctl start remark-drop
systemctl status remark-drop
```

### 6. Test It
```bash
curl http://YOUR_DROPLET_IP:3000/

# Should return: {"status":"ok","service":"remark-drop"}
```

## Android App Configuration

Set the API endpoint to:
```
http://YOUR_DROPLET_IP:3000/send
```

## Useful Commands

```bash
# View logs
journalctl -u remark-drop -f

# Restart service
systemctl restart remark-drop

# Stop service
systemctl stop remark-drop

# Check if running
netstat -tlnp | grep 3000

# Update code
cd /opt/remark-drop && git pull && systemctl restart remark-drop
```

## Troubleshooting

```bash
# Service won't start
journalctl -u remark-drop -n 50 -p err

# Port in use
sudo lsof -i :3000

# Reinstall dependencies
cd /opt/remark-drop && source .venv/bin/activate && uv pip install -e .
```
