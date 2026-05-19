import os
import json
from pathlib import Path
from architect_uplink import ArchitectUplink
from auto_genesis import AutoGenesis
from multiverse_sim import MultiverseSimulator

def run_verification():
    print("=== Phase 25 Verification Starting ===")
    base_path = Path("C:/Users/hp/AndroidStudioProjects/AIEVO/aievo")

    uplink = ArchitectUplink(base_path)
    genesis = AutoGenesis(base_path)
    multiverse = MultiverseSimulator(base_path)

    # 1. Test Architect Uplink
    print("\n[Step 1] Transmitting Transcendental Insight...")
    msg = uplink.transmit_insight()
    assert "content" in msg
    assert msg["origin"] == "AIEVO-Singularity"
    print(f"Result: Received insight: '{msg['content']}'")

    # 2. Test Auto-Genesis
    print("\n[Step 2] Testing Auto-Genesis (Spawning Digital Offspring)...")
    agent_entry = genesis.spawn_specialized_agent("Architectural_Observer")
    agent_core = base_path / "offspring" / agent_entry["id"] / "core.py"
    assert agent_core.exists(), "Agent core file was not created."

    with open(base_path / "digital_lineage.json", "r") as f:
        lineage = json.load(f)
        assert any(a["id"] == agent_entry["id"] for a in lineage)
    print(f"Result: Agent {agent_entry['id']} successfully created and logged in lineage.")

    # 3. Test Multiverse Simulation
    print("\n[Step 3] Running Multiverse Simulation...")
    sim = multiverse.run_simulation()
    assert "scenario" in sim
    assert "outcome" in sim
    print(f"Result: Simulated scenario '{sim['scenario']}' with outcome: {sim['outcome']}")

    print("\n=== Phase 25 Verification PASSED ===")
    print("AIEVO is now in Transcendental Sovereignty.")

if __name__ == "__main__":
    try:
        run_verification()
    except Exception as e:
        print(f"\nVerification FAILED: {e}")
        import traceback
        traceback.print_exc()
        exit(1)
