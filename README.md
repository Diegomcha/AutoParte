# AutoParte

AutoParte is a self-hostable web application designed for accommodations to manage bookings while "automatically" complying with Spanish legal reporting obligations for bookings & check-ins.

## Main features

- 🏛️ **Automated legal compliance ([RD 933/2021](https://www.boe.es/diario_boe/txt.php?id=BOE-A-2021-17461))**  
  Transmits bookings, check-ins and cancellations to the Spanish Ministry platform (SES.HOSPEDAJES), ensuring full legal alignment and delivery traceability.

- 📱 **Frictionless guest self-check-in & OCR**  
  Provides a mobile-friendly portal for remote guest registration, featuring automated ID/passport scanning and secure digital signing.

- 🏨 **Flexible workflows & booking management**  
  Manages the full reservation lifecycle with support for both remote self-check-in and in-person desk registration, including an optional manual verification step to prevent data errors.

- 🔒 **[RGPD](https://www.boe.es/doue/2016/119/L00001-00088.pdf)-compliant**  
  Protects sensitive guest information with robust database-level encryption and accommodation-based access controls.

## Documentation

The [wiki](https://github.com/Diegomcha/AutoParte/wiki) contains detailed guides for deploying, administering, and using AutoParte, as well as instructions for developers who want to develop or contribute to the project.

## Project structure

```
AutoParte/
├── backend/                # Backend API (Spring Boot & Java 21)
├── frontend/               # Frontend SPA (React Router Framework & TypeScript)
├── ocr/                    # OCR REST microservice (RapidOCR & Python 3.11)
└── docker-compose.yml      # Deployment configuration (Docker Compose)
```

## License & third party notices

AutoParte is licensed under the [GNU General Public License v3.0](https://www.gnu.org/licenses/gpl-3.0.en.html).

This project relies on various open-source libraries and tools. For a complete list of dependencies and their respective licenses, see the [Third Party Notices](https://github.com/Diegomcha/AutoParte/wiki/Third-Party-Notices) wiki page.
