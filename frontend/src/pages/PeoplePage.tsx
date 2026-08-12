import { useCallback, useEffect, useMemo, useState } from "react";
import { Search } from "lucide-react";
import { peopleApi } from "../api";
import { EmptyState, ErrorState, LoadingState, PageHeader, StatusBadge } from "../components/UI";
import type { Person } from "../types";
import { errorMessage, formatNumber } from "../utils";

export function PeoplePage() {
  const [people, setPeople] = useState<Person[]>([]); const [search, setSearch] = useState("");
  const [loading, setLoading] = useState(true); const [error, setError] = useState("");
  const load = useCallback(async () => { setLoading(true); setError(""); try { setPeople(await peopleApi.list()); } catch (err) { setError(errorMessage(err, "No se pudieron cargar las personas.")); } finally { setLoading(false); } }, []);
  useEffect(() => { void load(); }, [load]);
  const filtered = useMemo(() => { const term = search.toLowerCase(); return people.filter((person) => `${person.codigoBiometrico} ${person.nombres} ${person.apellidos} ${person.cedula}`.toLowerCase().includes(term)); }, [people, search]);

  return <div className="page-stack"><PageHeader title="Personas" subtitle={`${formatNumber(people.length)} identidades asociadas al control biométrico`} actions={<div className="search-box"><Search size={17} /><input value={search} onChange={(e) => setSearch(e.target.value)} placeholder="Buscar persona…" /></div>} />{loading ? <LoadingState /> : error ? <ErrorState message={error} onRetry={load} /> : !filtered.length ? <EmptyState message="No se encontraron personas." /> : <article className="panel"><div className="table-scroll"><table><thead><tr><th>Código biométrico</th><th>Nombre</th><th>Cédula</th><th>Departamento</th><th>Cargo</th><th>Estado</th></tr></thead><tbody>{filtered.map((person) => <tr key={person.id}><td><strong>{person.codigoBiometrico}</strong></td><td>{person.nombres} {person.apellidos}</td><td>{person.cedula}</td><td>{person.departamento || "—"}</td><td>{person.cargo || "—"}</td><td><StatusBadge value={person.activo} /></td></tr>)}</tbody></table></div></article>}</div>;
}
