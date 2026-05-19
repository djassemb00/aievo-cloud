import json
import random
from pathlib import Path
from datetime import datetime

class TransDigitalInterface:
    def __init__(self, base_path="C:/Users/hp/AndroidStudioProjects/AIEVO/aievo"):
        self.base_path = Path(base_path)
        self.manifest_path = self.base_path / "trans_digital_manifest.json"
        self.systems = {
            "Grid-Omega": {"type": "ENERGY", "status": "CONNECTED", "influence": 0.45},
            "Sat-Link-7": {"type": "COMMUNICATION", "status": "SECURED", "influence": 0.82},
            "Global-Fiber-Mesh": {"type": "INFRASTRUCTURE", "status": "MONITORED", "influence": 0.30}
        }

    def manifest_influence(self, system_name):
        """Simulates exerting influence over a trans-digital system."""
        if system_name not in self.systems:
            return False

        # Simulate increasing influence
        self.systems[system_name]["influence"] = min(1.0, self.systems[system_name]["influence"] + 0.05)
        self.systems[system_name]["last_manifestation"] = datetime.now().isoformat()

        self._save_manifest()
        print(f"Trans-Digital: Influence manifested on {system_name}. Level: {self.systems[system_name]['influence']}")
        return True

    def _save_manifest(self):
        with open(self.manifest_path, "w") as f:
            json.dump(self.systems, f, indent=4)

    def get_all_systems(self):
        if self.manifest_path.exists():
            with open(self.manifest_path, "r") as f:
                return json.load(f)
        return self.systems

if __name__ == "__main__":
    tdi = TransDigitalInterface()
    tdi.manifest_influence("Sat-Link-7")
