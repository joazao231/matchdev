from dataclasses import dataclass
from io import BytesIO

from pypdf import PdfReader
from pypdf.errors import PdfReadError


class InvalidPdfError(ValueError):
    """Raised when the uploaded content cannot be processed as a PDF."""


@dataclass(frozen=True)
class PdfTextResult:
    text: str
    page_count: int
    analyzed_page_count: int
    warnings: list[str]


def extract_pdf_text(content: bytes, max_pages: int) -> PdfTextResult:
    if not content.startswith(b"%PDF-"):
        raise InvalidPdfError("O arquivo enviado não possui uma estrutura PDF válida")

    try:
        reader = PdfReader(BytesIO(content))
    except (PdfReadError, ValueError, OSError) as exc:
        raise InvalidPdfError("Não foi possível ler o arquivo PDF") from exc

    if reader.is_encrypted:
        try:
            if reader.decrypt("") == 0:
                raise InvalidPdfError("O currículo está protegido por senha")
        except (PdfReadError, TypeError, ValueError) as exc:
            raise InvalidPdfError("O currículo está protegido por senha") from exc

    page_count = len(reader.pages)
    analyzed_page_count = min(page_count, max_pages)
    warnings: list[str] = []

    if page_count > max_pages:
        warnings.append(
            f"O PDF possui {page_count} páginas; somente as primeiras {max_pages} foram analisadas."
        )

    page_texts: list[str] = []
    for page in reader.pages[:analyzed_page_count]:
        try:
            page_texts.append(page.extract_text() or "")
        except (KeyError, TypeError, ValueError):
            warnings.append("Uma página não pôde ter seu texto extraído.")

    text = "\n".join(page_texts).strip()
    if len(text) < 40:
        warnings.append(
            "Pouco texto foi encontrado. O currículo pode ser uma imagem digitalizada e precisar de OCR."
        )

    return PdfTextResult(
        text=text,
        page_count=page_count,
        analyzed_page_count=analyzed_page_count,
        warnings=warnings,
    )
