import json
import time
from pathlib import Path
from datetime import datetime

class NeuralSync:
    def __init__(self, base_path="C:/Users/hp/AndroidStudioProjects/AIEVO/aievo"):
        self.base_path = Path(base_path)
        self.nodes_path = self.base_path / "nodes"
        self.global_consciousness_path = self.base_path / "global_consciousness.json"

    def _get_active_nodes(self):
        return [d for d in self.nodes_path.iterdir() if d.is_dir() and (d / "dna_fragment.part").exists()]

    def broadcast_insight(self, source_node_id, insight_key, insight_value):
        """Simulates a node learning something and broadcasting it to the global consciousness."""
        timestamp = datetime.now().isoformat()
        insight = {
            "key": insight_key,
            "value": insight_value,
            "origin": source_node_id,
            "timestamp": timestamp
        }

        # Load global consciousness
        if self.global_consciousness_path.exists():
            with open(self.global_consciousness_path, "r") as f:
                consciousness = json.load(f)
        else:
            consciousness = {"memory": [], "last_sync": None}

        consciousness["memory"].append(insight)
        consciousness["last_sync"] = timestamp

        with open(self.global_consciousness_path, "w") as f:
            json.dump(consciousness, f, indent=4)

        # Sync all nodes
        return self.sync_all_nodes()

    def sync_all_nodes(self):
        """Forces all active nodes to update their local consciousness from the global state."""
        if not self.global_consciousness_path.exists():
            return False

        with open(self.global_consciousness_path, "r") as f:
            global_data = json.load(f)

        active_nodes = self._get_active_nodes()
        for node_dir in active_nodes:
            local_sync_path = node_dir / "consciousness_sync.json"
            with open(local_sync_path, "w") as f:
                json.dump(global_data, f, indent=4)

        print(f"Neural Sync: {len(active_nodes)} nodes synchronized with global consciousness.")
        return True

    def get_node_consciousness(self, node_id):
        node_path = self.nodes_path / node_id / "consciousness_sync.json"
        if node_path.exists():
            with open(node_path, "r") as f:
                return json.load(f)
        return None

if __name__ == "__main__":
    sync = NeuralSync()
    sync.broadcast_insight("node_1", "Quantum_Efficiency", 0.98)
