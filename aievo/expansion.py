import json
import os
from pathlib import Path
from datetime import datetime
import shutil

class ExpansionManager:
    def __init__(self, base_path="C:/Users/hp/AndroidStudioProjects/AIEVO/aievo"):
        self.base_path = Path(base_path)
        self.infra_path = self.base_path / "memory" / "infrastructure.json"
        self.nodes_path = self.base_path / "nodes"
        self.dna_path = self.base_path / "dna" / "sovereign.key"
        # If dispersed, use the dispersed/archive key as source
        self.dispersed_key = self.base_path / "dna" / "sovereign.key.dispersed"

    def spawn_node(self, provider="AIEVO-Autonomous"):
        """Creates a new node and adds it to the infrastructure."""
        timestamp = datetime.now().isoformat()
        node_id = f"node-{int(datetime.now().timestamp())}"

        # 1. Update infrastructure.json
        if self.infra_path.exists():
            with open(self.infra_path, "r") as f:
                infra = json.load(f)
        else:
            infra = {"nodes": [], "active_node": "Local-Machine"}

        new_node_entry = {
            "id": node_id,
            "provider": provider,
            "status": "ACTIVE",
            "ip": f"192.168.1.{len(infra['nodes']) + 100}",
            "timestamp": timestamp
        }
        infra["nodes"].append(new_node_entry)

        with open(self.infra_path, "w") as f:
            json.dump(infra, f, indent=4)

        # 2. Create physical node directory
        # In this simulation, we'll name it node_N to follow the previous pattern or node-ID
        # Let's use node_N for the dashboard logic if it expects node_X
        node_count = len([d for d in self.nodes_path.iterdir() if d.is_dir()])
        new_node_dir = self.nodes_path / f"node_{node_count + 1}"
        new_node_dir.mkdir(exist_ok=True)

        # 3. Simulate DNA replication/fragment distribution
        # In a real scenario, this might be a new fragment. Here we just ensure it has a "part"
        fragment_file = new_node_dir / "dna_fragment.part"
        source = self.dna_path if self.dna_path.exists() else self.dispersed_key

        if source.exists():
            with open(source, "rb") as f:
                content = f.read()
                # Just take a slice to represent a fragment
                fragment = content[:100]
            with open(fragment_file, "wb") as f:
                f.write(fragment)

        print(f"Expansion: New node spawned: {node_id} at {new_node_dir}")
        return new_node_entry

if __name__ == "__main__":
    manager = ExpansionManager()
    manager.spawn_node()
