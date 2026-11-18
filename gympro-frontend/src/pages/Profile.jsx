import { useEffect, useState } from "react";
import api from "../api.js";

export default function Profile() {
  const [form, setForm] = useState({ name: "", phone: "", age: "" });
  const [exists, setExists] = useState(false);
  const [msg, setMsg] = useState("");
  const [error, setError] = useState("");

  useEffect(() => {
    api.get("/members/me")
      .then((r) => { setForm({ name: r.data.name, phone: r.data.phone, age: r.data.age }); setExists(true); })
      .catch(() => setExists(false));
  }, []);

  const change = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const save = async () => {
    setError(""); setMsg("");
    try {
      await api.post("/members/profile", { ...form, age: Number(form.age) });
      setExists(true);
      setMsg("Profile saved!");
    } catch (e) {
      setError(e.response?.data?.message || "Could not save profile");
    }
  };

  return (
    <div className="card">
      <h2>My Profile</h2>
      <input name="name" placeholder="Name" value={form.name} onChange={change} disabled={exists} />
      <input name="phone" placeholder="Phone" value={form.phone} onChange={change} disabled={exists} />
      <input name="age" type="number" placeholder="Age" value={form.age} onChange={change} disabled={exists} />
      {!exists && <button onClick={save}>Save Profile</button>}
      {exists && <p className="success">Your profile is created.</p>}
      <p className="success">{msg}</p>
      <p className="error">{error}</p>
    </div>
  );
}