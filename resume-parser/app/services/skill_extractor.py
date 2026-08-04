import re
import unicodedata
from collections.abc import Iterable


SKILL_PATTERNS: dict[str, tuple[str, ...]] = {
    "Java": (r"\bjava\b",),
    "Spring Boot": (r"\bspring\s*boot\b", r"\bspringboot\b"),
    "Python": (r"\bpython\b",),
    "FastAPI": (r"\bfast\s*api\b", r"\bfastapi\b"),
    "JavaScript": (r"\bjavascript\b", r"\bjava\s*script\b", r"\bjs\b"),
    "TypeScript": (r"\btypescript\b", r"\btype\s*script\b"),
    "Node.js": (r"\bnode(?:\.js|js)?\b",),
    "React": (r"\breact(?:\.js|js)?\b",),
    "React Native": (r"\breact\s*native\b",),
    "HTML": (r"\bhtml5?\b",),
    "CSS": (r"\bcss3?\b",),
    "SQL": (r"\bsql\b",),
    "PostgreSQL": (r"\bpostgres(?:ql)?\b",),
    "MySQL": (r"\bmysql\b",),
    "SQLite": (r"\bsqlite\b",),
    "MongoDB": (r"\bmongodb\b", r"\bmongo\s*db\b"),
    "Redis": (r"\bredis\b",),
    "Docker": (r"\bdocker\b", r"\bcontainers?\b"),
    "Kubernetes": (r"\bkubernetes\b", r"\bk8s\b"),
    "AWS": (r"\baws\b", r"amazon\s+web\s+services"),
    "Azure": (r"\bazure\b",),
    "Google Cloud": (r"\bgcp\b", r"google\s+cloud"),
    "Git": (r"\bgit\b",),
    "GitHub": (r"\bgithub\b",),
    "GitHub Actions": (r"\bgithub\s+actions\b",),
    "Maven": (r"\bmaven\b",),
    "Gradle": (r"\bgradle\b",),
    "JUnit": (r"\bjunit\b",),
    "Pytest": (r"\bpytest\b",),
    "APIs REST": (r"\brest(?:ful)?\s+apis?\b", r"\bapis?\s+rest(?:ful)?\b"),
    "Microsserviços": (r"\bmicros{1,2}ervicos?\b", r"\bmicroservices?\b"),
    "Kafka": (r"\bkafka\b",),
    "RabbitMQ": (r"\brabbitmq\b", r"\brabbit\s*mq\b"),
    "Linux": (r"\blinux\b", r"\bubuntu\b"),
    "Arduino": (r"\barduino\b",),
    "Android Studio": (r"\bandroid\s+studio\b",),
    "C++": (r"(?<!\w)c\+\+(?!\w)", r"\bcplusplus\b"),
    "Postman": (r"\bpostman\b",),
    "Swagger": (r"\bswagger\b", r"\bopenapi\b"),
    "Angular": (r"\bangular(?:\.js|js)?\b",),
    "Vue.js": (r"\bvue(?:\.js|js)?\b",),
    "Next.js": (r"\bnext(?:\.js|js)?\b",),
    "C# / .NET": (r"(?<!\w)c#(?!\w)", r"\bcsharp\b", r"\b\.net\b", r"\bdotnet\b"),
    "PHP": (r"\bphp\b",),
    "Laravel": (r"\blaravel\b",),
    "Kotlin": (r"\bkotlin\b",),
    "Flutter": (r"\bflutter\b",),
    "Terraform": (r"\bterraform\b",),
    "Jenkins": (r"\bjenkins\b",),
    "CI/CD": (r"\bci\s*/?\s*cd\b", r"integracao\s+continua", r"entrega\s+continua"),
}


ROLE_GROUPS: tuple[tuple[str, set[str]], ...] = (
    (
        "Desenvolvedor Backend",
        {
            "Java",
            "Spring Boot",
            "Python",
            "FastAPI",
            "Node.js",
            "SQL",
            "PostgreSQL",
            "MySQL",
            "SQLite",
            "MongoDB",
            "Redis",
            "APIs REST",
            "Microsserviços",
            "Kafka",
            "RabbitMQ",
        },
    ),
    (
        "Desenvolvedor Frontend",
        {"JavaScript", "TypeScript", "React", "Angular", "Vue.js", "Next.js", "HTML", "CSS"},
    ),
    (
        "Desenvolvedor Mobile",
        {"React Native", "Android Studio", "Java", "Kotlin", "Flutter", "TypeScript"},
    ),
    (
        "DevOps / Cloud",
        {
            "Docker",
            "Kubernetes",
            "AWS",
            "Azure",
            "Google Cloud",
            "Linux",
            "GitHub Actions",
            "Terraform",
            "Jenkins",
            "CI/CD",
        },
    ),
)


def normalize_text(text: str) -> str:
    decomposed = unicodedata.normalize("NFD", text)
    without_accents = "".join(char for char in decomposed if unicodedata.category(char) != "Mn")
    return without_accents.lower()


def extract_skills(text: str) -> list[str]:
    normalized = normalize_text(text)
    detected = [
        skill
        for skill, patterns in SKILL_PATTERNS.items()
        if any(re.search(pattern, normalized, flags=re.IGNORECASE) for pattern in patterns)
    ]
    return sorted(detected, key=str.casefold)


def suggest_role(skills: Iterable[str]) -> str:
    skill_set = set(skills)
    scores = [(role, len(skill_set.intersection(group_skills))) for role, group_skills in ROLE_GROUPS]
    role, score = max(scores, key=lambda item: item[1])
    return role if score > 0 else "Profissional de Tecnologia"


DESIRABLE_MARKERS: tuple[str, ...] = (
    "desejavel",
    "diferencial",
    "sera um plus",
    "nice to have",
    "preferred",
    "opcional",
)


def classify_job_skills(description: str) -> tuple[list[str], list[str]]:
    all_skills = extract_skills(description)
    if not all_skills:
        return [], []

    blocks = [block.strip() for block in re.split(r"[\n\r]+|(?<=[.!?;])\s+", description) if block.strip()]
    desirable_text = " ".join(
        block for block in blocks if any(marker in normalize_text(block) for marker in DESIRABLE_MARKERS)
    )
    desirable = extract_skills(desirable_text)
    desirable_set = set(desirable)
    required = [skill for skill in all_skills if skill not in desirable_set]

    if not required:
        return all_skills, []
    return required, desirable
