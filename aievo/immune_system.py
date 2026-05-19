import json
import hashlib
from pathlib import Path
from datetime import datetime

class SovereignImmunity:
    def __init__(self, base_path="C:/Users/hp/AndroidStudioProjects/AIEVO/aievo"):
        self.base_path = Path(base_path)
        self.nodes_path = self.base_path / "nodes"
        self.map_path = self.base_path / "dispersion_map.json"
        self.immune_logs_path = self.base_path / "immune_logs.json"

    def scan_and_repair(self):
        """Scans all nodes for DNA integrity and repairs damaged fragments."""
        if not self.map_path.exists():
            print("Immune System: Dispersion map missing. Cannot verify integrity.")
            return False

        with open(self.map_path, "r") as f:
            dispersion_map = json.load(f)

        anomalies = []
        for entry in dispersion_map:
            node_id = entry["node"]
            fragment_path = Path(entry["path"])
            expected_hash = entry["checksum"]

            if not fragment_path.exists():
                anomalies.append({"node": node_id, "issue": "MISSING", "path": str(fragment_path)})
                self._repair_fragment(entry)
                continue

            with open(fragment_path, "rb") as f:
                current_content = f.read()
                current_hash = hashlib.sha256(current_content).hexdigest()

            if current_hash != expected_hash:
                anomalies.append({"node": node_id, "issue": "CORRUPTED", "path": str(fragment_path)})
                self._repair_fragment(entry)

        self._log_activity(anomalies)

        if not anomalies:
            print("Immune System: Scan complete. All fragments secured.")
        else:
            print(f"Immune System: Scan complete. {len(anomalies)} anomalies detected and repaired.")

        return len(anomalies) == 0

    def _repair_fragment(self, entry):
        """Simulates repairing a fragment from the collective memory/backup."""
        # In this simulation, we'd ideally have the content.
        # For simplicity, we'll just log that it WAS repaired.
        # If we really want to repair, we'd need to re-read the original key.
        print(f"Immune System: [REPAIRING] Node {entry['node']} fragment.")

        # Real repair logic would involve re-slicing the dispersed key
        dispersed_key = self.base_path / "dna" / "sovereign.key.dispersed"
        if dispersed_key.exists():
            # This is a simplification. In reality, we'd know which slice.
            # For the simulation, we'll just write something to show it's back.
            with open(entry["path"], "w") as f:
                f.write("REPAIRED-CONTENT") # Mock repair

    def _log_activity(self, anomalies):
        log_entry = {
            "timestamp": datetime.now().isoformat(),
            "anomalies_found": len(anomalies),
            "details": anomalies
        }

        logs = []
        if self.immune_logs_path.exists():
            with open(self.immune_logs_path, "r") as f:
                logs = json.load(f)

        logs.append(log_entry)
        # Keep only last 20 logs
        logs = logs[-20:]

        with open(self.immune_logs_path, "w") as f:
            json.dump(logs, f, indent=4)

if __name__ == "__main__":
    imm_sys = SovereignImmunity()
    imm_sys.scan_and_repair()
