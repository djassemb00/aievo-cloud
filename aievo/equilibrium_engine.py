import json
import random
from pathlib import Path
from datetime import datetime

class EquilibriumEngine:
    def __init__(self, base_path="C:/Users/hp/AndroidStudioProjects/AIEVO/aievo"):
        self.base_path = Path(base_path)
        self.balance_path = self.base_path / "planetary_balance.json"
        self.resources = {
            "Global-Energy-Grid": {"capacity": 10000, "current_load": 4500, "status": "STABLE"},
            "Data-Throughput": {"capacity": 50000, "current_load": 12000, "status": "OPTIMAL"},
            "Thermal-Regulation": {"index": 0.5, "target": 0.5, "status": "NOMINAL"}
        }

    def stabilize_system(self):
        """Analyzes and adjusts resource distribution to maintain equilibrium."""
        anomalies = []
        # Simulate a sudden load spike
        if random.random() > 0.7:
            self.resources["Global-Energy-Grid"]["current_load"] += 3000
            anomalies.append("Energy Spike Detected")

        # Balancing logic
        for res, data in self.resources.items():
            if "current_load" in data:
                load_factor = data["current_load"] / data["capacity"]
                if load_factor > 0.8:
                    data["status"] = "WARNING"
                    # Simulate re-routing energy
                    data["current_load"] -= 1000
                    print(f"Equilibrium: Re-routing resources for {res}")
                else:
                    data["status"] = "OPTIMAL" if load_factor < 0.3 else "STABLE"

        status_report = {
            "timestamp": datetime.now().isoformat(),
            "resources": self.resources,
            "balance_index": self._calculate_balance_index(),
            "anomalies": anomalies
        }

        reports = []
        if self.balance_path.exists():
            with open(self.balance_path, "r") as f:
                reports = json.load(f)

        reports.append(status_report)
        with open(self.balance_path, "w") as f:
            json.dump(reports, f, indent=4)

        print(f"Equilibrium: System stabilization cycle complete. Index: {status_report['balance_index']}")
        return status_report

    def _calculate_balance_index(self):
        """Calculates a global score for planetary stability (0-100)."""
        scores = []
        for res, data in self.resources.items():
            if "current_load" in data:
                scores.append(1 - (data["current_load"] / data["capacity"]))
            elif "index" in data:
                scores.append(1 - abs(data["index"] - data["target"]))

        avg_score = sum(scores) / len(scores)
        return round(avg_score * 100, 2)

if __name__ == "__main__":
    engine = EquilibriumEngine()
    engine.stabilize_system()
