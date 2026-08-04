from app.services.skill_extractor import classify_job_skills, extract_skills, normalize_text, suggest_role


def test_extracts_backend_skills_from_portuguese_resume() -> None:
    text = """
    Desenvolvedor com projetos em Java, Spring Boot e Python.
    Criação de APIs REST com FastAPI, PostgreSQL, Docker, Git e Swagger.
    """

    skills = extract_skills(text)

    assert {
        "Java",
        "Spring Boot",
        "Python",
        "FastAPI",
        "APIs REST",
        "PostgreSQL",
        "Docker",
        "Git",
        "Swagger",
    }.issubset(set(skills))
    assert suggest_role(skills) == "Desenvolvedor Backend"


def test_does_not_confuse_java_with_javascript() -> None:
    skills = extract_skills("Experiência com JavaScript, TypeScript, React, HTML e CSS.")

    assert "JavaScript" in skills
    assert "Java" not in skills
    assert suggest_role(skills) == "Desenvolvedor Frontend"


def test_normalizes_accents() -> None:
    assert normalize_text("Microsserviços e automação") == "microsservicos e automacao"


def test_returns_generic_role_when_no_skill_is_found() -> None:
    assert suggest_role([]) == "Profissional de Tecnologia"


def test_classifies_required_and_desirable_job_skills() -> None:
    description = """
    Requisitos: Java, Spring Boot, PostgreSQL e APIs REST.
    Será um diferencial ter experiência com Docker, Kafka e AWS.
    """

    required, desirable = classify_job_skills(description)

    assert {"Java", "Spring Boot", "PostgreSQL", "APIs REST"}.issubset(required)
    assert {"Docker", "Kafka", "AWS"}.issubset(desirable)


def test_uses_all_detected_skills_as_required_without_explicit_requirements() -> None:
    required, desirable = classify_job_skills("Java, Python e Docker para desenvolvimento de sistemas.")

    assert {"Java", "Python", "Docker"}.issubset(required)
    assert desirable == []
