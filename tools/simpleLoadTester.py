import hashlib
import json
import paho.mqtt.client as mqtt
import random
import time

username = "ubi"
password = "ubirch123"
mqttc = mqtt.Client(client_id="receiver1")
mqttc.username_pw_set(username, password)
mqttc.connect("mq2.dev.ubirch.com", 1883, 60)

url = "http://api.ubirch.dev.ubirch.com:8080/api/avatarService/v1/device/update"

devices = [
    {
        "deviceId": "145db162-5b94-452b-8093-576dcaf1627f",
        "hHwDeviceId": "NieQminDE4Ggcewn98nKl3Jhgq7Smn3dLlQ1MyLPswq7njpt8qwsIP4jQ2MR1nhWTQyNMFkwV19g4tPQSBhNeQ=="
    },
    {
        "deviceId": "14067148-0c42-4c91-b02b-c9b0614c168d",
        "hHwDeviceId": "WAnOUyY4y1FCJXr9GqRgODP8zqD1T1vyHaURHBpFLux+LM7bfCCvoSNS2vmhXovX7+R9mVaVr1f3tDd6FsiBKA=="
    },
    {
        "deviceId": "881fa441-e26b-4f4c-8f43-2ab0fb9dfcd6",
        "hHwDeviceId": "gfSHRAJj3pTK9cUgsYuPEY0jmbCct6gW1/w+v+NVcxxUa5p/1ag+N+bKRdhDupiGxO9tC0+YPHnKhtCluGtKzg=="
    }
]

topic = "ubirch_dev/ubirch/devices/%s/in"

for i in range(1000):
    print i
    for d in devices:
        did = d["deviceId"]
        hwdid = d["hHwDeviceId"]
        message = {
            "v": "0.0.0",
            "a": hwdid,
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
        currentTopic = topic % (did)
        mqttc.reconnect()
        mqttc.publish(topic=currentTopic, payload=jsonMsg, qos=1)
    time.sleep(0.5)
