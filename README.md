# PayGrind

Project serves as an example of implementation path from simple app to complex distributed system.

## Architecture

## Services

- `billing-service` - service responsible for managing billing data
- `expenses-service` - service responsible for managing expenses data
- `invoice-matching-service` - service responsible for matching invoices with expenses

## How to run

## Roadmap

- ~PGE-1: Switch from Javelin to ktor~
- PGE-2: Implement Watchdog generic service for controlling scheduled jobs (IN PROGRESS)
- PGE-3: Switch from Exposed to JOOQ + common library
- PGE-4: Deploy billing service to Kubernetes
- PGE-5: Implement basic functionality of `expenses-service`
    - PGE-5.1: Implement `create` endpoint
    - PGE-5.2: Connect `expenses-service` with `billing-service` through http / kafka
    - PGE-5.3: Deploy `expenses-service` to Kubernetes
- PGE-6: Implement basic functionality of `invoice-matching-service`
    - PGE-6.1: Add generic interface for mailbox scraping
    - PGE-6.2: Implement `gmail` mailbox scraping
    - PGE-6.3: Connect `billing-service` with `invoice-matching-service` kafka
    - PGE-6.4: Deploy `invoice-matching-service` to Kubernetes
    - PGE-6.3: Implement `outlook` mailbox scraping

