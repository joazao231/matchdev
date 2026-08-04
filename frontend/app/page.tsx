"use client";

import { FormEvent, useEffect, useMemo, useState } from "react";

type View = "overview" | "resume" | "profile" | "matches";
type AuthMode = "login" | "register";

type Profile = {
  fullName: string;
  email: string;
  headline: string | null;
  desiredRole: string | null;
  location: string | null;
  desiredSeniority: string | null;
  skills: string[];
  preferredWorkModels: string[];
};

type Match = {
  id: string;
  jobId: string;
  jobTitle: string;
  company: string;
  location: string;
  workModel: string;
  sourceUrl: string;
  score: number;
  recommendation: string;
  matchedSkills: string[];
  missingRequiredSkills: string[];
  missingDesirableSkills: string[];
  explanation: string;
};

type ResumeResult = {
  analysis: {
    fileName: string;
    pageCount: number;
    skills: string[];
    suggestedRole: string;
    warnings: string[];
  };
  profile: Profile;
  newSkillsImported: number;
};

const DEMO_PROFILE: Profile = {
  fullName: "João Antonio",
  email: "joao@matchdev.dev",
  headline: "Desenvolvedor Backend Java e Python",
  desiredRole: "Desenvolvedor Backend",
  location: "Umuarama, PR",
  desiredSeniority: "JUNIOR",
  skills: ["java", "spring boot", "python", "fastapi", "postgresql", "docker", "git"],
  preferredWorkModels: ["REMOTE", "HYBRID"],
};

const DEMO_MATCHES: Match[] = [
  {
    id: "demo-1",
    jobId: "job-1",
    jobTitle: "Desenvolvedor Backend Java Júnior",
    company: "Nexa Tecnologia",
    location: "Curitiba, PR",
    workModel: "REMOTE",
    sourceUrl: "#",
    score: 92,
    recommendation: "EXCELLENT",
    matchedSkills: ["java", "spring boot", "postgresql", "git"],
    missingRequiredSkills: [],
    missingDesirableSkills: ["kafka"],
    explanation: "Seu perfil atende todos os requisitos obrigatórios e combina com a senioridade da vaga.",
  },
  {
    id: "demo-2",
    jobId: "job-2",
    jobTitle: "Backend Developer Python",
    company: "Orbit Sistemas",
    location: "São Paulo, SP",
    workModel: "HYBRID",
    sourceUrl: "#",
    score: 78,
    recommendation: "GOOD",
    matchedSkills: ["python", "fastapi", "postgresql", "docker"],
    missingRequiredSkills: ["aws"],
    missingDesirableSkills: ["redis"],
    explanation: "Boa aderência técnica. Conhecimentos em AWS aumentariam sua competitividade.",
  },
  {
    id: "demo-3",
    jobId: "job-3",
    jobTitle: "Analista de Desenvolvimento Júnior",
    company: "Vale Digital",
    location: "Maringá, PR",
    workModel: "ONSITE",
    sourceUrl: "#",
    score: 64,
    recommendation: "POSSIBLE",
    matchedSkills: ["java", "sql", "git"],
    missingRequiredSkills: ["angular"],
    missingDesirableSkills: ["azure"],
    explanation: "A vaga é possível, mas pede uma tecnologia frontend que ainda não aparece no perfil.",
  },
];

const API_DEFAULT = "http://localhost:8080";

function friendlyError(value: unknown) {
  if (value instanceof Error && value.message) return value.message;
  return "Não foi possível concluir a operação. Verifique se a API está ligada.";
}

function recommendationLabel(value: string) {
  return {
    EXCELLENT: "Excelente match",
    GOOD: "Bom match",
    POSSIBLE: "Match possível",
    LOW: "Baixa aderência",
  }[value] ?? value;
}

function workModelLabel(value: string) {
  return { REMOTE: "Remoto", HYBRID: "Híbrido", ONSITE: "Presencial" }[value] ?? value;
}

function initials(name: string) {
  return name
    .split(" ")
    .filter(Boolean)
    .slice(0, 2)
    .map((part) => part[0])
    .join("")
    .toUpperCase();
}

export default function Home() {
  const [sessionReady, setSessionReady] = useState(false);
  const [authMode, setAuthMode] = useState<AuthMode>("login");
  const [view, setView] = useState<View>("overview");
  const [token, setToken] = useState("");
  const [demoMode, setDemoMode] = useState(false);
  const [apiBase, setApiBase] = useState(API_DEFAULT);
  const [profile, setProfile] = useState<Profile | null>(null);
  const [matches, setMatches] = useState<Match[]>([]);
  const [busy, setBusy] = useState(false);
  const [notice, setNotice] = useState("");
  const [error, setError] = useState("");
  const [resumeFile, setResumeFile] = useState<File | null>(null);
  const [resumeResult, setResumeResult] = useState<ResumeResult | null>(null);
  const [dragging, setDragging] = useState(false);
  const [authForm, setAuthForm] = useState({ fullName: "", email: "", password: "" });
  const [profileForm, setProfileForm] = useState({
    headline: "",
    desiredRole: "",
    location: "",
    desiredSeniority: "JUNIOR",
    skills: "",
    preferredWorkModels: ["REMOTE"] as string[],
  });

  const isAuthenticated = demoMode || Boolean(token);

  useEffect(() => {
    const savedToken = window.localStorage.getItem("matchdev_token") ?? "";
    const savedApi = window.localStorage.getItem("matchdev_api") ?? API_DEFAULT;
    queueMicrotask(() => {
      setApiBase(savedApi);
      setToken(savedToken);
      setSessionReady(true);
    });
  }, []);

  useEffect(() => {
    if (!profile) return;
    queueMicrotask(() => {
      setProfileForm({
        headline: profile.headline ?? "",
        desiredRole: profile.desiredRole ?? "",
        location: profile.location ?? "",
        desiredSeniority: profile.desiredSeniority ?? "JUNIOR",
        skills: profile.skills.join(", "),
        preferredWorkModels: profile.preferredWorkModels.length
          ? profile.preferredWorkModels
          : ["REMOTE"],
      });
    });
  }, [profile]);

  const matchAverage = useMemo(() => {
    if (!matches.length) return 0;
    return Math.round(matches.reduce((total, item) => total + item.score, 0) / matches.length);
  }, [matches]);

  async function apiRequest<T>(path: string, options: RequestInit = {}): Promise<T> {
    const isForm = options.body instanceof FormData;
    const response = await fetch(`${apiBase.replace(/\/$/, "")}${path}`, {
      ...options,
      headers: {
        ...(isForm ? {} : { "Content-Type": "application/json" }),
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
        ...(options.headers ?? {}),
      },
    });

    const raw = await response.text();
    let payload: unknown = null;
    if (raw) {
      try {
        payload = JSON.parse(raw);
      } catch {
        payload = raw;
      }
    }
    if (!response.ok) {
      const body = payload as { message?: string; detail?: string } | null;
      throw new Error(body?.message ?? body?.detail ?? `A API respondeu com status ${response.status}.`);
    }
    return payload as T;
  }

  async function loadWorkspace() {
    setBusy(true);
    setError("");
    try {
      const [currentProfile, currentMatches] = await Promise.all([
        apiRequest<Profile>("/api/v1/profile"),
        apiRequest<Match[]>("/api/v1/matches"),
      ]);
      setProfile(currentProfile);
      setMatches(currentMatches);
    } catch (requestError) {
      setError(friendlyError(requestError));
    } finally {
      setBusy(false);
    }
  }

  useEffect(() => {
    if (sessionReady && token && !demoMode) queueMicrotask(() => void loadWorkspace());
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [sessionReady, token]);

  async function submitAuth(event: FormEvent) {
    event.preventDefault();
    setBusy(true);
    setError("");
    try {
      window.localStorage.setItem("matchdev_api", apiBase);
      const endpoint = authMode === "login" ? "/api/v1/auth/login" : "/api/v1/auth/register";
      const body =
        authMode === "login"
          ? { email: authForm.email, password: authForm.password }
          : authForm;
      const response = await apiRequest<{ accessToken: string }>(endpoint, {
        method: "POST",
        body: JSON.stringify(body),
      });
      window.localStorage.setItem("matchdev_token", response.accessToken);
      setToken(response.accessToken);
      setNotice(authMode === "login" ? "Login realizado com sucesso." : "Conta criada. Bem-vindo ao MatchDev!");
    } catch (requestError) {
      setError(friendlyError(requestError));
    } finally {
      setBusy(false);
    }
  }

  function enterDemo() {
    setDemoMode(true);
    setProfile(DEMO_PROFILE);
    setMatches(DEMO_MATCHES);
    setNotice("Você está explorando a demonstração do MatchDev.");
  }

  function logout() {
    window.localStorage.removeItem("matchdev_token");
    setToken("");
    setDemoMode(false);
    setProfile(null);
    setMatches([]);
    setResumeResult(null);
    setView("overview");
    setNotice("");
    setError("");
  }

  async function uploadResume(event: FormEvent) {
    event.preventDefault();
    if (!resumeFile) {
      setError("Selecione um currículo em PDF.");
      return;
    }
    setBusy(true);
    setError("");
    setNotice("");
    try {
      if (demoMode) {
        await new Promise((resolve) => setTimeout(resolve, 700));
        const result: ResumeResult = {
          analysis: {
            fileName: resumeFile.name,
            pageCount: 2,
            skills: ["Java", "Spring Boot", "Python", "FastAPI", "PostgreSQL", "Docker", "Git"],
            suggestedRole: "Desenvolvedor Backend",
            warnings: [],
          },
          profile: DEMO_PROFILE,
          newSkillsImported: 7,
        };
        setResumeResult(result);
        setProfile(result.profile);
      } else {
        const formData = new FormData();
        formData.append("file", resumeFile);
        const result = await apiRequest<ResumeResult>("/api/v1/profile/resume", {
          method: "POST",
          body: formData,
        });
        setResumeResult(result);
        setProfile(result.profile);
      }
      setNotice("Currículo analisado e perfil atualizado.");
    } catch (requestError) {
      setError(friendlyError(requestError));
    } finally {
      setBusy(false);
    }
  }

  async function saveProfile(event: FormEvent) {
    event.preventDefault();
    setBusy(true);
    setError("");
    try {
      const payload = {
        ...profileForm,
        skills: profileForm.skills
          .split(",")
          .map((skill) => skill.trim())
          .filter(Boolean),
      };
      if (demoMode) {
        await new Promise((resolve) => setTimeout(resolve, 450));
        setProfile({
          ...(profile ?? DEMO_PROFILE),
          ...payload,
        });
      } else {
        setProfile(
          await apiRequest<Profile>("/api/v1/profile", {
            method: "PUT",
            body: JSON.stringify(payload),
          }),
        );
      }
      setNotice("Perfil profissional salvo com sucesso.");
      setView("overview");
    } catch (requestError) {
      setError(friendlyError(requestError));
    } finally {
      setBusy(false);
    }
  }

  async function refreshMatches() {
    setBusy(true);
    setError("");
    try {
      if (demoMode) {
        await new Promise((resolve) => setTimeout(resolve, 600));
        setMatches(DEMO_MATCHES);
      } else {
        setMatches(await apiRequest<Match[]>("/api/v1/matches/refresh", { method: "POST" }));
      }
      setNotice("Ranking recalculado com seu perfil atual.");
    } catch (requestError) {
      setError(friendlyError(requestError));
    } finally {
      setBusy(false);
    }
  }

  if (!sessionReady) {
    return <main className="splash"><div className="brand-mark">M</div></main>;
  }

  if (!isAuthenticated) {
    return (
      <main className="auth-shell">
        <section className="auth-story">
          <div className="brand brand-large"><span className="brand-mark">M</span><span>MatchDev</span></div>
          <div className="story-copy">
            <span className="eyebrow">CARREIRA MOVIDA POR DADOS</span>
            <h1>Descubra onde o seu perfil realmente <em>combina.</em></h1>
            <p>Transforme seu currículo em um ranking claro de oportunidades e concentre energia nas vagas com maior potencial.</p>
          </div>
          <div className="story-proof">
            <div><strong>01</strong><span>Envie seu currículo</span></div>
            <div><strong>02</strong><span>Mapeie suas habilidades</span></div>
            <div><strong>03</strong><span>Encontre seus melhores matches</span></div>
          </div>
          <p className="story-note">Projeto de portfólio • João Antonio</p>
        </section>

        <section className="auth-panel">
          <div className="auth-card">
            <div className="mobile-brand brand"><span className="brand-mark">M</span><span>MatchDev</span></div>
            <span className="eyebrow dark">SUA ÁREA PESSOAL</span>
            <h2>{authMode === "login" ? "Bem-vindo de volta" : "Crie seu perfil"}</h2>
            <p className="auth-intro">
              {authMode === "login"
                ? "Entre para acompanhar seus melhores matches."
                : "Comece importando suas habilidades em poucos minutos."}
            </p>

            {error && <div className="alert error-alert" role="alert">{error}</div>}

            <form onSubmit={submitAuth} className="auth-form">
              {authMode === "register" && (
                <label>
                  Nome completo
                  <input
                    required
                    value={authForm.fullName}
                    onChange={(event) => setAuthForm({ ...authForm, fullName: event.target.value })}
                    placeholder="João Antonio"
                    autoComplete="name"
                  />
                </label>
              )}
              <label>
                E-mail
                <input
                  required
                  type="email"
                  value={authForm.email}
                  onChange={(event) => setAuthForm({ ...authForm, email: event.target.value })}
                  placeholder="voce@email.com"
                  autoComplete="email"
                />
              </label>
              <label>
                Senha
                <input
                  required
                  minLength={8}
                  type="password"
                  value={authForm.password}
                  onChange={(event) => setAuthForm({ ...authForm, password: event.target.value })}
                  placeholder="Mínimo de 8 caracteres"
                  autoComplete={authMode === "login" ? "current-password" : "new-password"}
                />
              </label>
              <details className="api-settings">
                <summary>Configuração da API</summary>
                <label>
                  Endereço
                  <input
                    value={apiBase}
                    onChange={(event) => setApiBase(event.target.value)}
                    placeholder={API_DEFAULT}
                  />
                </label>
              </details>
              <button className="primary-button" type="submit" disabled={busy}>
                {busy ? "Conectando..." : authMode === "login" ? "Entrar no MatchDev" : "Criar minha conta"}
              </button>
            </form>

            <button className="demo-button" type="button" onClick={enterDemo}>Explorar demonstração</button>
            <p className="auth-switch">
              {authMode === "login" ? "Ainda não possui conta?" : "Já possui uma conta?"}{" "}
              <button type="button" onClick={() => { setAuthMode(authMode === "login" ? "register" : "login"); setError(""); }}>
                {authMode === "login" ? "Criar agora" : "Fazer login"}
              </button>
            </p>
          </div>
        </section>
      </main>
    );
  }

  const firstName = profile?.fullName?.split(" ")[0] ?? "Dev";

  return (
    <main className="app-shell">
      <aside className="sidebar">
        <div className="brand"><span className="brand-mark">M</span><span>MatchDev</span></div>
        <nav aria-label="Navegação principal">
          <button className={view === "overview" ? "active" : ""} onClick={() => setView("overview")}><span>01</span> Visão geral</button>
          <button className={view === "resume" ? "active" : ""} onClick={() => setView("resume")}><span>02</span> Meu currículo</button>
          <button className={view === "profile" ? "active" : ""} onClick={() => setView("profile")}><span>03</span> Meu perfil</button>
          <button className={view === "matches" ? "active" : ""} onClick={() => setView("matches")}><span>04</span> Ranking de vagas</button>
        </nav>
        <div className="sidebar-footer">
          <div className="connection"><span className={demoMode ? "demo-dot" : "online-dot"} />{demoMode ? "Modo demonstração" : "API conectada"}</div>
          <button onClick={logout}>Sair da conta</button>
        </div>
      </aside>

      <section className="workspace">
        <header className="topbar">
          <div>
            <span className="eyebrow dark">MATCHDEV WORKSPACE</span>
            <h1>Olá, {firstName}.</h1>
          </div>
          <div className="profile-chip">
            <span className="avatar">{initials(profile?.fullName ?? "Dev")}</span>
            <div><strong>{profile?.fullName ?? "Seu perfil"}</strong><small>{profile?.desiredRole ?? "Complete seu perfil"}</small></div>
          </div>
        </header>

        <div className="mobile-navigation">
          <button className={view === "overview" ? "active" : ""} onClick={() => setView("overview")}>Início</button>
          <button className={view === "resume" ? "active" : ""} onClick={() => setView("resume")}>Currículo</button>
          <button className={view === "profile" ? "active" : ""} onClick={() => setView("profile")}>Perfil</button>
          <button className={view === "matches" ? "active" : ""} onClick={() => setView("matches")}>Vagas</button>
        </div>

        {(notice || error) && (
          <div className={`alert ${error ? "error-alert" : "success-alert"}`} role="status">
            <span>{error ? "!" : "✓"}</span>{error || notice}
            <button onClick={() => { setNotice(""); setError(""); }} aria-label="Fechar aviso">×</button>
          </div>
        )}

        {view === "overview" && (
          <div className="page-content">
            <section className="hero-card">
              <div className="hero-copy">
                <span className="eyebrow">SEU PRÓXIMO PASSO</span>
                <h2>Seu melhor match começa com um perfil completo.</h2>
                <p>Importe seu currículo e deixe o MatchDev transformar suas experiências em oportunidades mais relevantes.</p>
                <button className="lime-button" onClick={() => setView("resume")}>Analisar meu currículo <span>→</span></button>
              </div>
              <div className="match-orbit" aria-label={`Média de compatibilidade: ${matchAverage}%`}>
                <div className="orbit-ring" style={{ "--score": `${matchAverage * 3.6}deg` } as React.CSSProperties}>
                  <div><strong>{matchAverage || "—"}<small>{matchAverage ? "%" : ""}</small></strong><span>média dos matches</span></div>
                </div>
                <span className="orbit-label">PERFIL EM MOVIMENTO</span>
              </div>
            </section>

            <section className="stats-grid">
              <article><span>HABILIDADES MAPEADAS</span><strong>{profile?.skills.length ?? 0}</strong><small>tecnologias no seu perfil</small></article>
              <article><span>VAGAS ANALISADAS</span><strong>{matches.length}</strong><small>ordenadas por compatibilidade</small></article>
              <article><span>MELHOR OPORTUNIDADE</span><strong>{matches[0]?.score ?? 0}<small>%</small></strong><small>{matches[0]?.company ?? "Atualize o ranking"}</small></article>
            </section>

            <section className="section-block">
              <div className="section-heading"><div><span className="eyebrow dark">PRIORIDADE</span><h2>Melhores oportunidades</h2></div><button className="text-button" onClick={() => setView("matches")}>Ver ranking completo →</button></div>
              {matches.length ? (
                <div className="compact-matches">
                  {matches.slice(0, 3).map((match, index) => (
                    <article key={match.id}>
                      <span className="rank">0{index + 1}</span>
                      <div className="match-title"><strong>{match.jobTitle}</strong><span>{match.company} • {match.location}</span></div>
                      <span className="work-badge">{workModelLabel(match.workModel)}</span>
                      <div className={`score score-${Math.floor(match.score / 20)}`}><strong>{match.score}</strong><span>/100</span></div>
                    </article>
                  ))}
                </div>
              ) : (
                <div className="empty-state"><strong>Ainda não há matches calculados.</strong><p>Complete seu perfil e atualize o ranking para começar.</p><button className="secondary-button" onClick={() => setView("profile")}>Completar perfil</button></div>
              )}
            </section>
          </div>
        )}

        {view === "resume" && (
          <div className="page-content narrow-page">
            <div className="page-heading"><span className="eyebrow dark">LEITURA INTELIGENTE</span><h2>Transforme seu currículo em dados úteis.</h2><p>O arquivo é analisado em memória e não fica armazenado no MatchDev.</p></div>
            <form className="upload-card" onSubmit={uploadResume}>
              <label
                className={`drop-zone ${dragging ? "dragging" : ""}`}
                onDragOver={(event) => { event.preventDefault(); setDragging(true); }}
                onDragLeave={() => setDragging(false)}
                onDrop={(event) => { event.preventDefault(); setDragging(false); setResumeFile(event.dataTransfer.files[0] ?? null); }}
              >
                <input type="file" accept="application/pdf,.pdf" onChange={(event) => setResumeFile(event.target.files?.[0] ?? null)} />
                <span className="file-symbol">PDF</span>
                <strong>{resumeFile ? resumeFile.name : "Arraste seu currículo ou escolha um arquivo"}</strong>
                <small>{resumeFile ? `${(resumeFile.size / 1024 / 1024).toFixed(2)} MB • pronto para análise` : "PDF com até 5 MB"}</small>
                <span className="choose-file">Escolher arquivo</span>
              </label>
              <button className="primary-button" type="submit" disabled={busy || !resumeFile}>{busy ? "Analisando currículo..." : "Analisar e importar habilidades"}</button>
            </form>

            {resumeResult && (
              <section className="result-card">
                <div className="result-heading"><span className="result-check">✓</span><div><span className="eyebrow dark">ANÁLISE CONCLUÍDA</span><h3>{resumeResult.newSkillsImported} habilidades novas importadas</h3></div></div>
                <div className="result-meta"><div><span>Arquivo</span><strong>{resumeResult.analysis.fileName}</strong></div><div><span>Área sugerida</span><strong>{resumeResult.analysis.suggestedRole}</strong></div><div><span>Páginas</span><strong>{resumeResult.analysis.pageCount}</strong></div></div>
                <div className="skills-cloud">{resumeResult.analysis.skills.map((skill) => <span key={skill}>{skill}</span>)}</div>
                <button className="secondary-button" onClick={() => setView("profile")} type="button">Revisar meu perfil →</button>
              </section>
            )}
          </div>
        )}

        {view === "profile" && (
          <div className="page-content narrow-page">
            <div className="page-heading"><span className="eyebrow dark">POSICIONAMENTO PROFISSIONAL</span><h2>Conte ao MatchDev o que você procura.</h2><p>Essas informações definem como cada oportunidade será pontuada.</p></div>
            <form className="profile-form" onSubmit={saveProfile}>
              <div className="form-grid">
                <label className="full-field">Título profissional<input required value={profileForm.headline} onChange={(event) => setProfileForm({ ...profileForm, headline: event.target.value })} placeholder="Ex.: Desenvolvedor Backend Java e Python" /></label>
                <label>Cargo desejado<input required value={profileForm.desiredRole} onChange={(event) => setProfileForm({ ...profileForm, desiredRole: event.target.value })} placeholder="Desenvolvedor Backend" /></label>
                <label>Localização<input required value={profileForm.location} onChange={(event) => setProfileForm({ ...profileForm, location: event.target.value })} placeholder="Umuarama, PR" /></label>
                <label>Senioridade<select value={profileForm.desiredSeniority} onChange={(event) => setProfileForm({ ...profileForm, desiredSeniority: event.target.value })}><option value="INTERN">Estágio</option><option value="JUNIOR">Júnior</option><option value="MID">Pleno</option><option value="SENIOR">Sênior</option></select></label>
                <fieldset><legend>Modelo de trabalho</legend><div className="choice-row">{[["REMOTE", "Remoto"], ["HYBRID", "Híbrido"], ["ONSITE", "Presencial"]].map(([value, label]) => <label className="check-choice" key={value}><input type="checkbox" checked={profileForm.preferredWorkModels.includes(value)} onChange={() => setProfileForm({ ...profileForm, preferredWorkModels: profileForm.preferredWorkModels.includes(value) ? profileForm.preferredWorkModels.filter((item) => item !== value) : [...profileForm.preferredWorkModels, value] })} /><span>{label}</span></label>)}</div></fieldset>
                <label className="full-field">Habilidades <small>Separe as tecnologias por vírgulas.</small><textarea required value={profileForm.skills} onChange={(event) => setProfileForm({ ...profileForm, skills: event.target.value })} rows={5} placeholder="java, spring boot, postgresql, docker" /></label>
              </div>
              <div className="form-actions"><button className="primary-button" type="submit" disabled={busy}>{busy ? "Salvando..." : "Salvar perfil profissional"}</button></div>
            </form>
          </div>
        )}

        {view === "matches" && (
          <div className="page-content">
            <div className="page-heading row-heading"><div><span className="eyebrow dark">OPORTUNIDADES PRIORIZADAS</span><h2>Ranking de vagas</h2><p>Entenda onde você já se destaca e o que ainda precisa desenvolver.</p></div><button className="primary-button compact-button" onClick={refreshMatches} disabled={busy}>{busy ? "Recalculando..." : "Atualizar ranking"}</button></div>
            <div className="match-list">
              {matches.map((match, index) => (
                <article className="match-card" key={match.id}>
                  <div className="match-rank">#{String(index + 1).padStart(2, "0")}</div>
                  <div className="match-main">
                    <div className="match-card-heading"><div><span className="company-label">{match.company}</span><h3>{match.jobTitle}</h3><p>{match.location} • {workModelLabel(match.workModel)}</p></div><div className="large-score"><strong>{match.score}</strong><span>/100</span></div></div>
                    <div className="recommendation-line"><span className={`recommendation ${match.recommendation.toLowerCase()}`}>{recommendationLabel(match.recommendation)}</span><p>{match.explanation}</p></div>
                    <div className="skill-columns"><div><span>Você já atende</span><div className="skills-cloud matched">{match.matchedSkills.map((skill) => <span key={skill}>✓ {skill}</span>)}</div></div><div><span>Vale desenvolver</span><div className="skills-cloud missing">{[...match.missingRequiredSkills, ...match.missingDesirableSkills].map((skill) => <span key={skill}>+ {skill}</span>)}</div></div></div>
                    {match.sourceUrl && match.sourceUrl !== "#" && <a className="job-link" href={match.sourceUrl} target="_blank" rel="noreferrer">Abrir vaga original →</a>}
                  </div>
                </article>
              ))}
              {!matches.length && <div className="empty-state"><strong>Nenhuma vaga analisada ainda.</strong><p>Atualize o ranking para comparar seu perfil com as oportunidades disponíveis.</p><button className="primary-button compact-button" onClick={refreshMatches}>Calcular meus matches</button></div>}
            </div>
          </div>
        )}

        {busy && !profile && <div className="loading-layer"><span /><p>Preparando seu workspace...</p></div>}
      </section>
    </main>
  );
}
