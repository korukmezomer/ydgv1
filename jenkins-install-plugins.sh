#!/bin/bash
# Jenkins Pipeline plugin'lerini otomatik kur

JENKINS_URL="http://localhost:8082"
PLUGINS="workflow-aggregator pipeline-stage-view git"

echo "Jenkins plugin'lerini kuruyor..."
echo "Jenkins'in başlamasını bekliyorum..."

# Jenkins'in hazır olmasını bekle
for i in {1..30}; do
    if curl -s "$JENKINS_URL" > /dev/null 2>&1; then
        echo "Jenkins hazır!"
        break
    fi
    echo "Bekleniyor... ($i/30)"
    sleep 2
done

# Jenkins CLI ile plugin kur
echo "Plugin'ler kuruluyor..."
docker exec yazilimdogrulama-jenkins jenkins-plugin-cli --plugins $PLUGINS

echo "Plugin'ler kuruldu! Jenkins'i yeniden başlatıyorum..."
docker-compose restart jenkins

echo "Jenkins yeniden başlatıldı. Birkaç saniye bekleyin ve http://localhost:8082 adresini yenileyin."

