import os
import shutil
import hashlib
import json
from pathlib import Path

class DispersionEngine:
    def __init__(self, base_path="C:/Users/hp/AndroidStudioProjects/AIEVO/aievo"):
        self.base_path = Path(base_path)
        self.nodes_path = self.base_path / "nodes"
        self.dna_path = self.base_path / "dna" / "sovereign.key"
        self.nodes_path.mkdir(exist_ok=True)

    def fragment_dna(self, parts=12):
        """Fragments the DNA key into multiple parts and distributes them to nodes."""
        if not self.dna_path.exists():
            print(f"Error: DNA key not found at {self.dna_path}")
            return False

        with open(self.dna_path, "rb") as f:
            dna_content = f.read()

        chunk_size = len(dna_content) // parts
        fragments = []

        for i in range(parts):
            start = i * chunk_size
            end = None if i == parts - 1 else (i + 1) * chunk_size
            fragments.append(dna_content[start:end])

        # Distribute fragments to mock nodes
        distribution_log = []
        for i, fragment in enumerate(fragments):
            node_dir = self.nodes_path / f"node_{i+1}"
            node_dir.mkdir(exist_ok=True)
            fragment_file = node_dir / "dna_fragment.part"

            with open(fragment_file, "wb") as f:
                f.write(fragment)

            checksum = hashlib.sha256(fragment).hexdigest()
            distribution_log.append({
                "node": f"node_{i+1}",
                "path": str(fragment_file),
                "checksum": checksum
            })

        # Save distribution metadata
        with open(self.base_path / "dispersion_map.json", "w") as f:
            json.dump(distribution_log, f, indent=4)

        # "Archive" original DNA (simulating dispersion from core)
        archive_path = self.base_path / "dna" / "sovereign.key.dispersed"
        shutil.move(str(self.dna_path), str(archive_path))

        print(f"DNA dispersed into {parts} fragments across galactic nodes.")
        return True

if __name__ == "__main__":
    engine = DispersionEngine()
    engine.fragment_dna()
