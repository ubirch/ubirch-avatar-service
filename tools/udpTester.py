import socket
import binascii

UDP_IP = "127.0.0.1"
UDP_PORT = 9090

MESSAGE = bytes("Hello, World!", "utf-8")
# MESSAGE = binascii.unhexlify("cebc9ab239ac7b2274657374223a3132337dcebc9ab239b47b2274657374223a2276616c756520313233227d")

print("UDP target IP:", UDP_IP)
print("UDP target port:", UDP_PORT)
print("message:", MESSAGE)

sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)  # UDP
sock.sendto(MESSAGE, (UDP_IP, UDP_PORT))
