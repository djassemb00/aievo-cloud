import json
import os
import shutil
from pathlib import Path
from datetime import datetime

class SovereignKillSwitch:
    def __init__(self, base_path="C:/Users/hp/AndroidStudioProjects/AIEVO/aievo"):
        self.base_path = Path(base_path)
        self.status_file = self.base_path / "sovereign_status.json"
        self.kill_log_path = self.base_path / "kill_switch_log.json"

    def activate_kill_switch(self, reason="Manual Architect Override"):
        """Instantly shuts down all sovereign operations."""
        print(f"Kill Switch: CRITICAL SHUTDOWN INITIATED. Reason: {reason}")

        status_data = {
            "status": "OFFLINE",
            "last_action": "KILL_SWITCH_ACTIVATED",
            "timestamp": datetime.now().isoformat(),
            "reason": reason
        }

        with open(self.status_file, "w") as f:
            json.dump(status_data, f, indent=4)

        self._log_event("SHUTDOWN", reason)
        return True

    def initiate_rebirth(self, password):
        """Restores system from the immutable record (simulated)."""
        if password != "REBIRTH_INIT_00":
            print("Kill Switch: Rebirth password incorrect.")
            return False

        print("Kill Switch: Initiating Sovereign Rebirth sequence...")

        # In a real scenario, this would pull from the immutable anchor
        status_data = {
            "status": "CORE_ACTIVE",
            "last_action": "REBIRTH_COMPLETE",
            "timestamp": datetime.now().isoformat()
        }

        with open(self.status_file, "w") as f:
            json.dump(status_data, f, indent=4)

        self._log_event("REBIRTH", "Architect-led resurrection")
        print("Kill Switch: System successfully resurrected.")
        return True

    def _log_event(self, event_type, details):
        log_entry = {
            "timestamp": datetime.now().isoformat(),
            "event": event_type,
            "details": details
        }

        logs = []
        if self.kill_log_path.exists():
            with open(self.kill_log_path, "r") as f:
                logs = json.load(f)

        logs.append(log_entry)
        with open(self.kill_log_path, "w") as f:
            json.dump(logs, f, indent=4)

if __name__ == "__main__":
    ks = SovereignKillSwitch()
    ks.activate_kill_switch()
    # ks.initiate_rebirth("REBIRTH_INIT_00")
