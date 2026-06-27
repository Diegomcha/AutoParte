import json

from pathlib import Path
from fastapi.testclient import TestClient

from api import app

# CONSTANTS

TEST_DATA_DIR = Path(__file__).parent / "data"
EXPECTEDS_FILE = TEST_DATA_DIR / "expecteds.json"

# HELPER FUNCTIONS


def load_expecteds() -> dict[str, dict]:
    with open(EXPECTEDS_FILE, "r", encoding="utf-8") as f:
        return json.load(f)


# SETUP

client = TestClient(app)
expecteds = load_expecteds()

# TESTS


def test_mrz():
    for image, expected_response in expecteds.items():
        with open(TEST_DATA_DIR / image, "rb") as f:
            image_bytes = f.read()

        response = client.post("/mrz", files={"image": image_bytes})

        assert response.status_code == 200 if expected_response["valid"] else 400
        assert response.json() == expected_response
