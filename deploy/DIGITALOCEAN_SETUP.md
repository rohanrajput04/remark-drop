# Deploying Remark Drop on DigitalOcean

## Prerequisites
- DigitalOcean account
- SSH key pair
- Dropbox developer account

## Step 1: Create a Droplet

1. Go to [DigitalOcean Console](https://cloud.digitalocean.com)
2. Click "Create" → "Droplets"
3. **Choose Image:** Ubuntu 22.04 LTS x64
4. **Choose Plan:** Basic ($6/month or higher)
5. **Choose Region:** Pick closest to your location
6. **Authentication:** Select your SSH key
7. **Hostname:** `remark-drop`
8. Click "Create Droplet"

## Step 2: Connect via SSH

```bash
ssh root@your_droplet_ip
```

## Step 3: Run Deployment Script

```bash
cd /tmp
curl -O https://raw.githubusercontent.com/YOUR_USERNAME/remark-drop/main/deploy/digitalocean-deploy.sh
chmod +x digitalocean-deploy.sh
./digitalocean-deploy.sh
```

## Step 4: Create .env File

```bash
nano /opt/remark-drop/.env
```

Add:
```env
# Twitter cookies (from browser DevTools > Application > Cookies > x.com)
TWITTER_AUTH_TOKEN=your_auth_token
TWITTER_CT0=your_ct0_token

# Dropbox (see DROPBOX_SETUP.md)
DROPBOX_ACCESS_TOKEN=your_dropbox_token
DROPBOX_UPLOAD_PATH=/reMarkable
```

Save with `Ctrl+X`, then `Y`, then `Enter`

## Step 5: Enable and Start Service

```bash
systemctl enable remark-drop
systemctl start remark-drop
systemctl status remark-drop
```

## Step 6: Test

```bash
curl http://your_droplet_ip:3000/
# Should return: {"status":"ok","service":"remark-drop"}
```

## Android App Configuration

Set the API endpoint to:
```
http://your_droplet_ip:3000/send
```

## Useful Commands

```bash
# View logs
journalctl -u remark-drop -f

# Restart service
systemctl restart remark-drop

# Check if running
netstat -tlnp | grep 3000
```

## Troubleshooting

```bash
# Service won't start
journalctl -u remark-drop -n 50

# Restart service
systemctl restart remark-drop
```
