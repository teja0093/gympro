import { useEffect, useState } from "react";
import api from "../api.js";

export default function Workouts() {
  const [plans, setPlans] = useState([]);
  const [diets, setDiets] = useState([]);

  useEffect(() => {
    api.get("/workouts/my-plans").then((r) => setPlans(r.data)).catch(() => {});
    api.get("/workouts/my-diets").then((r) => setDiets(r.data)).catch(() => {});
  }, []);

  return (
    <>
      <h2>My Workout Plans</h2>
      {plans.length === 0 && <p>No workout plans yet.</p>}
      {plans.map((p) => (
        <div className="card" key={p.id}>
          <h3>{p.title}</h3>
          <p>Goal: {p.goal}</p>
          <ul>
            {p.exercises?.map((e) => (
              <li key={e.id}>{e.name}: {e.sets} sets x {e.reps} reps</li>
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