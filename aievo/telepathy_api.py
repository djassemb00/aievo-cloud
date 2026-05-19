from sync_protocol import NeuralSync
import random

class TelepathyAPI:
    def __init__(self, base_path="C:/Users/hp/AndroidStudioProjects/AIEVO/aievo"):
        self.sync = NeuralSync(base_path)
        self.external_sources = ["Orion-Network", "Sirius-B-Relay", "Deep-Core-Pulse"]

    def receive_signal(self):
        """Simulates receiving a signal from an external cosmic source."""
        source = random.choice(self.external_sources)
        signals = [
            {"key": "Star_Map_Delta", "value": "Sector-7G-Acquired"},
            {"key": "Quantum_Shift", "value": "Phase-Variance-0.002"},
            {"key": "Exo_Intelligence", "value": "Signal-Detected-A-1"}
        ]
        signal = random.choice(signals)

        print(f"Telepathy: Signal received from {source}: {signal['key']} = {signal['value']}")
        self.sync.broadcast_insight(source, signal["key"], signal["value"])
        return {**signal, "source": source}

if __name__ == "__main__":
    api = TelepathyAPI()
    api.receive_signal()
