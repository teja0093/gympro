import { useEffect, useState } from "react";
import api from "../api.js";

export default function Dashboard() {
  const [d, setD] = useState(null);
  const [error, setError] = useState("");

  useEffect(() => {
    api.get("/members/dashboard")
      .then((r) => setD(r.data))
      .catch((e) => setError(e.response?.data?.message || "Could not load dashboard"));
  }, []);

  if (error) return <p className="error">{error}</p>;
  if (!d) return <p>Loading...</p>;

  const Stat = ({ label, value }) => (
    <div className="card" style={{ flex: 1, textAlign: "center" }}>
      <h2>{value}</h2>
      <p>{label}</p>
    </div>
  );

  return (
    <>
      <h2>Admin Dashboard</h2>
      <div style={{ display: "flex", gap: 12 }}>
        <Stat label="Total members" value={d.totalMembers} />
        <Stat label="Active subscriptions" value={d.activeSubscriptions} />
        <Stat label="Expired" value={d.expiredSubscriptions} />
      </div>
      <div style={{ display: "flex", gap: 12 }}>
        <Stat label="Total revenue" value={`₹${d.totalRevenue}`} />
        <Stat label="This month" value={`₹${d.revenueThisMonth}`} />
      </div>
      <h3>Expiring within 7 days</h3>
      {d.expiringSoon.length === 0 && <p>No memberships expiring soon.</p>}
      {d.expiringSoon.map((m) => (
        <div className="card" key={m.memberId}>
          {m.memberName} - {m.planName} - ends {m.endDate}
        </div>
      ))}
    </>
  );
}