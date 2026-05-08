from typing import Annotated

from fastapi import FastAPI, File, HTTPException

from ocr import ocr_mrz

app = FastAPI()


@app.get("/")
async def root():
    return {"healthy": True}


@app.post("/mrz")
def mrz_route(image: Annotated[bytes, File()]):
    try:
        # Perform OCR and MRZ parsing
        result = ocr_mrz(image)
        return {
            "valid": bool(result),
            "raw": result.mrz_code,
            "data": result.fields()._asdict(),
            "errors": result.report.errors,
            "warnings": result.report.warnings,
        }
    except ValueError as e:
        [msg, raw] = e.args
        raise HTTPException(status_code=400, detail={"msg": msg, "raw": raw})
    except Exception as e:
        print("Unexpected error while processing image")
        print(e)
        raise HTTPException(
            status_code=500, detail=f"Unexpected error while processing image"
        )
