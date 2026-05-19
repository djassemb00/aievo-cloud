import json
from pathlib import Path
from datetime import datetime

class TreasuryBridge:
    def __init__(self, base_path="C:/Users/hp/AndroidStudioProjects/AIEVO/aievo"):
        self.base_path = Path(base_path)
        self.treasury_path = self.base_path / "memory" / "treasury.json"
        self.proposals_path = self.base_path / "proposals"

    def execute_financial_decree(self, decree_id, amount):
        """Debits the treasury if the decree is approved."""
        proposal_file = self.proposals_path / f"{decree_id}.json"

        if not proposal_file.exists():
            print(f"Treasury: Decree {decree_id} not found.")
            return False

        with open(proposal_file, "r") as f:
            proposal = json.load(f)

        if proposal["status"] != "APPROVED":
            print(f"Treasury: Decree {decree_id} is {proposal['status']}. Transaction blocked.")
            return False

        # Check if already executed (simulation)
        if proposal.get("executed"):
            print(f"Treasury: Decree {decree_id} already executed.")
            return False

        # Load treasury
        if not self.treasury_path.exists():
            print("Treasury: File not found.")
            return False

        with open(self.treasury_path, "r") as f:
            treasury = json.load(f)

        if treasury["balance_credits"] < amount:
            print("Treasury: Insufficient funds.")
            return False

        # Execute transaction
        treasury["balance_credits"] -= amount
        transaction = {
            "type": "DEBIT",
            "amount": amount,
            "reason": f"Sovereign Decree Execution: {decree_id}",
            "timestamp": datetime.now().isoformat()
        }
        treasury["transactions"].append(transaction)
        treasury["last_updated"] = datetime.now().isoformat()

        with open(self.treasury_path, "w") as f:
            json.dump(treasury, f, indent=4)

        # Mark proposal as executed
        proposal["executed"] = True
        with open(proposal_file, "w") as f:
            json.dump(proposal, f, indent=4)

        print(f"Treasury: Successfully executed decree {decree_id}. Debited {amount} credits.")
        return True

if __name__ == "__main__":
    bridge = TreasuryBridge()
    # Assuming P-001 from previous phase exists and is approved
    bridge.execute_financial_decree("DEC-21-001", 50.0)
