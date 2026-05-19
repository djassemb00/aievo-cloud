from fastapi import FastAPI, BackgroundTasks
from pydantic import BaseModel
from singularity_core import SingularityCore
import uvicorn
import json
from pathlib import Path

import os

app = FastAPI(title="AI EVO: Cloud Singularity Core")
core = SingularityCore()

@app.get("/")
def read_root():
    return {"status": "ONLINE", "system": "AI EVO SOVEREIGN CORE"}

@app.get("/status")
def get_status():
    status_path = core.base_path / "singularity_state.json"
    if status_path.exists():
        with open(status_path, "r") as f:
            return json.load(f)
    return {"error": "No state data available"}

if __name__ == "__main__":
    port = int(os.environ.get("PORT", 8000))
    uvicorn.run(app, host="0.0.0.0", port=port)

@app.post("/execute-cycle")
def run_cycle(background_tasks: BackgroundTasks):
    background_tasks.add_task(core.execute_core_cycle)
    return {"message": "Cycle execution started in background"}

@app.post("/issue-decree")
def issue_decree(cmd: Command):
    # Logic to interface with architect_supremacy.py
    return {"message": f"Decree {cmd.type} received and processing"}

if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=8000)
