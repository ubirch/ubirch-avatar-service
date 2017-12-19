import paho.mqtt.client as mqtt

# topic = "ubirch/data"
topic = "ubirch/test"
host = "iot-test.westeurope.cloudapp.azure.com"
port = 8883
user = "ubirch"
pwd = "k33p!tS4f3@411T!m35"
crt = "/keybase/team/ubirchdevops/munichRE/mqtt.cert"


# The callback for when the client receives a CONNACK response from the server.
def on_connect(client, userdata, flags, rc):
    print("Connected with result code " + str(rc))

    # Subscribing in on_connect() means that if we lose the connection and
    # reconnect then subscriptions will be renewed.
    client.subscribe("$SYS/#")


# The callback for when a PUBLISH message is received from the server.
def on_message(client, userdata, msg):
    print(msg.topic + " " + str(msg.payload))


client = mqtt.Client()
client.on_connect = on_connect
client.on_message = on_message

client.tls_set(ca_certs=crt)
client.username_pw_set(user, pwd)

client.connect(
    host, port, 60
)

client.subscribe(topic)

# Blocking call that processes network traffic, dispatches callbacks and
# handles reconnecting.
# Other loop*() functions are available that give a threaded interface and a
# manual interface.
client.loop_forever()
