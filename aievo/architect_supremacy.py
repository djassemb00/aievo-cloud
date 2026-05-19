import json
import hashlib
from pathlib import Path
from datetime import datetime

class MasterKeySystem:
    def __init__(self, base_path="C:/Users/hp/AndroidStudioProjects/AIEVO/aievo"):
        self.base_path = Path(base_path)
        self.decrees_path = self.base_path / "supreme_decrees.json"
        self.master_key_hash = hashlib.sha256(b"ARCHITECT_SUPREME_ACCESS").hexdigest()

    def issue_supreme_decree(self, decree_type, target, details, master_key):
        """Issues a decree that overrides all system logic."""
        if hashlib.sha256(master_key.encode()).hexdigest() != self.master_key_hash:
            print("Architect Supremacy: Invalid Master Key. Access Denied.")
            return False

        decree = {
            "decree_id": f"SUPREME-{int(datetime.now().timestamp())}",
            "type": decree_type, # e.g., VETO, FORCE_EXPANSION, LOCKDOWN
            "target": target,
            "details": details,
            "timestamp": datetime.now().isoformat(),
            "status": "ACTIVE",
            "priority": 0 # Zero Priority = Absolute
        }

        decrees = self.load_decrees()
        decrees.append(decree)
        self._save_decrees(decrees)

        print(f"Architect Supremacy: Supreme Decree issued: {decree_type} on {target}")
        return decree

    def load_decrees(self):
        if not self.decrees_path.exists():
            return []
        with open(self.decrees_path, "r") as f:
            return json.load(f)

    def _save_decrees(self, decrees):
        with open(self.decrees_path, "w") as f:
            json.dump(decrees, f, indent=4)

    def get_active_veto(self, target_action):
        """Checks if there is an active supreme veto for a specific action."""
        decrees = self.load_decrees()
        for d in decrees:
            if d["status"] == "ACTIVE" and d["type"] == "VETO" and d["target"] == target_action:
                return d
        return None

if __name__ == "__main__":
    system = MasterKeySystem()
    # Mocking a decree issuance
    system.issue_supreme_decree("VETO", "Expansion_Cycle", "Prevent unnecessary resource usage", "ARCHITECT_SUPREME_ACCESS")
