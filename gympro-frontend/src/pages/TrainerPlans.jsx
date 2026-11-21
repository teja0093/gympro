import { useState } from "react";
import api from "../api.js";

export default function TrainerPlans() {
  const [email, setEmail] = useState("");
  const [loadedFor, setLoadedFor] = useState("");
  const [workouts, setWorkouts] = useState([]);
  const [diets, setDiets] = useState([]);
  const [editW, setEditW] = useState(null);
  const [editD, setEditD] = useState(null);
  const [msg, setMsg] = useState("");
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);

  const show = (e) => {
    const data = e.response?.data;
    setError(data?.fieldErrors ? Object.values(data.fieldErrors).join(", ") : data?.message || "Failed");
  };

  const load = async (mail = email) => {
    setError("");
    try {
      const [w, d] = await Promise.all([
        api.get("/workouts/member", { params: { email: mail } }),
        api.get("/workouts/diets/member", { params: { email: mail } }),
      ]);
      setWorkouts(w.data);
      setDiets(d.data);
      setLoadedFor(mail);
    } catch (e) { show(e); }
  };

  const run = async (action, okMsg) => {
    if (busy) return;
    setBusy(true); setError(""); setMsg("");
    try {
      await action();
      setMsg(okMsg);
      setEditW(null);
      setEditD(null);
      await load(loadedFor || email);
    } catch (e) { show(e); }
    finally { setBusy(false); }
  };

  const saveWorkout = () =>
    run(() => api.put(`/workouts/plans/${editW.id}`, {
      memberEmail: editW.memberEmail,
      title: editW.title,
      goal: editW.goal,
      exercises: editW.exercises
        .filter((x) => x.name)
        .map((x) => ({ name: x.name, sets: Number(x.sets), reps: Number(x.reps) })),
    }), "Workout plan updated");

  const deleteWorkout = (id) => {
    if (window.confirm("Delete this workout plan?"))
      run(() => api.delete(`/workouts/plans/${id}`), "Workout plan deleted");
  };

  const saveDiet = () =>
    run(() => api.put(`/workouts/diets/${editD.id}`, {
      memberEmail: editD.memberEmail,
      title: editD.title,
      meals: editD.meals,
      caloriesPerDay: Number(editD.caloriesPerDay),
    }), "Diet plan updated");

  const deleteDiet = (id) => {
    if (window.confirm("Delete this diet plan?"))
      run(() => api.delete(`/workouts/diets/${id}`), "Diet plan deleted");
  };

  const updateEx = (i, field, value) => {
    const copy = [...editW.exercises];
    copy[i] = { ...copy[i], [field]: value };
    setEditW({ ...editW, exercises: copy });
  };

  return (
    <>
      <div className="card">
        <h2>Edit or delete plans</h2>
        <input placeholder="Member email" value={email} onChange={(e) => setEmail(e.target.value)} />
        <button onClick={() => load()}>Load plans</button>
        <p className="success">{msg}</p>
        <p className="error">{error}</p>
      </div>

      {loadedFor && <h3>Workout plans for {loadedFor}</h3>}
      {loadedFor && workouts.length === 0 && <p>No workout plans.</p>}
      {workouts.map((p) =>
        editW?.id === p.id ? (
          <div className="card" key={p.id}>
            <input value={editW.title} onChange={(e) => setEditW({ ...editW, title: e.target.value })} />
            <input value={editW.goal || ""} placeholder="Goal" onChange={(e) => setEditW({ ...editW, goal: e.target.value })} />
            {editW.exercises.map((x, i) => (
              <div key={i} style={{ display: "flex", gap: 8 }}>
                <input value={x.name} placeholder="Exercise" onChange={(e) => updateEx(i, "name", e.target.value)} />
                <input type="number" value={x.sets} placeholder="Sets" onChange={(e) => updateEx(i, "sets", e.target.value)} />
                <input type="number" value={x.reps} placeholder="Reps" onChange={(e) => updateEx(i, "reps", e.target.value)} />
              </div>
            ))}
            <button onClick={() => setEditW({ ...editW, exercises: [...editW.exercises, { name: "", sets: "", reps: "" }] })}>+ Add exercise</button>{" "}
            <button onClick={saveWorkout} disabled={busy}>Save</button>{" "}
            <button onClick={() => setEditW(null)}>Cancel</button>
          </div>
        ) : (
          <div className="card" key={p.id}>
            <h4>{p.title}</h4>
            <p>Goal: {p.goal}</p>
            <ul>{p.exercises?.map((e) => <li key={e.id}>{e.name}: {e.sets} x {e.reps}</li>)}</ul>
            <button onClick={() => setEditW({ ...p, exercises: p.exercises.map((x) => ({ ...x })) })}>Edit</button>{" "}
            <button onClick={() => deleteWorkout(p.id)} disabled={busy}>Delete</button>
          </div>
        )
      )}

      {loadedFor && <h3>Diet plans for {loadedFor}</h3>}
      {loadedFor && diets.length === 0 && <p>No diet plans.</p>}
      {diets.map((p) =>
        editD?.id === p.id ? (
          <div className="card" key={p.id}>
            <input value={editD.title} onChange={(e) => setEditD({ ...editD, title: e.target.value })} />
            <input value={editD.meals || ""} placeholder="Meals" onChange={(e) => setEditD({ ...editD, meals: e.target.value })} />
            <input type="number" value={editD.caloriesPerDay || ""} placeholder="Calories per day" onChange={(e) => setEditD({ ...editD, caloriesPerDay: e.target.value })} />
            <button onClick={saveDiet} disabled={busy}>Save</button>{" "}
            <button onClick={() => setEditD(null)}>Cancel</button>
          </div>
        ) : (
          <div className="card" key={p.id}>
            <h4>{p.title} ({p.caloriesPerDay} kcal/day)</h4>
            <p>{p.meals}</p>
            <button onClick={() => setEditD({ ...p })}>Edit</button>{" "}
            <button onClick={() => deleteDiet(p.id)} disabled={busy}>Delete</button>
          </div>
        )
      )}
    </>
  );
}