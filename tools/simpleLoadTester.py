import paho.mqtt.client as mqtt
import hashlib
import base64
import json
import random
import time

from datetime import datetime

username = "ubi"
password = "ubirch123"
mqttc = mqtt.Client(client_id="receiver1")
mqttc.username_pw_set(username, password)
mqttc.connect("mq2.dev.ubirch.com", 1883, 60)

url = "http://api.ubirch.dev.ubirch.com:8080/api/avatarService/v1/device/update"

deviceId = "145db162-5b94-452b-8093-576dcaf1627f"
hHwDeviceId = "NieQminDE4Ggcewn98nKl3Jhgq7Smn3dLlQ1MyLPswq7njpt8qwsIP4jQ2MR1nhWTQyNMFkwV19g4tPQSBhNeQ=="
topic = "ubirch_dev/ubirch/devices/%s/in"

for i in range(10):
    print i
    message = {
        "v": "0.0.0",
        "a": hHwDeviceId,
        "p": {
            "t": 3059 + random.randint(1, 10),
            "p": 101304 + random.randint(1, 10),
            "h": 3252 + random.randint(1, 50),
            "a": 175 + random.randint(1, 10),
            "la": "52.478682",
            "lo": "13.369360",
            "ba": 90 + random.randint(1, 10),
            "lp": 0,
            "e": 0
        }
    }
    jsonMsg = json.dumps(message)
    currentTopic = topic % (deviceId)
    mqttc.reconnect()
    mqttc.publish(topic=currentTopic, payload=jsonMsg, qos=1)
    time.sleep(1)
