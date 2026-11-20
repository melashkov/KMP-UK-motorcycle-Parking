#!/usr/bin/env python3
"""Synthetic contributor API. Run with: python3 scripts/mock-api.py"""

import hashlib
import json
import math
import random
from http.server import BaseHTTPRequestHandler, HTTPServer
from urllib.parse import parse_qs, urlsplit


def parking_response(query):
    bounds = {key: float(query[key][0]) for key in ("north", "south", "east", "west")}
    north, south, east, west = (bounds[key] for key in ("north", "south", "east", "west"))
    if not all(math.isfinite(value) for value in bounds.values()):
        raise ValueError("Bounds must be finite")
    if not -90 <= south < north <= 90 or not (-180 <= west <= 180 and -180 <= east <= 180):
        raise ValueError("Invalid bounds")
    width = east - west if east >= west else east + 360 - west
    if width == 0:
        raise ValueError("Bounds must have a width")
    normalized_bounds = tuple(0.0 if value == 0 else value for value in (north, south, east, west))
    seed = json.dumps(normalized_bounds).encode("ascii")
    rng = random.Random(seed)
    bay_count = 10
    # Reserve a block per viewport within the signed 32-bit API ID range.
    viewport_hash = int.from_bytes(hashlib.sha256(seed).digest(), "big")
    first_id = 1 + bay_count * (viewport_hash % ((2**31 - 1) // bay_count))
    results = []
    for index in range(bay_count):
        longitude = west + rng.uniform(0.1, 0.9) * width
        if longitude > 180:
            longitude -= 360
        results.append({
            "id": first_id + index,
            "title": f"Mock bay {index + 1}",
            "type": (1, 2, 3, 4, 5, 9)[index % 6],
            "latitude": south + rng.uniform(0.1, 0.9) * (north - south),
            "longitude": longitude,
            "description": "Synthetic test bay — not a real parking location.",
        })
    return {"status": "OK", "count": len(results), "limit": bay_count, "bounds": bounds, "results": results}


class MockApiHandler(BaseHTTPRequestHandler):
    def respond(self, status, body):
        payload = json.dumps(body).encode("utf-8")
        self.send_response(status)
        self.send_header("Content-Type", "application/json; charset=utf-8")
        self.send_header("Content-Length", str(len(payload)))
        self.end_headers()
        self.wfile.write(payload)

    def do_GET(self):
        url = urlsplit(self.path)
        query = parse_qs(url.query)
        if url.path == "/api/bounds.php":
            try:
                response = parking_response(query)
            except (KeyError, ValueError, IndexError):
                self.respond(400, {"status": "ERROR", "message": "Provide valid north, south, east and west bounds."})
                return
            self.respond(200, response)
        elif url.path == "/api/places.php":
            # A predictable place to navigate to when testing the search UI.
            results = [{
                "id": "mock-london",
                "name": "Mock London",
                "subtitle": "Synthetic search result",
                "type": "city",
                "latitude": 51.5074,
                "longitude": -0.1278,
            }] if query.get("q", [""])[0].strip() else []
            self.respond(200, {"status": "OK" if results else "NO_RESULTS", "results": results})
        else:
            self.respond(404, {"status": "ERROR", "message": "Unknown mock route"})

    def do_POST(self):
        # No persistence and no forwarding: contributors can safely test submissions.
        if urlsplit(self.path).path in ("/api/report.php", "/api/feedback.php"):
            self.respond(200, {"status": "OK"})
        else:
            self.respond(404, {"status": "ERROR", "message": "Unknown mock route"})

    def log_message(self, format, *args):
        # Avoid logging submission contents, request headers or query strings.
        pass


if __name__ == "__main__":
    with HTTPServer(("127.0.0.1", 8080), MockApiHandler) as server:
        print("Mock API: http://127.0.0.1:8080/api/ — synthetic data only. Ctrl+C to stop.", flush=True)
        try:
            server.serve_forever()
        except KeyboardInterrupt:
            pass
