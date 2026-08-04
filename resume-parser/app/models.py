from pydantic import BaseModel, Field


class ResumeAnalysisResponse(BaseModel):
    file_name: str = Field(alias="fileName")
    page_count: int = Field(alias="pageCount", ge=0)
    analyzed_page_count: int = Field(alias="analyzedPageCount", ge=0)
    character_count: int = Field(alias="characterCount", ge=0)
    skills: list[str]
    suggested_role: str = Field(alias="suggestedRole")
    warnings: list[str]

    model_config = {"populate_by_name": True}


class HealthResponse(BaseModel):
    status: str
    service: str


class JobAnalysisRequest(BaseModel):
    description: str = Field(min_length=30, max_length=20_000)


class JobAnalysisResponse(BaseModel):
    character_count: int = Field(alias="characterCount", ge=0)
    required_skills: list[str] = Field(alias="requiredSkills")
    desirable_skills: list[str] = Field(alias="desirableSkills")
    suggested_role: str = Field(alias="suggestedRole")
    warnings: list[str]

    model_config = {"populate_by_name": True}
