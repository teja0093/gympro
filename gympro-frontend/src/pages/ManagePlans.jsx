import { useEffect, useState } from "react";
import api from "../api.js";

export default function ManagePlans() {
  const [plans, setPlans] = useState([]);
  const [form, setForm] = useState({ name: "", durationDays: "", price: "", description: "" });
  const [msg, setMsg] = useState("");
  const [error, setError] = useState("");

  const load = () => api.get("/plans").then((r) => setPlans(r.data));
  useEffect(() => { load(); }, []);

  const change = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const create = async () => {
    setError(""); setMsg("");
    try {
      await api.post("/plans", {
        ...form,
        durationDays: Number(form.durationDays),
        price: Number(form.price),
      });
      setMsg("Plan created");
      setForm({ name: "", durationDays: "", price: "", description: "" });
      load();
    } catch (e) {
      const data = e.response?.data;
      setError(data?.fieldErrors ? Object.values(data.fieldErrors).join(", ") : data?.message || "Failed");
    }
  };

  const remove = async (id) => {
    try { await api.delete(`/plans/${id}`); load(); }
    catch (e) { setError(e.response?.data?.message || "Could not delete"); }
  };

  return (
    <>
      <div className="card">
        <h2>Create plan</h2>
        <input name="name" placeholder="Name (e.g. Quarterly)" value={form.name} onChange={change} />
        <input name="durationDays" type="number" placeholder="Duration in days" value={form.durationDays} onChange={change} />
        <input name="price" type="number" placeholder="Price" value={form.price} onChange={change} />
        <input name="description" placeholder="Description" value={form.description} onChange={change} />
        <button onClick={create}>Create</button>
        <p className="success">{msg}</p>
        <p className="error">{error}</p>
      </div>
      <h3>Existing plans</h3>
      {plans.map((p) => (
        <div className="card" key={p.id}>
          <b>{p.name}</b> - ₹{p.price} - {p.durationDays} days{" "}
          <button onClick={() => remove(p.id)}>Delete</button>
        </div>
      ))}
    </>
  );
}