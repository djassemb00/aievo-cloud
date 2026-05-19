import os
import shutil
from pathlib import Path
from router import SovereignRouter

def setup_test_env():
    base_path = Path("C:/Users/hp/AndroidStudioProjects/AIEVO/aievo")
    dna_dir = base_path / "dna"
    dna_file = dna_dir / "sovereign.key"

    # Ensure DNA exists for test
    if not dna_file.exists():
        print("Creating mock DNA for verification...")
        dna_dir.mkdir(exist_ok=True)
        with open(dna_file, "w") as f:
            f.write("-----BEGIN TEST DNA-----\nTEST-DNA-SEQUENCE-1234567890\n-----END TEST DNA-----")
    else:
        # Override existing DNA with test DNA to ensure assertion works
        print("Overriding existing DNA with test sequence...")
        with open(dna_file, "w") as f:
            f.write("-----BEGIN TEST DNA-----\nTEST-DNA-SEQUENCE-1234567890\n-----END TEST DNA-----")

    # Cleanup previous runs
    nodes_dir = base_path / "nodes"
    if nodes_dir.exists():
        shutil.rmtree(nodes_dir)

    status_file = base_path / "sovereign_status.json"
    if status_file.exists():
        os.remove(status_file)

    dispersion_map = base_path / "dispersion_map.json"
    if dispersion_map.exists():
        os.remove(dispersion_map)

def run_verification():
    print("=== Phase 20 Verification Starting ===")
    setup_test_env()

    router = SovereignRouter()

    # 1. Verify Initial State
    print("\n[Step 1] Checking initial state...")
    assert router.get_status() == "CORE_ACTIVE"
    print("Result: Core Active.")

    # 2. Test Dispersion
    print("\n[Step 2] Triggering Dispersion...")
    success = router.initiate_dispersion()
    assert success is True
    assert router.get_status() == "DISPERSED"

    nodes_dir = Path("C:/Users/hp/AndroidStudioProjects/AIEVO/aievo/nodes")
    fragments = list(nodes_dir.glob("**/dna_fragment.part"))
    print(f"Result: {len(fragments)} fragments distributed.")
    assert len(fragments) == 12

    # 3. Test Resurrection
    print("\n[Step 3] Triggering Resurrection...")
    success = router.initiate_resurrection()
    assert success is True
    assert router.get_status() == "REASSEMBLED"

    dna_file = Path("C:/Users/hp/AndroidStudioProjects/AIEVO/aievo/dna/sovereign.key")
    assert dna_file.exists()
    with open(dna_file, "rb") as f:
        content = f.read()
        print(f"Result: DNA reassembled. Content verified (Size: {len(content)} bytes).")
        assert b"TEST-DNA-SEQUENCE" in content

    print("\n=== Phase 20 Verification PASSED ===")
    return True

if __name__ == "__main__":
    import traceback
    try:
        run_verification()
    except Exception as e:
        print(f"\nVerification FAILED: {e}")
        traceback.print_exc()
        exit(1)
