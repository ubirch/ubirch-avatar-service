import socket
import binascii

# UDP_IP = "127.0.0.1"
# UDP_IP = "13.93.47.253"
# UDP_IP = "13.93.92.129" #demo@aws
# UDP_IP = "13.80.77.86"  # ubirch-dev@azure
UDP_IP = "23.101.65.255"  # ubirch-demo@azure
# UDP_IP = "udp.api.ubirch.dev.ubirch.com"

UDP_PORT = 9090

# MESSAGE = bytes("Hello, World!", "utf-8")
MESSAGE = binascii.unhexlify(
    "01CEB6877DCAD9513DB12CEF81C4FC8341C983783CD61569C68496612D86439B2BCE6DECCD6544BBCFF3A8DDEC7A6246B05FE590398CCAFF1690135C8622F096BD79324AE3BB1E087B2274656D70657261747572223A32337D")

print("UDP target IP:", UDP_IP)
print("UDP target port:", UDP_PORT)
print("message:", MESSAGE)

sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)  # UDP
sock.sendto(MESSAGE, (UDP_IP, UDP_PORT))
