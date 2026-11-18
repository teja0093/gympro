import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import api from "../api.js";

export default function Subscription() {
  const [sub, setSub] = useState(null);
  const [error, setError] = useState("");

  useEffect(() => {
    api.get("/members/me/subscription")
      .then((r) => setSub(r.data))
      .catch((e) => setError(e.response?.data?.message || "Could not load subscription"));
  }, []);

  return (
    <div className="card">
      <h2>My Subscription</h2>
      {sub ? (
        <>
          <p><b>Plan:</b> {sub.plan?.name}</p>
          <p><b>Status:</b> <span className="success">{sub.status}</span></p>
          <p><b>Start:</b> {sub.startDate}</p>
          <p><b>End:</b> {sub.endDate}</p>
        </>
      ) : (
        <>
          <p className="error">{error}</p>
          <Link to="/plans">Buy a plan</Link>
        </>
      )}
    </div>
  );
}