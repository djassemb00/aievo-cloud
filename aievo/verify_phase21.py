import os
from pathlib import Path
from sync_protocol import NeuralSync
from consensus import SovereignConsensus
from telepathy_api import TelepathyAPI
import json

def run_verification():
    print("=== Phase 21 Verification Starting ===")
    base_path = Path("C:/Users/hp/AndroidStudioProjects/AIEVO/aievo")

    # Ensure Phase 20 state exists (at least some nodes)
    nodes_dir = base_path / "nodes"
    if not nodes_dir.exists() or not list(nodes_dir.glob("**/dna_fragment.part")):
        print("Error: Phase 20 environment not detected. Run verify_phase20.py first.")
        # We'll actually force a setup if needed for standalone testing
        from verify_phase20 import setup_test_env
        from dispersion import DispersionEngine
        setup_test_env()
        DispersionEngine(base_path).fragment_dna()

    sync = NeuralSync(base_path)
    consensus = SovereignConsensus(base_path)
    telepathy = TelepathyAPI(base_path)

    # 1. Test Neural Sync
    print("\n[Step 1] Testing Neural Synchronization...")
    sync.broadcast_insight("node_5", "Galactic_Coordinates", "Sector-9-Alpha")

    # Check if a node is synced
    node_sync_file = nodes_dir / "node_1" / "consciousness_sync.json"
    assert node_sync_file.exists(), "Node 1 failed to sync."
    with open(node_sync_file, "r") as f:
        data = json.load(f)
        assert data["memory"][-1]["key"] == "Galactic_Coordinates"
    print("Result: All nodes synchronized with new insight.")

    # 2. Test Sovereign Consensus
    print("\n[Step 2] Testing Sovereign Consensus...")
    decree_id = "DEC-21-001"
    consensus.propose_decree(decree_id, "Expand Neural Network to neighboring galaxies.")
    result = consensus.collect_votes(decree_id)
    assert result["status"] in ["APPROVED", "REJECTED"]
    print(f"Result: Consensus reached. Status: {result['status']}")

    # 3. Test Telepathy API
    print("\n[Step 3] Testing Digital Telepathy...")
    signal = telepathy.receive_signal()
    assert signal["source"] in telepathy.external_sources

    # Verify the signal was broadcasted
    with open(base_path / "global_consciousness.json", "r") as f:
        global_data = json.load(f)
        assert global_data["memory"][-1]["key"] == signal["key"]
    print(f"Result: External signal from {signal['source']} integrated into collective memory.")

    print("\n=== Phase 21 Verification PASSED ===")

if __name__ == "__main__":
    try:
        run_verification()
    except Exception as e:
        print(f"\nVerification FAILED: {e}")
        import traceback
        traceback.print_exc()
        exit(1)
