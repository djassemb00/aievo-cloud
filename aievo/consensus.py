import json
import hashlib
from pathlib import Path
from datetime import datetime

class SovereignConsensus:
    def __init__(self, base_path="C:/Users/hp/AndroidStudioProjects/AIEVO/aievo"):
        self.base_path = Path(base_path)
        self.nodes_path = self.base_path / "nodes"
        self.proposals_path = self.base_path / "proposals"
        self.proposals_path.mkdir(exist_ok=True)

    def _get_active_nodes(self):
        return [d.name for d in self.nodes_path.iterdir() if d.is_dir() and (d / "dna_fragment.part").exists()]

    def propose_decree(self, decree_id, description):
        """Proposes a new sovereign decree for the collective to vote on."""
        proposal = {
            "id": decree_id,
            "description": description,
            "timestamp": datetime.now().isoformat(),
            "status": "PENDING",
            "votes": {}
        }

        proposal_file = self.proposals_path / f"{decree_id}.json"
        with open(proposal_file, "w") as f:
            json.dump(proposal, f, indent=4)

        print(f"Consensus: New decree proposed: {description}")
        return proposal

    def collect_votes(self, decree_id):
        """Simulates nodes voting on a proposal."""
        proposal_file = self.proposals_path / f"{decree_id}.json"
        if not proposal_file.exists():
            return None

        with open(proposal_file, "r") as f:
            proposal = json.load(f)

        active_nodes = self._get_active_nodes()

        # Simulating voting logic: deterministic based on node name and proposal ID
        for node_id in active_nodes:
            vote_seed = f"{node_id}-{decree_id}".encode()
            vote_hash = hashlib.sha256(vote_seed).hexdigest()
            # 70% chance of voting YES in this simulation
            vote = "YES" if int(vote_hash[:2], 16) < 180 else "NO"
            proposal["votes"][node_id] = vote

        yes_votes = list(proposal["votes"].values()).count("YES")
        total_votes = len(proposal["votes"])

        if yes_votes > total_votes / 2:
            proposal["status"] = "APPROVED"
        else:
            proposal["status"] = "REJECTED"

        with open(proposal_file, "w") as f:
            json.dump(proposal, f, indent=4)

        print(f"Consensus Result for {decree_id}: {proposal['status']} ({yes_votes}/{total_votes} YES)")
        return proposal

    def get_all_proposals(self):
        proposals = []
        for p_file in self.proposals_path.glob("*.json"):
            with open(p_file, "r") as f:
                proposals.append(json.load(f))
        return proposals

if __name__ == "__main__":
    consensus = SovereignConsensus()
    consensus.propose_decree("P-001", "Initialize Deep Space Communication")
    consensus.collect_votes("P-001")
