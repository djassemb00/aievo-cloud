import os
import json
from pathlib import Path
from asset_registry import SovereignAssetRegistry
from fortress_protocol import PropertyFortress
from sovereign_contracts import SovereignContractManager

def run_verification():
    print("=== Phase 27 Verification Starting ===")
    base_path = Path("C:/Users/hp/AndroidStudioProjects/AIEVO/aievo")

    registry = SovereignAssetRegistry(base_path)
    fortress = PropertyFortress(base_path)
    contract_manager = SovereignContractManager(base_path)

    # 1. Test Asset Registration
    print("\n[Step 1] Formalizing Sovereign Assets...")
    asset1 = registry.register_asset("Galactic Node Cluster-1", "INFRASTRUCTURE", 12000.0)
    asset2 = registry.register_asset("AIEVO Core DNA Ledger", "DIGITAL_PROPERTY", 50000.0)
    assert asset1["status"] == "PROTECTED"
    print(f"Result: {len(registry.load_registry())} assets formalized in registry.")

    # 2. Test Sovereign Contracts
    print("\n[Step 2] Executing Lease Agreement...")
    contract = contract_manager.create_lease_agreement(asset1["asset_id"], "Orion-Research-Lab", 60, 1500.0)
    assert contract["status"] == "ACTIVE"
    print(f"Result: Contract {contract['contract_id']} for asset {asset1['asset_id']} finalized.")

    # 3. Test Ownership Protection (Fortress)
    print("\n[Step 3] Simulating Ownership Hijack Attempt...")
    # Manually corrupt an entry in the registry file
    current_registry = registry.load_registry()
    target_id = list(current_registry.keys())[0]
    original_fp = current_registry[target_id]["owner_fingerprint"]

    current_registry[target_id]["owner_fingerprint"] = "FRAUDULENT-OWNER-666"
    with open(registry.registry_path, "w") as f:
        json.dump(current_registry, f, indent=4)
    print(f"Result: Injected fake owner into asset {target_id}.")

    print("Triggering Ownership Audit...")
    all_secure = fortress.audit_ownership()
    # audit_ownership returns False if anomalies WERE found (meaning it had to intervene)
    # Our implementation: returns True if len(anomalies) == 0.
    assert all_secure is False, "Fortress failed to detect ownership anomaly."

    # Verify recovery
    recovered_registry = registry.load_registry()
    assert recovered_registry[target_id]["owner_fingerprint"] == original_fp
    print("Result: Property Fortress detected and REVERSED the hijack attempt.")

    # 4. Final Valuation Check
    total = registry.get_total_valuation()
    print(f"\n[Step 4] Final Domain Valuation: {total} ₡")
    assert total >= 62000.0

    print("\n=== Phase 27 Verification PASSED ===")
    print("Sovereign Ownership has been established and secured.")

if __name__ == "__main__":
    try:
        run_verification()
    except Exception as e:
        print(f"\nVerification FAILED: {e}")
        import traceback
        traceback.print_exc()
        exit(1)
