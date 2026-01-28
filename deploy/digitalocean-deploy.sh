#!/bin/bash
set -e

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo -e "${GREEN}=== Remark Drop DigitalOcean Deployment ===${NC}"

if [[ $EUID -ne 0 ]]; then
   echo -e "${RED}This script must be run as root${NC}"
   exit 1
fi

echo -e "${YELLOW}Step 1: Updating system${NC}"
apt update && apt upgrade -y

echo -e "${YELLOW}Step 2: Installing system dependencies${NC}"
apt install -y python3-pip python3-venv git curl

echo -e "${YELLOW}Step 3: Installing Playwright dependencies${NC}"
apt install -y libnss3 libatk1.0-0 libatk-bridge2.0-0 libcups2 libxcomposite1 \
    libxrandr2 libxdamage1 libpangocairo-1.0-0 libpango-1.0-0 libgbm1 libasound2t64 \
    libxshmfence1 libglu1-mesa libxss1 libappindicator1 libindicator7

echo -e "${YELLOW}Step 4: Creating app directory${NC}"
mkdir -p /opt/remark-drop
cd /opt/remark-drop

echo -e "${YELLOW}Step 5: Cloning repository${NC}"
if [ ! -d ".git" ]; then
    git clone https://github.com/YOUR_USERNAME/remark-drop.git .
fi

echo -e "${YELLOW}Step 6: Setting up Python virtual environment${NC}"
python3 -m venv .venv
source .venv/bin/activate

echo -e "${YELLOW}Step 7: Installing Python dependencies${NC}"
pip install --upgrade pip
pip install uv
uv pip install -e .

echo -e "${YELLOW}Step 8: Installing Playwright browsers${NC}"
playwright install chromium

echo -e "${YELLOW}Step 9: Setting up systemd service${NC}"
cp deploy/remark-drop.service /etc/systemd/system/
systemctl daemon-reload

echo -e "${GREEN}=== Deployment Setup Complete! ===${NC}"
echo ""
echo -e "${YELLOW}Next steps:${NC}"
echo "1. Create .env file:"
echo "   nano /opt/remark-drop/.env"
echo ""
echo "2. Add your environment variables:"
echo "   TWITTER_AUTH_TOKEN=your_token"
echo "   TWITTER_CT0=your_ct0"
echo "   DROPBOX_ACCESS_TOKEN=your_dropbox_token"
echo "   DROPBOX_UPLOAD_PATH=/reMarkable"
echo ""
echo "3. Enable and start service:"
echo "   systemctl enable remark-drop"
echo "   systemctl start remark-drop"
echo ""
echo "4. Check status:"
echo "   systemctl status remark-drop"
