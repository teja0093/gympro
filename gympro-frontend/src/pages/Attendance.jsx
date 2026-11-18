import { useEffect, useState } from "react";
import api from "../api.js";

export default function Attendance() {
  const [list, setList] = useState([]);
  const [msg, setMsg] = useState("");
  const [error, setError] = useState("");

  const load = () =>
    api.get("/attendance/me").then((r) => setList(r.data)).catch(() => {});

  useEffect(() => { load(); }, []);

  const act = async (path, okMsg) => {
    setError(""); setMsg("");
    try {
      await api.post(path);
      setMsg(okMsg);
      load();
    } catch (e) {
      setError(e.response?.data?.message || "Action failed");
    }
  };

  const fmt = (t) => (t ? new Date(t).toLocaleString() : "-");

  return (
    <>
      <div className="card">
        <h2>Attendance</h2>
        <button onClick={() => act("/attendance/check-in", "Checked in!")}>Check In</button>{" "}
        <button onClick={() => act("/attendance/check-out", "Checked out!")}>Check Out</button>
        <p className="success">{msg}</p>
        <p className="error">{error}</p>
      </div>
      <h3>History</h3>
      {list.length === 0 && <p>No visits yet.</p>}
      {list.map((a) => (
        <div className="card" key={a.id}>
          In: {fmt(a.checkIn)} | Out: {fmt(a.checkOut)}
        </div>
      ))}
    </>
  );
}