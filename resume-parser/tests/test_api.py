from fastapi.testclient import TestClient

import app.main as main_module
from app.services.pdf_reader import PdfTextResult


client = TestClient(main_module.app)


def test_analyzes_pdf_and_returns_camel_case_contract(monkeypatch) -> None:
    monkeypatch.setattr(
        main_module,
        "extract_pdf_text",
        lambda content, max_pages: PdfTextResult(
            text="Projetos com Java, Spring Boot, PostgreSQL, Docker e APIs REST.",
            page_count=2,
            analyzed_page_count=2,
            warnings=[],
        ),
    )

    response = client.post(
        "/api/v1/resumes/analyze",
        files={"file": ("curriculo.pdf", b"%PDF-fake-for-mocked-reader", "application/pdf")},
    )

    assert response.status_code == 200
    body = response.json()
    assert body["fileName"] == "curriculo.pdf"
    assert body["pageCount"] == 2
    assert body["suggestedRole"] == "Desenvolvedor Backend"
    assert {"Java", "Spring Boot", "PostgreSQL", "Docker", "APIs REST"}.issubset(body["skills"])


def test_rejects_non_pdf_file() -> None:
    response = client.post(
        "/api/v1/resumes/analyze",
        files={"file": ("curriculo.txt", b"Java e Spring Boot", "text/plain")},
    )

    assert response.status_code == 415
    assert response.json()["detail"] == "Envie um currículo no formato PDF"


def test_analyzes_job_description_and_classifies_skills() -> None:
    response = client.post(
        "/api/v1/jobs/analyze",
        json={
            "description": (
                "Buscamos pessoa com Java, Spring Boot, PostgreSQL e APIs REST. "
                "Docker e Kafka serão um diferencial para o time."
            )
        },
    )

    assert response.status_code == 200
    body = response.json()
    assert {"Java", "Spring Boot", "PostgreSQL", "APIs REST"}.issubset(body["requiredSkills"])
    assert {"Docker", "Kafka"}.issubset(body["desirableSkills"])
    assert body["suggestedRole"] == "Desenvolvedor Backend"
