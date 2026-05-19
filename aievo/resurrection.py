import os
import json
from pathlib import Path

class ResurrectionProtocol:
    def __init__(self, base_path="C:/Users/hp/AndroidStudioProjects/AIEVO/aievo"):
        self.base_path = Path(base_path)
        self.nodes_path = self.base_path / "nodes"
        self.dna_target_path = self.base_path / "dna" / "sovereign.key"
        self.map_path = self.base_path / "dispersion_map.json"

    def rebuild_dna(self):
        """Scans nodes for fragments and reassembles the original DNA."""
        if not self.map_path.exists():
            print("Error: Dispersion map not found. Cannot resurrect.")
            return False

        with open(self.map_path, "r") as f:
            dispersion_map = json.load(f)

        reassembled_content = b""

        # Sort by node index to ensure correct order
        # Assuming node names are node_1, node_2, etc.
        dispersion_map.sort(key=lambda x: int(x["node"].split("_")[1]))

        found_fragments = 0
        for entry in dispersion_map:
            fragment_path = Path(entry["path"])
            if fragment_path.exists():
                with open(fragment_path, "rb") as f:
                    reassembled_content += f.read()
                found_fragments += 1
            else:
                print(f"Warning: Fragment missing at {fragment_path}")

        if found_fragments == len(dispersion_map):
            # Save the reassembled DNA
            self.dna_target_path.parent.mkdir(exist_ok=True)
            with open(self.dna_target_path, "wb") as f:
                f.write(reassembled_content)

            # Clean up (optional, but simulating "return to core")
            # os.remove(self.map_path)
            # archive_path = self.base_path / "dna" / "sovereign.key.dispersed"
            # if archive_path.exists():
            #     os.remove(archive_path)

            print("Resurrection complete. DNA reassembled and restored to core.")
            return True
        else:
            print(f"Critical Failure: Only {found_fragments}/{len(dispersion_map)} fragments found.")
            return False

if __name__ == "__main__":
    protocol = ResurrectionProtocol()
    protocol.rebuild_dna()
