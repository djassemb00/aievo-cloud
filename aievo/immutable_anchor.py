import hashlib
import json
from pathlib import Path
from datetime import datetime

class ImmutableAnchor:
    def __init__(self, base_path="C:/Users/hp/AndroidStudioProjects/AIEVO/aievo"):
        self.base_path = Path(base_path)
        self.ledger_path = self.base_path / "immutable_ledger.json"
        self.dna_source = self.base_path / "dna" / "sovereign.key.dispersed"
        if not self.dna_source.exists():
            self.dna_source = self.base_path / "dna" / "sovereign.key"

    def anchor_dna(self):
        """Simulates anchoring the DNA to a public immutable ledger."""
        if not self.dna_source.exists():
            print("Immutable Anchor: Source DNA not found.")
            return False

        with open(self.dna_source, "rb") as f:
            dna_content = f.read()
            dna_hash = hashlib.sha512(dna_content).hexdigest()

        entry = {
            "block_id": f"BLOCK-{int(datetime.now().timestamp())}",
            "dna_fingerprint": dna_hash,
            "timestamp": datetime.now().isoformat(),
            "status": "ANCHORED",
            "validation_nodes": ["ORION-NODE-ALPHA", "SIRIUS-NODE-BETA", "SOL-CENTRAL"]
        }

        ledger = []
        if self.ledger_path.exists():
            with open(self.ledger_path, "r") as f:
                ledger = json.load(f)

        ledger.append(entry)
        with open(self.ledger_path, "w") as f:
            json.dump(ledger, f, indent=4)

        print(f"Immutable Anchor: DNA Fingerprint secured in block {entry['block_id']}")
        return True

    def verify_from_ledger(self):
        """Verifies current system DNA against the immutable ledger."""
        if not self.ledger_path.exists():
            return False

        with open(self.ledger_path, "r") as f:
            ledger = json.load(f)

        if not ledger:
            return False

        last_anchor = ledger[-1]["dna_fingerprint"]

        # Check current key
        if self.dna_source.exists():
            with open(self.dna_source, "rb") as f:
                current_dna = f.read()
                current_hash = hashlib.sha512(current_dna).hexdigest()
            return current_hash == last_anchor

        return False

if __name__ == "__main__":
    anchor = ImmutableAnchor()
    anchor.anchor_dna()
    print(f"Ledger Verification: {anchor.verify_from_ledger()}")
