from fastapi import FastAPI, File, HTTPException, UploadFile, status

from app.config import MAX_FILE_SIZE_BYTES, MAX_PAGES
from app.models import HealthResponse, JobAnalysisRequest, JobAnalysisResponse, ResumeAnalysisResponse
from app.services.pdf_reader import InvalidPdfError, extract_pdf_text
from app.services.skill_extractor import classify_job_skills, extract_skills, suggest_role


app = FastAPI(
    title="MatchDev Resume Parser",
    version="1.0.0",
    description="Extrai habilidades técnicas de currículos em PDF sem armazenar o arquivo.",
)


@app.get("/health", response_model=HealthResponse, tags=["Health"])
def health() -> HealthResponse:
    return HealthResponse(status="UP", service="matchdev-resume-parser")


@app.post(
    "/api/v1/jobs/analyze",
    response_model=JobAnalysisResponse,
    response_model_by_alias=True,
    tags=["Vagas"],
)
def analyze_job(request: JobAnalysisRequest) -> JobAnalysisResponse:
    required_skills, desirable_skills = classify_job_skills(request.description)
    all_skills = [*required_skills, *desirable_skills]
    warnings: list[str] = []
    if not all_skills:
        warnings.append("Nenhuma habilidade do catálogo atual foi identificada na vaga.")

    return JobAnalysisResponse(
        characterCount=len(request.description),
        requiredSkills=required_skills,
        desirableSkills=desirable_skills,
        suggestedRole=suggest_role(all_skills),
        warnings=warnings,
    )


@app.post(
    "/api/v1/resumes/analyze",
    response_model=ResumeAnalysisResponse,
    response_model_by_alias=True,
    tags=["Currículos"],
)
async def analyze_resume(file: UploadFile = File(...)) -> ResumeAnalysisResponse:
    file_name = file.filename or "curriculo.pdf"
    if not file_name.lower().endswith(".pdf"):
        raise HTTPException(
            status_code=status.HTTP_415_UNSUPPORTED_MEDIA_TYPE,
            detail="Envie um currículo no formato PDF",
        )

    content = await file.read(MAX_FILE_SIZE_BYTES + 1)
    await file.close()

    if not content:
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="O arquivo está vazio")
    if len(content) > MAX_FILE_SIZE_BYTES:
        max_size_mb = MAX_FILE_SIZE_BYTES // (1024 * 1024)
        raise HTTPException(
            status_code=status.HTTP_413_CONTENT_TOO_LARGE,
            detail=f"O PDF deve ter no máximo {max_size_mb} MB",
        )

    try:
        pdf_result = extract_pdf_text(content, MAX_PAGES)
    except InvalidPdfError as exc:
        raise HTTPException(status_code=status.HTTP_422_UNPROCESSABLE_CONTENT, detail=str(exc)) from exc

    skills = extract_skills(pdf_result.text)
    warnings = list(pdf_result.warnings)
    if not skills:
        warnings.append("Nenhuma habilidade do catálogo atual foi identificada.")

    return ResumeAnalysisResponse(
        fileName=file_name,
        pageCount=pdf_result.page_count,
        analyzedPageCount=pdf_result.analyzed_page_count,
        characterCount=len(pdf_result.text),
        skills=skills,
        suggestedRole=suggest_role(skills),
        warnings=warnings,
    )
