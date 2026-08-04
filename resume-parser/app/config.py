import os


MAX_FILE_SIZE_BYTES = int(os.getenv("MAX_PDF_SIZE_BYTES", str(5 * 1024 * 1024)))
MAX_PAGES = int(os.getenv("MAX_PDF_PAGES", "30"))
