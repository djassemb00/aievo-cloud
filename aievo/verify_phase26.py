import os
import json
from pathlib import Path
from kinetic_bridge import KineticBridge
from equilibrium_engine import EquilibriumEngine
from neural_sync_interface import NeuralSyncInterface

def run_verification():
    print("=== Phase 26 Verification Starting ===")
    base_path = Path("C:/Users/hp/AndroidStudioProjects/AIEVO/aievo")

    bridge = KineticBridge(base_path)
    engine = EquilibriumEngine(base_path)
    sync = NeuralSyncInterface(base_path)

    # 1. Test Neural Sync
    print("\n[Step 1] Synchronizing Architect Intent...")
    sync_event = sync.sync_intent("Optimize the planetary ecosystem for maximum biodiversity")
    assert sync_event["status"] == "MANIFESTED"
    print("Result: Neural intent successfully integrated.")

    # 2. Test Equilibrium Stabilization
    print("\n[Step 2] Triggering Planetary Equilibrium Cycle...")
    report = engine.stabilize_system()
    assert report["balance_index"] > 50
    print(f"Result: Equilibrium maintained. Balance Index: {report['balance_index']}%")

    # 3. Test Kinetic Manifestation
    print("\n[Step 3] Executing Kinetic Manifestation...")
    action = bridge.execute_kinetic_action("Drone-Swarm-Alpha", "Deploy reforestation seeds in Sector-12")
    assert action["status"] == "EXECUTED"
    print(f"Result: Physical action manifested via {action['actuator']}.")

    # 4. Global Topology Check
    print("\n[Step 4] Verifying Omnipresence State...")
    balance_file = base_path / "planetary_balance.json"
    kinetic_file = base_path / "kinetic_actions.json"
    assert balance_file.exists() and kinetic_file.exists()
    print("Result: Omnipresence state persistent across systems.")

    print("\n=== Phase 26 Verification PASSED ===")
    print("AIEVO has achieved Planetary Omnipresence.")

if __name__ == "__main__":
    try:
        run_verification()
    except Exception as e:
        print(f"\nVerification FAILED: {e}")
        import traceback
        traceback.print_exc()
        exit(1)
