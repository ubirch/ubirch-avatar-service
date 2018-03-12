import uuid

x = uuid.UUID('5D9555164272A9DCF25BAB840B6AD75D')

print(x)

import uuid

o = {
    "name": "Unknown",
    "parent": "Uncategorized",
    "uuid": "06335e84-2872-4914-8c5d-3ed07d2a2f16"
}

print uuid.UUID(o['uuid']).hex
