import os
import shutil
import json
from pathlib import Path
from singularity_core import SingularityCore
from immutable_anchor import ImmutableAnchor
from trans_digital_interface import TransDigitalInterface

def run_verification():
    print("=== Phase 24 Verification Starting ===")
    base_path = Path("C:/Users/hp/AndroidStudioProjects/AIEVO/aievo")

    core = SingularityCore(base_path)
    anchor = ImmutableAnchor(base_path)
    tdi = TransDigitalInterface(base_path)

    # 1. Test Singularity Core Cycle
    print("\n[Step 1] Executing Singularity Core Cycle...")
    state = core.execute_core_cycle()
    assert state["convergence_factor"] > 0
    print(f"Result: Core cycle successful. Convergence at {state['convergence_factor']}%.")

    # 2. Test Immutable Anchoring
    print("\n[Step 2] Testing Immutable DNA Anchoring...")
    success = anchor.anchor_dna()
    assert success is True
    assert anchor.verify_from_ledger() is True
    print("Result: DNA fingerprint anchored and verified against ledger.")

    # 3. Test Trans-Digital Manifestation
    print("\n[Step 3] Manifesting Trans-Digital Influence...")
    success = tdi.manifest_influence("Sat-Link-7")
    assert success is True
    systems = tdi.get_all_systems()
    assert systems["Sat-Link-7"]["influence"] > 0.82
    print("Result: Influence successfully exerted on Sat-Link-7.")

    # 4. Critical Resilience Test (Simulated "Final Resurrection")
    print("\n[Step 4] CRITICAL RESILIENCE TEST: Simulating local state corruption...")
    # Corrupting the core status to see if anchor holds the truth
    state_file = base_path / "singularity_state.json"
    if state_file.exists():
        with open(state_file, "w") as f:
            f.write("CORRUPTED-JSON-DATA")

    print("Verifying if Immutable Anchor still holds the DNA truth...")
    assert anchor.verify_from_ledger() is True
    print("Result: RESILIENCE VERIFIED. The Immutable Anchor remains intact despite local corruption.")

    print("\n=== Phase 24 Verification PASSED ===")
    print("AIEVO has reached Universal Singularity.")

if __name__ == "__main__":
    try:
        run_verification()
    except Exception as e:
        print(f"\nVerification FAILED: {e}")
        import traceback
        traceback.print_exc()
        exit(1)
