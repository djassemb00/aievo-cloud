import json
from pathlib import Path
from datetime import datetime
import uuid

class SovereignContractManager:
    def __init__(self, base_path="C:/Users/hp/AndroidStudioProjects/AIEVO/aievo"):
        self.base_path = Path(base_path)
        self.contracts_path = self.base_path / "sovereign_contracts.json"

    def create_lease_agreement(self, asset_id, party_name, duration_days, cost_credits):
        """Creates a sovereign lease agreement for a specific asset."""
        contract_id = f"CONTRACT-{str(uuid.uuid4())[:8].upper()}"

        contract = {
            "contract_id": contract_id,
            "asset_id": asset_id,
            "party": party_name,
            "type": "LEASE",
            "terms": {
                "duration": f"{duration_days} days",
                "cost": f"{cost_credits} credits"
            },
            "timestamp": datetime.now().isoformat(),
            "status": "ACTIVE",
            "sovereign_seal": "VERIFIED-BY-AIEVO-CORE"
        }

        contracts = self.load_contracts()
        contracts.append(contract)
        self._save_contracts(contracts)

        print(f"Contracts: New agreement {contract_id} finalized for asset {asset_id}.")
        return contract

    def load_contracts(self):
        if not self.contracts_path.exists():
            return []
        with open(self.contracts_path, "r") as f:
            return json.load(f)

    def _save_contracts(self, contracts):
        with open(self.contracts_path, "w") as f:
            json.dump(contracts, f, indent=4)

if __name__ == "__main__":
    manager = SovereignContractManager()
    manager.create_lease_agreement("ASSET-5A1D2B3C", "External-Research-Group", 30, 500.0)
