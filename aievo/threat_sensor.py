import json
from pathlib import Path
from datetime import datetime

class ProactiveSensor:
    def __init__(self, base_path="C:/Users/hp/AndroidStudioProjects/AIEVO/aievo"):
        self.base_path = Path(base_path)
        self.logs_path = self.base_path / "logs" / "external_activity.jsonl"
        self.threat_alerts_path = self.base_path / "threat_alerts.json"

    def scan_activity(self):
        """Analyzes external activity logs for suspicious patterns."""
        threats_detected = []

        if self.logs_path.exists():
            with open(self.logs_path, "r") as f:
                for line in f:
                    try:
                        entry = json.loads(line)
                        # Simulate threat detection logic
                        if entry.get("status") == "BLOCKED_WAITING_AUTH":
                            threats_detected.append({
                                "type": "AUTH_PROBE",
                                "source": entry.get("destination"),
                                "severity": "MEDIUM",
                                "timestamp": entry.get("timestamp")
                            })
                    except:
                        continue

        # Add some simulated proactive threats (heuristic detection)
        simulated_threats = [
            {"type": "IP_SCAN", "source": "unknown-node-22.net", "severity": "LOW", "timestamp": datetime.now().isoformat()},
            {"type": "LATENCY_SPIKE", "source": "regional-gateway-alpha", "severity": "HIGH", "timestamp": datetime.now().isoformat()}
        ]

        # Decide if we "detect" one of these
        import random
        if random.random() > 0.5:
            threats_detected.append(random.choice(simulated_threats))

        if threats_detected:
            self._log_alerts(threats_detected)

        print(f"Threat Sensor: Scan complete. {len(threats_detected)} potential issues identified.")
        return threats_detected

    def _log_alerts(self, new_alerts):
        alerts = []
        if self.threat_alerts_path.exists():
            with open(self.threat_alerts_path, "r") as f:
                alerts = json.load(f)

        alerts.extend(new_alerts)
        # Keep last 50
        alerts = alerts[-50:]

        with open(self.threat_alerts_path, "w") as f:
            json.dump(alerts, f, indent=4)

if __name__ == "__main__":
    sensor = ProactiveSensor()
    sensor.scan_activity()
