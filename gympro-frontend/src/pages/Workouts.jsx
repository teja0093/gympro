import { useEffect, useState } from "react";
import api from "../api.js";

export default function Workouts() {
  const [plans, setPlans] = useState([]);
  const [diets, setDiets] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    Promise.all([api.get("/workouts/my-plans"), api.get("/workouts/my-diets")])
      .then(([p, d]) => {
        setPlans(Array.isArray(p.data) ? p.data : []);
        setDiets(Array.isArray(d.data) ? d.data : []);
      })
      .catch((e) =>
        setError(e.response?.data?.message || `Could not load plans (status: ${e.response?.status ?? "no response"})`)
      )
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <p>Loading...</p>;

  return (
    <>
      {error && <p className="error">{error}</p>}

      <h2>My Workout Plans</h2>
      {plans.length === 0 && <p>No workout plans yet.</p>}
      {plans.map((p) => (
        <div className="card" key={p.id}>
          <h3>{p.title}</h3>
          <p>Goal: {p.goal}</p>
          <ul>
            {(p.exercises || []).map((e, i) => (
              <li key={e.id ?? i}>{e.name}: {e.sets} sets x {e.reps} reps</li>
            ))}
          </ul>
        </div>
      ))}

      <h2>My Diet Plans</h2>
      {diets.length === 0 && <p>No diet plans yet.</p>}
      {diets.map((d) => (
        <div className="card" key={d.id}>
          <h3>{d.title} ({d.caloriesPerDay} kcal/day)</h3>
          <p>{d.meals}</p>
        </div>
      ))}
    </>
  );
}