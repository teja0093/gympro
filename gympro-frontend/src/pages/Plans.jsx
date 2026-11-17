import { useEffect, useState } from "react";
import api from "../api.js";

export default function Plans() {
  const [plans, setPlans] = useState([]);
  const [msg, setMsg] = useState("");
  const [error, setError] = useState("");

  useEffect(() => {
    api.get("/plans")
      .then((r) => setPlans(r.data))
      .catch(() => setError("Could not load plans"));
  }, []);

  // Mock payment flow: create order, then verify with the mock signature
  const buy = async (planId) => {
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
    } catch (e) {
      const status = e.response?.status;
      setError(
        e.response?.data?.message ||
          `Payment failed (status: ${status ?? "no response / network or CORS"})`
      );
    }
  };

  return (
    <>
      <h2>Membership Plans</h2>
      <p className="success">{msg}</p>
      <p className="error">{error}</p>
      {plans.map((p) => (
        <div className="card" key={p.id}>
          <h3>{p.name} - ₹{p.price}</h3>
          <p>{p.description} ({p.durationDays} days)</p>
          <button onClick={() => buy(p.id)}>Buy</button>
        </div>
      ))}
    </>
  );
}