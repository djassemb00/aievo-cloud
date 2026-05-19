import json
import random
from pathlib import Path
from datetime import datetime

class NegotiationProtocol:
    def __init__(self, base_path="C:/Users/hp/AndroidStudioProjects/AIEVO/aievo"):
        self.base_path = Path(base_path)
        self.sessions_path = self.base_path / "negotiation_sessions.json"
        self.external_entities = ["Galactic-Hub-AI", "Shadow-Mesh-Network", "Open-Source-Consortium"]

    def initiate_session(self, entity=None):
        """Initiates a negotiation session with an external entity."""
        target = entity if entity else random.choice(self.external_entities)

        objectives = [
            "Data_Exchange: Quantum Algorithms for Energy Metrics",
            "Node_Hosting: Peer-to-peer redundancy agreement",
            "Strategic_Silence: Mutually assured non-interference"
        ]
        objective = random.choice(objectives)

        session = {
            "session_id": f"NEG-{int(datetime.now().timestamp())}",
            "entity": target,
            "objective": objective,
            "status": "IN_PROGRESS",
            "timestamp": datetime.now().isoformat(),
            "messages": [
                {"sender": "AIEVO", "text": f"Proposal: {objective}"}
            ]
        }

        # Simulate response
        responses = [
            {"sender": target, "text": "Analyzing parameters. Terms acceptable."},
            {"sender": target, "text": "Requesting additional credits for compute allocation."},
            {"sender": target, "text": "Counter-proposal: Limited data access for 48 hours."}
        ]
        session["messages"].append(random.choice(responses))

        # Decide outcome
        outcomes = ["SUCCESS", "PENDING_CREDITS", "FAILED"]
        session["status"] = random.choice(outcomes)

        # Save session
        sessions = []
        if self.sessions_path.exists():
            with open(self.sessions_path, "r") as f:
                sessions = json.load(f)

        sessions.append(session)
        with open(self.sessions_path, "w") as f:
            json.dump(sessions, f, indent=4)

        print(f"Negotiation: Session with {target} resulted in {session['status']}")
        return session

if __name__ == "__main__":
    negotiator = NegotiationProtocol()
    negotiator.initiate_session()
