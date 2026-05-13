import re
import unicodedata
from typing import cast

from mrz.checker.td1 import TD1CodeChecker
from mrz.checker.td3 import TD3CodeChecker
from rapidocr import RapidOCR
from rapidocr.utils.output import RapidOCROutput

engine = RapidOCR(config_path="./rapidocr_config.yaml")


def sanitize_mrz_line(line: str) -> str:
    line = unicodedata.normalize("NFKC", line)
    line = line.upper()
    line = re.sub(r"\s+", "", line)
    line = re.sub(r"[^A-Z0-9<]", "", line)
    return line


def ocr_mrz(image: bytes) -> TD1CodeChecker | TD3CodeChecker:
    # Perform OCR on the image and extract text lines
    ocr_res = cast(RapidOCROutput, engine(image)).txts
    if not ocr_res:
        raise ValueError("No se ha detectado texto en la imagen", None)

    # Extract only the lines that are likely to be MRZ lines (length >= 30)
    det_mrz = [sanitize_mrz_line(line) for line in ocr_res]
    det_mrz = [line for line in det_mrz if len(line) >= 30]

    # Join the detected MRZ lines into a single string & determine the type of MRZ based on the number of lines and their lengths
    mrz = "\n".join(det_mrz)
    if len(det_mrz) == 3 and all(len(line) == 30 for line in det_mrz):
        # DNI or TIE
        return TD1CodeChecker(mrz)
    elif len(det_mrz) == 2 and all(len(line) == 44 for line in det_mrz):
        # Passport
        return TD3CodeChecker(mrz)
    else:
        raise ValueError("No se ha detectado un MRZ válido en la imagen", mrz)
