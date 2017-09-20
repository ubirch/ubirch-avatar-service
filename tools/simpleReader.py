import json
import paho.mqtt.client as mqtt
from datetime import datetime

message = 'ON'
counter = 0


def on_connect(mosq, obj, rc):
    print("rc: " + str(rc))


def on_message(mosq, obj, msg):
    print(msg.topic + " " + str(msg.qos) + " " + str(msg.payload))


def on_publish(mosq, obj, mid):
    print("mid: " + str(mid))


def on_subscribe(mosq, obj, mid, granted_qos):
    print("Subscribed: " + str(mid) + " " + str(granted_qos))


def on_log(mosq, obj, level, string):
    print(string)


deviceId = "+"
username = "telekom"
password = "SmartPublicLife2017"
host = "mq.demo.ubirch.com"
# host = "mq.dev.ubirch.com"
port = 1883
topic = "ubirch_demo/ubirch/devices/+/processed"
# topic = "ubirch_demo/ubirch/devices/%s/processed" % (deviceId)
# topic = "ubirch_dev/ubirch/devices/%s/out" % (deviceId)

mqttc = mqtt.Client(client_id="py_tester_1")
mqttc.username_pw_set(username, password)

mqttc.on_message = on_message
mqttc.on_connect = on_connect
mqttc.on_publish = on_publish
mqttc.on_subscribe = on_subscribe
mqttc.connect(host, port, 60)
mqttc.subscribe(topic, 1)

rc = 0
while rc == 0:
    rc = mqttc.loop()

print("rc: " + str(rc))
