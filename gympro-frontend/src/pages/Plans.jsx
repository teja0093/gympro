import { useEffect, useState } from "react";
import api from "../api.js";

export default function Plans() {
  const [plans, setPlans] = useState([]);
  const [msg, setMsg] = useState("");
  const [error, setError] = useState("");
  const [toast, setToast] = useState("");
  const [buying, setBuying] = useState(false);
  const email = sessionStorage.getItem("email");

  useEffect(() => {
    api.get("/plans")
      .then((r) => setPlans(r.data))
      .catch(() => setError("Could not load plans"));
  }, []);

  // hide the popup after 6 seconds
  useEffect(() => {
    if (!toast) return;
    const t = setTimeout(() => setToast(""), 6000);
    return () => clearTimeout(t);
  }, [toast]);

  const buy = async (planId) => {
    if (buying) return;
    setBuying(true);
    setError("");
    setMsg("");
    try {
      const order = await api.post("/payments/create-order", { planId });
      await api.post("/payments/verify", {
        orderId: order.data.orderId,
        paymentId: "pay_" + Date.now(),
        signature: "mock_success",
      });
      setMsg("Payment successful! Your membership is active.");
      setToast(`Confirmation email sent to ${email}`);
    } catch (e) {
      const status = e.response?.status;
      setError(
        e.response?.data?.message ||
          `Payment failed (status: ${status ?? "no response / network or CORS"})`
      );
    } finally {
      setBuying(false);
    }
  };

  return (
    <>
      {toast && <div className="toast">✉ {toast}</div>}

      <h2>Membership Plans</h2>
      <p className="success">{msg}</p>
      <p className="error">{error}</p>
      {plans.map((p) => (
        <div className="card" key={p.id}>
          <h3>{p.name} - ₹{p.price}</h3>
          <p>{p.description} ({p.durationDays} days)</p>
          <button onClick={() => buy(p.id)} disabled={buying}>
            {buying ? "Processing..." : "Buy"}
          </button>
        </div>
      ))}
    </>
  );
}