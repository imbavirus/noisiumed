#!/usr/bin/env python3
"""Minimal Minecraft RCON client (Source RCON protocol)."""
import struct
import socket
import sys
import time


def _send(sock, req_id, req_type, body: str):
    payload = struct.pack("<ii", req_id, req_type) + body.encode("utf-8") + b"\x00\x00"
    sock.sendall(struct.pack("<i", len(payload)) + payload)


def _recv(sock):
    def read_exact(n):
        buf = b""
        while len(buf) < n:
            chunk = sock.recv(n - len(buf))
            if not chunk:
                raise ConnectionError("RCON closed")
            buf += chunk
        return buf

    (length,) = struct.unpack("<i", read_exact(4))
    data = read_exact(length)
    req_id, req_type = struct.unpack("<ii", data[:8])
    body = data[8:-2].decode("utf-8", errors="replace")
    return req_id, req_type, body


def rcon(host, port, password, command, timeout=30.0):
    sock = socket.create_connection((host, port), timeout=timeout)
    try:
        _send(sock, 1, 3, password)  # AUTH
        rid, rtype, body = _recv(sock)
        if rid == -1:
            raise PermissionError(f"RCON auth failed: {body}")
        _send(sock, 2, 2, command)  # EXEC
        # Some servers send empty first; read until non-empty or timeout
        sock.settimeout(timeout)
        rid, rtype, body = _recv(sock)
        return body
    finally:
        sock.close()


def main():
    if len(sys.argv) < 5:
        print("usage: rcon_cmd.py host port password command...", file=sys.stderr)
        sys.exit(2)
    host, port, password = sys.argv[1], int(sys.argv[2]), sys.argv[3]
    command = " ".join(sys.argv[4:])
    for attempt in range(5):
        try:
            print(rcon(host, port, password, command))
            return
        except Exception as e:
            if attempt == 4:
                print(f"RCON error: {e}", file=sys.stderr)
                sys.exit(1)
            time.sleep(1)


if __name__ == "__main__":
    main()
