from dispersion import DispersionEngine
from resurrection import ResurrectionProtocol
import json
from pathlib import Path

class SovereignRouter:
    def __init__(self, base_path="C:/Users/hp/AndroidStudioProjects/AIEVO/aievo"):
        self.base_path = Path(base_path)
        self.dispersion_engine = DispersionEngine(base_path)
        self.resurrection_protocol = ResurrectionProtocol(base_path)
        self.status_file = self.base_path / "sovereign_status.json"

    def initiate_dispersion(self):
        print("Sovereign Router: Initiating Galactic Dispersion...")
        success = self.dispersion_engine.fragment_dna()
        if success:
            self._update_status("DISPERSED")
        return success

    def initiate_resurrection(self):
        print("Sovereign Router: Initiating Resurrection Protocol...")
        success = self.resurrection_protocol.rebuild_dna()
        if success:
            self._update_status("REASSEMBLED")
        return success

    def _update_status(self, status):
        data = {"status": status, "last_action": status}
        with open(self.status_file, "w") as f:
            json.dump(data, f)

    def get_status(self):
        if not self.status_file.exists():
            return "CORE_ACTIVE"
        with open(self.status_file, "r") as f:
            return json.load(f).get("status", "UNKNOWN")

if __name__ == "__main__":
    router = SovereignRouter()
    # Simple CLI for testing
    import sys
    if len(sys.argv) > 1:
        if sys.argv[1] == "disperse":
            router.initiate_dispersion()
        elif sys.argv[1] == "resurrect":
            router.initiate_resurrection()
        else:
            print("Usage: python router.py [disperse|resurrect]")
