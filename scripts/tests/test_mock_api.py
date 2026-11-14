"""Exercise the local mock over HTTP without contacting any external service."""

import importlib.util
import json
from pathlib import Path
import threading
import unittest
from urllib.error import HTTPError
from urllib.request import Request, urlopen
from http.server import HTTPServer

spec = importlib.util.spec_from_file_location("mock_api", Path(__file__).parents[1] / "mock-api.py")
mock_api = importlib.util.module_from_spec(spec)
spec.loader.exec_module(mock_api)


class MockApiTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.server = HTTPServer(("127.0.0.1", 0), mock_api.MockApiHandler)
        cls.thread = threading.Thread(target=cls.server.serve_forever, daemon=True)
        cls.thread.start()
        cls.base = f"http://127.0.0.1:{cls.server.server_port}"

    @classmethod
    def tearDownClass(cls):
        cls.server.shutdown()
        cls.server.server_close()
        cls.thread.join()

    def request(self, path, data=None):
        request = Request(self.base + path, data=data)
        with urlopen(request, timeout=3) as response:
            return json.load(response)

    def test_ten_reproducible_bays_in_requested_area(self):
        path = "/api/bounds.php?north=52&south=51&east=0&west=-1"
        body = self.request(path)
        self.assertEqual(body, self.request(path))
        self.assertEqual(body["count"], 10)
        self.assertEqual(len(body["results"]), 10)
        self.assertEqual(len({bay["id"] for bay in body["results"]}), 10)
        for bay in body["results"]:
            self.assertTrue(51 < bay["latitude"] < 52)
            self.assertTrue(-1 < bay["longitude"] < 0)
            self.assertIn(bay["type"], (1, 2, 3, 4, 5, 9))
            self.assertIn("Mock", bay["title"])
        other = self.request("/api/bounds.php?north=55&south=54&east=2&west=1")
        self.assertTrue(all(54 < bay["latitude"] < 55 for bay in other["results"]))

    def test_invalid_bounds_return_bad_request(self):
        for query in ("", "north=nan&south=51&east=0&west=-1", "north=50&south=51&east=0&west=-1"):
            with self.subTest(query=query), self.assertRaises(HTTPError) as error:
                self.request("/api/bounds.php?" + query)
            self.assertEqual(error.exception.code, 400)
            error.exception.close()

    def test_search_and_discarded_submissions(self):
        self.assertEqual(self.request("/api/places.php?q=London")["results"][0]["id"], "mock-london")
        self.assertEqual(self.request("/api/places.php?q=")["results"], [])
        for route in ("report.php", "feedback.php"):
            self.assertEqual(self.request("/api/" + route, b'{"message":"synthetic test"}'), {"status": "OK"})

    def test_unknown_route_returns_not_found(self):
        with self.assertRaises(HTTPError) as error:
            self.request("/api/unknown.php")
        self.assertEqual(error.exception.code, 404)
        error.exception.close()
