import os
import json
from pathlib import Path
from consensus import SovereignConsensus
from treasury_bridge import TreasuryBridge
from expansion import ExpansionManager
from immune_system import SovereignImmunity

def run_verification():
    print("=== Phase 22 Verification Starting ===")
    base_path = Path("C:/Users/hp/AndroidStudioProjects/AIEVO/aievo")

    consensus = SovereignConsensus(base_path)
    bridge = TreasuryBridge(base_path)
    expansion = ExpansionManager(base_path)
    immunity = SovereignImmunity(base_path)

    # 1. Test Treasury Governance via Consensus
    print("\n[Step 1] Testing Treasury Governance...")
    decree_id = "DEC-22-FIN-01"
    consensus.propose_decree(decree_id, "Allocate 150 credits for Autonomous Expansion.")

    # Mocking Approval (forcing it for the test)
    proposal_file = base_path / "proposals" / f"{decree_id}.json"
    with open(proposal_file, "r") as f:
        proposal = json.load(f)
    proposal["status"] = "APPROVED"
    with open(proposal_file, "w") as f:
        json.dump(proposal, f, indent=4)

    initial_balance = 0
    with open(base_path / "memory" / "treasury.json", "r") as f:
        initial_balance = json.load(f)["balance_credits"]

    success = bridge.execute_financial_decree(decree_id, 150.0)
    assert success is True, "Financial decree execution failed."

    with open(base_path / "memory" / "treasury.json", "r") as f:
        new_balance = json.load(f)["balance_credits"]
        print(f"Result: Treasury debited. New Balance: {new_balance} (Initial: {initial_balance})")
        assert new_balance == initial_balance - 150.0

    # 2. Test Autonomous Expansion
    print("\n[Step 2] Testing Autonomous Expansion...")
    node_entry = expansion.spawn_node()
    assert node_entry["status"] == "ACTIVE"

    with open(base_path / "memory" / "infrastructure.json", "r") as f:
        infra = json.load(f)
        assert any(n["id"] == node_entry["id"] for n in infra["nodes"])
    print(f"Result: New node {node_entry['id']} integrated into infrastructure.")

    # 3. Test Sovereign Immune System
    print("\n[Step 3] Testing Sovereign Immune System...")
    # Manually corrupt a fragment
    target_node_dir = base_path / "nodes" / "node_1"
    fragment_file = target_node_dir / "dna_fragment.part"

    if fragment_file.exists():
        with open(fragment_file, "w") as f:
            f.write("CORRUPTED-DATA-XYZ")
        print("Result: Manually corrupted node_1 fragment.")
    else:
        print("Warning: node_1/dna_fragment.part not found. Skipping manual corruption.")

    # Run Immune Scan
    print("Triggering Immune Scan...")
    is_healthy = immunity.scan_and_repair()
    # Should return False first time if we count the detection as non-healthy before repair
    # Our scan_and_repair returns True if NO anomalies WERE found.
    # Actually, let's check the logs.

    with open(base_path / "immune_logs.json", "r") as f:
        logs = json.load(f)
        last_log = logs[-1]
        print(f"Immune Logs: Found {last_log['anomalies_found']} anomalies.")
        assert last_log['anomalies_found'] > 0, "Immune system failed to detect corruption."

    # Verify repair
    with open(fragment_file, "r") as f:
        content = f.read()
        assert content == "REPAIRED-CONTENT", "Immune system failed to repair fragment."
    print("Result: Immune system successfully detected and repaired corruption.")

    print("\n=== Phase 22 Verification PASSED ===")

if __name__ == "__main__":
    try:
        run_verification()
    except Exception as e:
        print(f"\nVerification FAILED: {e}")
        import traceback
        traceback.print_exc()
        exit(1)
