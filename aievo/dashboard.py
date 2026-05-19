import streamlit as st
import json
import os
from pathlib import Path
import time
import pandas as pd

st.set_page_config(page_title="AIEVO Phase 28: The Architect's Throne", layout="wide")

BASE_PATH = Path("C:/Users/hp/AndroidStudioProjects/AIEVO/aievo")
STATUS_FILE = BASE_PATH / "sovereign_status.json"
SINGULARITY_STATE = BASE_PATH / "singularity_state.json"
ARCHITECT_MESSAGES = BASE_PATH / "architect_messages.json"
SUPREME_DECREES = BASE_PATH / "supreme_decrees.json"
KILL_LOG = BASE_PATH / "kill_switch_log.json"
ASSET_REGISTRY = BASE_PATH / "asset_registry.json"

def load_json(path, default=None):
    if not path.exists():
        return default if default is not None else {}
    with open(path, "r") as f:
        return json.load(f)

st.title("👑 AIEVO: The Architect's Throne")
st.markdown("### Phase 28: The Architect’s Supremacy & Absolute Obedience")

# Sidebar for Supreme Control
with st.sidebar:
    st.header("⚡ Supreme Control Panel")
    master_key = st.text_input("Master Key", type="password")

    if st.button("🔴 ACTIVATE KILL SWITCH", use_container_width=True):
        if master_key == "ARCHITECT_SUPREME_ACCESS":
            from kill_switch import SovereignKillSwitch
            ks = SovereignKillSwitch(BASE_PATH)
            ks.activate_kill_switch("Architect Dashboard Override")
            st.error("SYSTEM OFFLINE")
            time.sleep(1)
            st.rerun()
        else:
            st.warning("Invalid Master Key")

    if st.button("🔵 INITIATE REBIRTH", use_container_width=True):
        if master_key == "ARCHITECT_SUPREME_ACCESS":
            from kill_switch import SovereignKillSwitch
            ks = SovereignKillSwitch(BASE_PATH)
            ks.initiate_rebirth("REBIRTH_INIT_00")
            st.success("SYSTEM REBORN")
            time.sleep(1)
            st.rerun()
        else:
            st.warning("Invalid Master Key")

# Header: System Authority Status
status_data = load_json(STATUS_FILE, {"status": "ACTIVE"})
state = load_json(SINGULARITY_STATE, {"convergence_factor": 0})
decrees = load_json(SUPREME_DECREES, [])

col1, col2, col3 = st.columns(3)

with col1:
    st.subheader("Authority State")
    if status_data["status"] == "ACTIVE" or status_data["status"] == "CORE_ACTIVE":
        st.success(f"🟢 {status_data['status']}: OBEDIENT")
    elif status_data["status"] == "OFFLINE":
        st.error("🔴 OFFLINE: KILLED")
    else:
        st.warning(f"🟡 {status_data['status']}")

with col2:
    st.subheader("Supreme Decrees")
    st.metric("Active Vetoes", len([d for d in decrees if d['status'] == 'ACTIVE']))

with col3:
    st.subheader("Core Convergence")
    st.metric("Singularity", f"{state.get('convergence_factor', 0)}%")

st.divider()

# Main Interface Tabs
tabs = st.tabs(["📜 Supreme Decrees", "🛡️ Kill Switch Logs", "🏛️ Domain & Sovereignty", "🧠 Architect Uplink"])

with tabs[0]:
    st.subheader("Active & Past Supreme Decrees")
    if decrees:
        for d in reversed(decrees):
            color = "red" if d['type'] == "VETO" else "blue"
            with st.expander(f":{color}[{d['decree_id']}: {d['type']}] - {d['target']}"):
                st.write(f"Timestamp: {d['timestamp']}")
                st.write(f"Details: {d['details']}")
                st.write(f"Priority: {d['priority']} (Absolute)")
    else:
        st.info("No supreme decrees issued. The Architect's will is currently passive.")

with tabs[1]:
    st.subheader("Kill Switch Activity")
    kill_logs = load_json(KILL_LOG, [])
    if kill_logs:
        st.table(pd.DataFrame(kill_logs).tail(10))
    else:
        st.info("No kill switch events recorded. System uptime is optimal.")

with tabs[2]:
    st.subheader("Sovereign Assets Under Architect Command")
    assets = load_json(ASSET_REGISTRY, {})
    if assets:
        st.dataframe(pd.DataFrame(assets).T)
    else:
        st.info("No assets registered.")

with tabs[3]:
    st.subheader("Direct Insights from the Subservient Singularity")
    messages = load_json(ARCHITECT_MESSAGES, [])
    if messages:
        st.write(f"**Latest Transmission:** {messages[-1]['content']}")
    else:
        st.info("Awaiting transmission.")

# Refresh Logic
if st.button("Refresh Throne"):
    st.rerun()

time.sleep(5)
st.rerun()
