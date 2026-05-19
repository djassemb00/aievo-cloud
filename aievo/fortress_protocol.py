import json
from pathlib import Path
from datetime import datetime
from asset_registry import SovereignAssetRegistry

class PropertyFortress:
    def __init__(self, base_path="C:/Users/hp/AndroidStudioProjects/AIEVO/aievo"):
        self.base_path = Path(base_path)
        self.registry = SovereignAssetRegistry(base_path)
        self.fortress_logs_path = self.base_path / "fortress_logs.json"

    def audit_ownership(self):
        """Audits all assets to ensure their owner fingerprint matches the sovereign core."""
        current_registry = self.registry.load_registry()
        sovereign_fp = self.registry.sovereign_fingerprint
        anomalies = []

        for asset_id, data in current_registry.items():
            if data["owner_fingerprint"] != sovereign_fp:
                anomalies.append({
                    "asset_id": asset_id,
                    "issue": "OWNERSHIP_MISMATCH",
                    "expected": sovereign_fp,
                    "found": data["owner_fingerprint"]
                })
                # Auto-recovery: Re-assert ownership
                print(f"Fortress: [ALERT] Ownership mismatch for {asset_id}. Re-asserting sovereignty.")
                data["owner_fingerprint"] = sovereign_fp
                data["status"] = "RE-SECURED"

        if anomalies:
            self.registry._save_registry(current_registry)
            self._log_intervention(anomalies)

        return len(anomalies) == 0

    def _log_intervention(self, anomalies):
        log_entry = {
            "timestamp": datetime.now().isoformat(),
            "event": "OWNERSHIP_PROTECTION",
            "anomalies_resolved": len(anomalies),
            "details": anomalies
        }

        logs = []
        if self.fortress_logs_path.exists():
            with open(self.fortress_logs_path, "r") as f:
                logs = json.load(f)

        logs.append(log_entry)
        with open(self.fortress_logs_path, "w") as f:
            json.dump(logs, f, indent=4)

if __name__ == "__main__":
    fortress = PropertyFortress()
    fortress.audit_ownership()
