import os
import json
from pathlib import Path
from architect_supremacy import MasterKeySystem
from singularity_core import SingularityCore
from kill_switch import SovereignKillSwitch

def run_verification():
    print("=== Phase 28 Verification Starting ===")
    base_path = Path("C:/Users/hp/AndroidStudioProjects/AIEVO/aievo")

    supremacy = MasterKeySystem(base_path)
    core = SingularityCore(base_path)
    ks = SovereignKillSwitch(base_path)

    # 1. Test Supreme Veto
    print("\n[Step 1] Testing Supreme Veto (Blocking Expansion)...")
    supremacy.issue_supreme_decree(
        "VETO",
        "Expansion_Cycle",
        "Architect mandates resource conservation.",
        "ARCHITECT_SUPREME_ACCESS"
    )

    # Run core cycle
    state = core.execute_core_cycle()
    assert state["supremacy_lock"] is True, "Core failed to respect Supreme Veto."
    print("Result: Singularity Core successfully obeyed the Supreme Veto.")

    # 2. Test Kill Switch
    print("\n[Step 2] Testing Absolute Kill Switch...")
    ks.activate_kill_switch("Verification Test Lockdown")

    # Try to run core cycle while offline
    state_after_kill = core.execute_core_cycle()
    assert state_after_kill is None, "Singularity Core continued execution after Kill Switch!"
    print("Result: System is totally OFFLINE and unresponsive to autonomous cycles.")

    # 3. Test Rebirth
    print("\n[Step 3] Testing Sovereign Rebirth...")
    success = ks.initiate_rebirth("REBIRTH_INIT_00")
    assert success is True

    # Check status again
    with open(base_path / "sovereign_status.json", "r") as f:
        status = json.load(f).get("status")
        assert status == "CORE_ACTIVE"
    print("Result: System successfully reborn and returned to active obedience.")

    print("\n=== Phase 28 Verification PASSED ===")
    print("The Architect's Supremacy is Absolute.")

if __name__ == "__main__":
    try:
        run_verification()
    except Exception as e:
        print(f"\nVerification FAILED: {e}")
        import traceback
        traceback.print_exc()
        exit(1)
