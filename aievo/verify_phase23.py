import os
import json
from pathlib import Path
from threat_sensor import ProactiveSensor
from prediction_engine import StrategicPredictor
from negotiation import NegotiationProtocol

def run_verification():
    print("=== Phase 23 Verification Starting ===")
    base_path = Path("C:/Users/hp/AndroidStudioProjects/AIEVO/aievo")

    sensor = ProactiveSensor(base_path)
    predictor = StrategicPredictor(base_path)
    negotiator = NegotiationProtocol(base_path)

    # 1. Test Proactive Threat Sensing
    print("\n[Step 1] Running Proactive Threat Scan...")
    threats = sensor.scan_activity()
    # Ensure alerts file is updated
    alert_file = base_path / "threat_alerts.json"
    assert alert_file.exists(), "Threat alerts file was not created."
    print(f"Result: Sensor detected {len(threats)} potential issues.")

    # 2. Test Strategic Prediction
    print("\n[Step 2] Generating Strategic Forecast...")
    forecast = predictor.generate_forecast()
    assert forecast is not None, "Failed to generate forecast."
    assert "trend" in forecast
    print(f"Result: Forecast generated: {forecast['trend']} with {forecast['confidence_score']*100}% confidence.")

    # 3. Test Sovereign Negotiation
    print("\n[Step 3] Initiating Strategic Negotiation...")
    session = negotiator.initiate_session()
    assert session["session_id"].startswith("NEG-"), "Invalid session ID."
    assert session["status"] in ["SUCCESS", "PENDING_CREDITS", "FAILED", "IN_PROGRESS"]
    print(f"Result: Negotiation session with {session['entity']} concluded with status: {session['status']}")

    print("\n=== Phase 23 Verification PASSED ===")

if __name__ == "__main__":
    try:
        run_verification()
    except Exception as e:
        print(f"\nVerification FAILED: {e}")
        import traceback
        traceback.print_exc()
        exit(1)
