import json
import paho.mqtt.client as mqtt
from datetime import datetime

message = 'ON'
counter = 0


def on_connect(mosq, obj, rc):
    print("rc: " + str(rc))


def on_message(mosq, obj, msg):
    global counter
    # print(msg.topic + " " + str(msg.qos) + " " + str(msg.payload))
    message = msg.payload
    counter += 1
    print("counter: %s" % (str(counter)))
    jsonMsg = json.loads(msg.payload)
    # print(jsonMsg)
    nowTs = datetime.utcnow()
    # msgTs = datetime.strptime(jsonMsg['timestamp'], "%Y-%m-%dT%H:%M:%S.%fZ")
    msgTs = datetime.strptime(jsonMsg['deviceDataRaw']['ts'], "%Y-%m-%dT%H:%M:%S.%fZ")
    mId = jsonMsg['deviceDataRaw']['id']
    diff = (nowTs - msgTs).total_seconds()
    print("%s: %s -> %s : %s" % (
        mId, msgTs.strftime("%Y-%m-%d %H:%M:%S:%f%Z"), nowTs.strftime("%Y-%m-%d %H:%M:%S:%f%Z"), str(diff)))

def on_publish(mosq, obj, mid):
    print("mid: " + str(mid))


def on_subscribe(mosq, obj, mid, granted_qos):
    print("Subscribed: " + str(mid) + " " + str(granted_qos))


def on_log(mosq, obj, level, string):
    print(string)


deviceId = "145db162-5b94-452b-8093-576dcaf1627f"
username = "ubi"
password = "ubirch123"
host = "rmq.dev.ubirch.com"
# host = "mq.dev.ubirch.com"
port = 1883
# topic = "ubirch_dev/ubirch/devices/+/processed"
topic = "ubirch_dev/ubirch/devices/%s/processed" % (deviceId)
# topic = "ubirch_dev/ubirch/devices/%s/out" % (deviceId)

mqttc = mqtt.Client(client_id="py_tester_1")
mqttc.username_pw_set(username, password)

mqttc.on_message = on_message
# mqttc.on_connect = on_connect
# mqttc.on_publish = on_publish
# mqttc.on_subscribe = on_subscribe

mqttc.connect(host, port, 60)

mqttc.subscribe(topic, 1)

rc = 0
while rc == 0:
    rc = mqttc.loop()

print("rc: " + str(rc))
