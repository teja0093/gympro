import { useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../api.js";

export default function Login() {
  const [isRegister, setIsRegister] = useState(false);
  const [form, setForm] = useState({ name: "", email: "", password: "" });
  const [msg, setMsg] = useState("");
  const [error, setError] = useState("");
  const navigate = useNavigate();

  const change = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const submit = async () => {
    setError(""); setMsg("");
    try {
      if (isRegister) {
        await api.post("/auth/register", { ...form, role: "MEMBER" });
        setMsg("Registered! Now log in.");
        setIsRegister(false);
      } else {
        const res = await api.post("/auth/login", { email: form.email, password: form.password });
        localStorage.setItem("token", res.data.token);
        localStorage.setItem("email", res.data.email);
        localStorage.setItem("role", res.data.role);

        // Send each role to its own home page
        const role = res.data.role;
        if (role === "ADMIN") navigate("/admin");
        else if (role === "TRAINER") navigate("/trainer");
        else navigate("/profile");
      }
    } catch (e) {
      const data = e.response?.data;
      setError(data?.fieldErrors ? Object.values(data.fieldErrors).join(", ") : data?.message || "Something went wrong");
    }
  };

  return (
    <div className="card">
      <h2>{isRegister ? "Register" : "Login"}</h2>
      {isRegister && <input name="name" placeholder="Name" value={form.name} onChange={change} />}
      <input name="email" placeholder="Email" value={form.email} onChange={change} />
      <input name="password" type="password" placeholder="Password (min 6)" value={form.password} onChange={change} />
      <button onClick={submit}>{isRegister ? "Register" : "Login"}</button>
      <p className="error">{error}</p>
      <p className="success">{msg}</p>
      <a href="#" onClick={(e) => { e.preventDefault(); setIsRegister(!isRegister); }}>
        {isRegister ? "Already have an account? Login" : "New here? Register"}
      </a>
    </div>
  );
}