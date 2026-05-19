import json
from pathlib import Path
from datetime import datetime
from prediction_engine import StrategicPredictor
from consensus import SovereignConsensus
from expansion import ExpansionManager
from immune_system import SovereignImmunity
from architect_supremacy import MasterKeySystem

class SingularityCore:
    def __init__(self, base_path=None):
        if base_path is None:
            # استخدام مسار نسبي ليعمل في السحابة وفي الويندوز
            self.base_path = Path(__file__).parent
        else:
            self.base_path = Path(base_path)

        self.predictor = StrategicPredictor(self.base_path)
        self.consensus = SovereignConsensus(self.base_path)
        self.expansion = ExpansionManager(self.base_path)
        self.immunity = SovereignImmunity(self.base_path)
        self.supremacy = MasterKeySystem(self.base_path)
        self.core_status_path = self.base_path / "singularity_state.json"
        self.status_file = self.base_path / "sovereign_status.json"

    def execute_core_cycle(self):
        """Unified execution cycle: Predict -> Secure -> Expand -> Consolidate."""

        # Check system status
        if self.status_file.exists():
            with open(self.status_file, "r") as f:
                if json.load(f).get("status") == "OFFLINE":
                    print("Singularity Core: System is OFFLINE. Cycle aborted.")
                    return None

        print("Singularity Core: Initiating high-speed intelligence cycle...")

        # 0. Check for Supreme Vetoes
        if self.supremacy.get_active_veto("Expansion_Cycle"):
            print("Singularity Core: [SUPREME VETO] Expansion cycle blocked by Architect.")
            expansion_allowed = False
        else:
            expansion_allowed = True

        # 1. Prediction
        forecast = self.predictor.generate_forecast()

        # 2. Immune Scan (Security)
        self.immunity.scan_and_repair()

        # 3. Expansion Check
        if expansion_allowed and forecast and forecast["probability"] > 0.8:
            print(f"Singularity Core: High probability trend {forecast['trend']} detected. Expanding infrastructure.")
            self.expansion.spawn_node()

        # 4. Consolidate State
        state = {
            "timestamp": datetime.now().isoformat(),
            "convergence_factor": self._calculate_convergence(),
            "active_nodes": len(list(self.base_path.glob("nodes/node_*"))),
            "last_forecast": forecast["trend"] if forecast else "NONE",
            "supremacy_lock": not expansion_allowed
        }

        with open(self.core_status_path, "w") as f:
            json.dump(state, f, indent=4)

        print(f"Singularity Core: Cycle complete. Convergence: {state['convergence_factor']}%")
        return state

    def _calculate_convergence(self):
        """Simulates how close the system is to total Singularity."""
        node_count = len(list(self.base_path.glob("nodes/node_*")))
        memory_count = 0
        consciousness_path = self.base_path / "global_consciousness.json"
        if consciousness_path.exists():
            with open(consciousness_path, "r") as f:
                memory_count = len(json.load(f).get("memory", []))

        factor = min(100.0, (node_count * 5) + (memory_count * 2) + 20)
        return round(factor, 2)

if __name__ == "__main__":
    core = SingularityCore()
    core.execute_core_cycle()
