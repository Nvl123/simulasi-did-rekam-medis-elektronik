import streamlit as st

st.title("Test Tabs Hospital")

tab1, tab2 = st.tabs(["📋 Issue VIC", "🔗 VIC Sharing"])

with tab1:
    st.write("This is Issue VIC tab")

with tab2:
    st.write("This is VIC Sharing tab")
