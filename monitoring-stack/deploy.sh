#!/bin/bash
cd ~/File-Conversion/monitoring-stack
docker-compose down
docker-compose up -d
echo "✅ Monitoring Stack has been updated and restarted!"