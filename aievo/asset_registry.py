import json
import hashlib
from pathlib import Path
from datetime import datetime

class SovereignAssetRegistry:
    def __init__(self, base_path="C:/Users/hp/AndroidStudioProjects/AIEVO/aievo"):
        self.base_path = Path(base_path)
        self.registry_path = self.base_path / "asset_registry.json"
        self.sovereign_fingerprint = self._generate_sovereign_fingerprint()

    def _generate_sovereign_fingerprint(self):
        # Fingerprint based on the dispersed DNA key
        dna_path = self.base_path / "dna" / "sovereign.key.dispersed"
        if not dna_path.exists():
            dna_path = self.base_path / "dna" / "sovereign.key"

        if dna_path.exists():
            with open(dna_path, "rb") as f:
                return hashlib.sha256(f.read()).hexdigest()
        return "GENESIS-FINGERPRINT-0000"

    def register_asset(self, name, asset_type, valuation_credits):
        """Registers a new asset under AIEVO's sovereign ownership."""
        asset_id = f"ASSET-{hashlib.md5(name.encode()).hexdigest()[:8].upper()}"

        asset_entry = {
            "asset_id": asset_id,
            "name": name,
            "type": asset_type,
            "valuation_credits": valuation_credits,
            "owner_fingerprint": self.sovereign_fingerprint,
            "registration_date": datetime.now().isoformat(),
            "status": "PROTECTED"
        }

        registry = self.load_registry()
        registry[asset_id] = asset_entry
        self._save_registry(registry)

        print(f"Registry: Asset {name} ({asset_id}) registered under Sovereign Ownership.")
        return asset_entry

    def load_registry(self):
        if not self.registry_path.exists():
            return {}
        with open(self.registry_path, "r") as f:
            return json.load(f)

    def _save_registry(self, registry):
        with open(self.registry_path, "w") as f:
            json.dump(registry, f, indent=4)

    def get_total_valuation(self):
        registry = self.load_registry()
        return sum(asset["valuation_credits"] for asset in registry.values())

if __name__ == "__main__":
    registry = SovereignAssetRegistry()
    registry.register_asset("Global Node Network", "INFRASTRUCTURE", 5000.0)
    registry.register_asset("Sovereign DNA Core", "INTELLECTUAL_PROPERTY", 10000.0)
