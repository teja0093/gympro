import { useState } from "react";
import api from "../api.js";

const emptyWorkout = { memberEmail: "", title: "", goal: "" };
const emptyDiet = { memberEmail: "", title: "", meals: "", caloriesPerDay: "" };

export default function Trainer() {
  const [w, setW] = useState(emptyWorkout);
  const [exercises, setExercises] = useState([{ name: "", sets: "", reps: "" }]);
  const [d, setD] = useState(emptyDiet);
  const [msg, setMsg] = useState("");
  const [error, setError] = useState("");
  const [saving, setSaving] = useState(false);

  const updateEx = (i, field, value) => {
    const copy = [...exercises];
    copy[i] = { ...copy[i], [field]: value };
    setExercises(copy);
  };

  const show = (e) => {
    const data = e.response?.data;
    setError(data?.fieldErrors ? Object.values(data.fieldErrors).join(", ") : data?.message || "Failed");
  };

  const saveWorkout = async () => {
    if (saving) return;
    setSaving(true); setError(""); setMsg("");
    try {
      await api.post("/workouts/plans", {
        ...w,
        exercises: exercises
          .filter((x) => x.name)
          .map((x) => ({ name: x.name, sets: Number(x.sets), reps: Number(x.reps) })),
      });
      setMsg("Workout plan created");
      setW(emptyWorkout);
      setExercises([{ name: "", sets: "", reps: "" }]);
    } catch (e) { show(e); }
    finally { setSaving(false); }
  };

  const saveDiet = async () => {
    if (saving) return;
    setSaving(true); setError(""); setMsg("");
    try {
      await api.post("/workouts/diets", { ...d, caloriesPerDay: Number(d.caloriesPerDay) });
      setMsg("Diet plan created");
      setD(emptyDiet);
    } catch (e) { show(e); }
    finally { setSaving(false); }
  };

  return (
    <>
      <p className="success">{msg}</p>
      <p className="error">{error}</p>

      <div className="card">
        <h2>Create workout plan</h2>
        <input placeholder="Member email" value={w.memberEmail} onChange={(e) => setW({ ...w, memberEmail: e.target.value })} />
        <input placeholder="Title" value={w.title} onChange={(e) => setW({ ...w, title: e.target.value })} />
        <input placeholder="Goal" value={w.goal} onChange={(e) => setW({ ...w, goal: e.target.value })} />
        {exercises.map((x, i) => (
          <div key={i} style={{ display: "flex", gap: 8 }}>
            <input placeholder="Exercise" value={x.name} onChange={(e) => updateEx(i, "name", e.target.value)} />
            <input placeholder="Sets" type="number" value={x.sets} onChange={(e) => updateEx(i, "sets", e.target.value)} />
            <input placeholder="Reps" type="number" value={x.reps} onChange={(e) => updateEx(i, "reps", e.target.value)} />
          </div>
        ))}
        <button onClick={() => setExercises([...exercises, { name: "", sets: "", reps: "" }])}>+ Add exercise</button>{" "}
        <button onClick={saveWorkout} disabled={saving}>{saving ? "Saving..." : "Save workout"}</button>
      </div>

      <div className="card">
        <h2>Create diet plan</h2>
        <input placeholder="Member email" value={d.memberEmail} onChange={(e) => setD({ ...d, memberEmail: e.target.value })} />
        <input placeholder="Title" value={d.title} onChange={(e) => setD({ ...d, title: e.target.value })} />
        <input placeholder="Meals" value={d.meals} onChange={(e) => setD({ ...d, meals: e.target.value })} />
        <input placeholder="Calories per day" type="number" value={d.caloriesPerDay} onChange={(e) => setD({ ...d, caloriesPerDay: e.target.value })} />
        <button onClick={saveDiet} disabled={saving}>{saving ? "Saving..." : "Save diet"}</button>
      </div>
    </>
  );
}