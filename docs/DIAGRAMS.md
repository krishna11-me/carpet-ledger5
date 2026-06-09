## Diagrams — Carpet Ledger

This file contains architecture and flow diagrams for the Carpet Ledger app. Use a Mermaid renderer to view the diagrams.

### Component diagram

```mermaid
graph LR
  UI["Compose UI\nCarpetHomeScreen"] -->|observes| VM["CarpetViewModel"]
  VM -->|uses| Repo["TransactionRepository"]
  Repo -->|reads/writes| DB["Room Database\nAppDatabase + DAOs"]
  DB --> Entities["Entities: CarpetImage, Transaction, CustomSize, Type, Category"]
  VM -->|calls| Pdf["PdfExporter"]

  classDef comp fill:#f9f,stroke:#333,stroke-width:1px;
  class UI,VM,Repo,DB,Pdf comp;
```

### Export sequence (PDF)

```mermaid
sequenceDiagram
  participant U as UI
  participant V as ViewModel
  participant R as Repository
  participant D as Database
  participant P as PdfExporter

  U->>V: requestExport(criteria)
  V->>R: queryTransactions(criteria)
  R->>D: SELECT transactions
  D-->>R: results
  R-->>V: transaction list
  V->>P: buildPdf(transaction list, metadata)
  P-->>V: pdfFile
  V-->>U: exportComplete(filePath)
```

### Database ER (simplified)

```mermaid
erDiagram
    CARPET_IMAGE {
        int id PK
        string uri
        string name
        datetime created_at
    }
    TRANSACTION {
        int id PK
        int carpet_image_id FK
        float price
        int quantity
        datetime date
    }
    CUSTOM_SIZE {
        int id PK
        string label
        float width
        float height
    }
    CUSTOM_TYPE {
        int id PK
        string label
    }
    CUSTOM_CATEGORY {
        int id PK
        string label
    }

    CARPET_IMAGE ||--o{ TRANSACTION : "has"
    CARPET_IMAGE }o--|| CUSTOM_SIZE : "may use"
    CARPET_IMAGE }o--|| CUSTOM_TYPE : "may use"
    CARPET_IMAGE }o--|| CUSTOM_CATEGORY : "may belong to"
```

### Notes

- The diagrams are simplified to show relationships and main flows.
- To render Mermaid diagrams, paste the contents into a Mermaid live editor or a Markdown viewer that supports Mermaid.
