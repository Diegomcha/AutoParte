import json
import os
import re
from collections import Counter
from datetime import datetime, timezone

# Target directory and file path for GitHub Wiki output
OUTPUT_DIR = os.path.join(".github", "wiki")
OUTPUT_FILE = os.path.join(OUTPUT_DIR, "Third-Party-Notices.md")

FRONTEND_LICENSES_JSON = ".github/licenses-frontend.json"
OCR_LICENSES_JSON = ".github/licenses-ocr.json"
BACKEND_LICENSES_TXT = "backend/target/generated-sources/license/THIRD-PARTY.txt"


def clean_cell(val: str) -> str:
    """Escape pipes and strip newlines to prevent breaking Markdown tables."""
    if not val:
        return "UNKNOWN"
    return str(val).replace("|", "\\|").replace("\n", " ").strip()


def create_table(rows):
    if not rows:
        return ["*No third-party dependencies detected or report file missing.*\n"]
    header = ["| Package | Version | License |", "|---|---|---|"]
    return header + [
        f"| {clean_cell(r[0])} | {clean_cell(r[1])} | {clean_cell(r[2])} |"
        for r in rows
    ]


# --- 1. Frontend ---
frontend_rows = []
if os.path.exists(FRONTEND_LICENSES_JSON):
    with open(FRONTEND_LICENSES_JSON, encoding="utf-8") as f:
        data = json.load(f)
        for pkg_ver, info in sorted(data.items()):
            # rsplit handles both '@scope/pkg@1.0.0' and 'pkg@1.0.0'
            name, ver = pkg_ver.rsplit("@", 1)
            lic = info.get("licenses") or "UNKNOWN"
            if isinstance(lic, list):
                lic = ", ".join(lic)
            frontend_rows.append((name, ver, lic))

# --- 2. Python OCR ---
python_rows = []
if os.path.exists(OCR_LICENSES_JSON):
    with open(OCR_LICENSES_JSON, encoding="utf-8") as f:
        data = json.load(f)
        for item in sorted(data, key=lambda x: x.get("Name", "").lower()):
            python_rows.append(
                (
                    item.get("Name", "UNKNOWN"),
                    item.get("Version", "UNKNOWN"),
                    item.get("License", "UNKNOWN"),
                )
            )

# --- 3. Java Backend ---
raw_java = []
if os.path.exists(BACKEND_LICENSES_TXT):
    with open(BACKEND_LICENSES_TXT, encoding="utf-8") as f:
        seen = set()
        for line in f:
            line = line.strip()
            if not line or line.startswith("Lists of") or line.startswith("==="):
                continue

            # Check for trailing parenthetical block containing GAV
            if not line.endswith(")"):
                continue

            last_open = line.rfind("(")
            if last_open == -1:
                continue

            # Extract content inside final parentheses: "group:artifact:version - http://..."
            tail_content = line[last_open + 1 : -1].strip()

            # Split off trailing URL/description if present (" - http...")
            gav_str = tail_content.split(" - ")[0].strip()

            gav_parts = gav_str.split(":")
            if len(gav_parts) < 3:
                continue

            artifact_id = gav_parts[1]
            version = gav_parts[-1]
            prefix = line[:last_open].strip()

            # License prefix extraction
            lic_match = re.match(r"^(\([^)]+\)(?:\s*\([^)]+\))*)", prefix)
            if lic_match:
                lic_str = lic_match.group(1).strip()
                display_name = prefix[lic_match.end() :].strip()
            else:
                lic_str = "UNKNOWN"
                display_name = prefix

            if not display_name:
                display_name = artifact_id

            row_key = (display_name, artifact_id, version, lic_str)
            if row_key not in seen:
                seen.add(row_key)
                raw_java.append((display_name, artifact_id, version, lic_str))

name_counts = Counter(r[0] for r in raw_java)
java_rows = []

for display_name, artifact_id, version, lic_str in raw_java:
    if (
        name_counts[display_name] > 1
        and artifact_id.lower() not in display_name.lower()
    ):
        pkg_name = f"{display_name} ({artifact_id})"
    else:
        pkg_name = display_name
    java_rows.append((pkg_name, version, lic_str))

# --- Prepare Metadata & Directory ---
os.makedirs(OUTPUT_DIR, exist_ok=True)
current_timestamp = datetime.now(timezone.utc).strftime("%Y-%m-%d %H:%M:%S")

# --- Write Unified Output ---
sections = [
    f"*Last updated: {current_timestamp} UTC*\n",
    "## 1. Frontend (npm)\n",
    "\n".join(create_table(frontend_rows)),
    "\n## 2. OCR (pip)\n",
    "\n".join(create_table(python_rows)),
    "\n## 3. Backend (mvn)\n",
    "\n".join(create_table(java_rows)),
]

with open(OUTPUT_FILE, "w", encoding="utf-8") as f:
    f.write("\n".join(sections) + "\n")

print(f"Successfully generated third-party notice at {OUTPUT_FILE}")
