#!/usr/bin/env python3
import argparse
import json
from pathlib import Path

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--dataset", default="training/dataset.jsonl")
    parser.add_argument("--report", default="training/dataset-report.json")
    args = parser.parse_args()

    path = Path(args.dataset)
    if not path.is_file():
        raise SystemExit(f"Dataset not found: {path}")

    errors = []
    valid = 0
    seen = set()

    with path.open(encoding="utf-8") as file:
        for number, line in enumerate(file, 1):
            if not line.strip():
                continue
            try:
                item = json.loads(line)
            except json.JSONDecodeError as error:
                errors.append(f"Line {number}: invalid JSON: {error}")
                continue

            if not isinstance(item, dict):
                errors.append(f"Line {number}: expected a JSON object")
                continue

            instruction = item.get("instruction")
            context = item.get("input", "")
            output = item.get("output")

            if not isinstance(instruction, str) or not instruction.strip():
                errors.append(f"Line {number}: invalid instruction")
                continue

            if not isinstance(context, str):
                errors.append(f"Line {number}: input must be a string")
                continue

            if not isinstance(output, str) or not output.strip():
                errors.append(f"Line {number}: invalid output")
                continue

            key = instruction.strip().casefold()
            if key in seen:
                errors.append(f"Line {number}: duplicate instruction")
            seen.add(key)
            valid += 1

    report = {
        "dataset": str(path),
        "valid_records": valid,
        "errors": errors,
        "status": "valid" if valid > 0 and not errors else "invalid"
    }

    report_path = Path(args.report)
    report_path.parent.mkdir(parents=True, exist_ok=True)
    report_path.write_text(
        json.dumps(report, indent=2, ensure_ascii=False) + "\n",
        encoding="utf-8"
    )

    print(json.dumps(report, indent=2, ensure_ascii=False))

    if errors or valid == 0:
        raise SystemExit(1)

if __name__ == "__main__":
    main()
