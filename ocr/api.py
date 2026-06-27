from typing import Annotated

from fastapi import FastAPI, File, HTTPException
from fastapi.responses import JSONResponse

from ocr import ocr_mrz

app = FastAPI()


@app.get("/")
async def root():
    return {"healthy": True}


@app.post(
    "/mrz",
    responses={
        400: {"description": "Bad Request"},
        500: {"description": "Internal Server Error"},
    },
)
def mrz_route(image: Annotated[bytes, File()]):
    try:
        # Perform OCR and MRZ parsing
        result = ocr_mrz(image)
        return JSONResponse(
            status_code=200 if bool(result) else 400,
            content={
                "valid": bool(result),
                "raw": result.mrz_code,
                "data": result.fields()._asdict(),
                "errors": result.report.errors,
                "warnings": result.report.warnings,
            },
        )
    except ValueError as e:
        [msg, raw] = e.args
        return JSONResponse(
            status_code=400,
            content={
                "valid": False,
                "raw": raw,
                "errors": [msg],
            },
        )
    except Exception as e:
        print("Unexpected error while processing image")
        print(e)
        raise HTTPException(
            status_code=500,
            detail=f"Unexpected error while processing image:{str(e)}",
        )
