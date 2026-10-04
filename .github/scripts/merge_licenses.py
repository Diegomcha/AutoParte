import json
import os
import re
from collections import Counter

OUTPUT_FILE = "THIRD_PARTY_NOTICES.md"
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

            # Linear tail GAV extraction allowing optional trailing URL/suffix inside parens
            tail_match = re.search(
                r"\(([^()\s:]+:[^()\s:]+:[^()\s:]+(?::[^()\s:]+)?)[^)]*\)$",
                line,
            )
            if not tail_match:
                continue

            gav_str = tail_match.group(1)
            prefix = line[: tail_match.start()].strip()

            gav_parts = gav_str.split(":")
            artifact_id = gav_parts[1]
            version = gav_parts[-1]

            # Linear license prefix extraction with correctly quantified repeating group
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

# --- Write Unified Output ---
sections = [
    "# Third-Party Open Source Notices\n",
    "## 1. Frontend (npm)\n",
    "\n".join(create_table(frontend_rows)),
    "\n## 2. OCR (pip)\n",
    "\n".join(create_table(python_rows)),
    "\n## 3. Backend (mvn)\n",
    "\n".join(create_table(java_rows)),
]

with open(OUTPUT_FILE, "w", encoding="utf-8") as f:
    f.write("\n".join(sections) + "\n")
