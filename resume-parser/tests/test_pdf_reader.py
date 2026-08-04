from io import BytesIO

from pypdf import PdfWriter

from app.services.pdf_reader import extract_pdf_text


def test_reads_a_real_pdf_and_warns_when_it_has_no_text() -> None:
    writer = PdfWriter()
    writer.add_blank_page(width=595, height=842)
    output = BytesIO()
    writer.write(output)

    result = extract_pdf_text(output.getvalue(), max_pages=30)

    assert result.page_count == 1
    assert result.analyzed_page_count == 1
    assert result.text == ""
    assert any("OCR" in warning for warning in result.warnings)
