# LibraryFlow
A professional, production-ready backend for managing a library catalog and circulation (books, authors, categories, copies, members, loans) with role-based access control, auditing, and clean REST endpoints.

## LibraryFlow ERD
```mermaid
erDiagram
  %% === Users & RBAC ===
  USER {
    uuid id PK
    string username  "UNIQUE"
    string email     "UNIQUE"
    string password_hash
    string full_name
    string status    "ACTIVE|INACTIVE"
    timestamptz created_at
    timestamptz updated_at
  }

  ROLE {
    int id PK
    string code "ADMIN|LIBRARIAN|STAFF UNIQUE"
    string name
  }

  USER_ROLE {
    uuid user_id FK
    int role_id  FK
    timestamptz assigned_at
  }

  USER ||--o{ USER_ROLE : has
  ROLE ||--o{ USER_ROLE : has

  %% === Members (Borrowers) ===
  MEMBER {
    uuid id PK
    string code       "library card code UNIQUE"
    string full_name
    string email      "UNIQUE"
    string phone
    string address
    string status     "ACTIVE|SUSPENDED"
    timestamptz created_at
    timestamptz updated_at
  }

  %% === Catalog Core ===
  PUBLISHER {
    int id PK
    string name "UNIQUE"
    string country
    string website
    timestamptz created_at
  }

  AUTHOR {
    int id PK
    string full_name
    string bio
    timestamptz created_at
  }

  CATEGORY {
    int id PK
    string name
    int parent_id FK "self ref"
  }
  CATEGORY ||--o{ CATEGORY : parent_of

  BOOK {
    int id PK
    string isbn13      "UNIQUE per edition"
    string title
    string subtitle
    int edition
    int publication_year
    string language_code
    text summary
    string cover_url
    int publisher_id FK
    timestamptz created_at
    timestamptz updated_at
  }

  PUBLISHER ||--o{ BOOK : publishes

  BOOK_AUTHOR {
    int book_id   FK
    int author_id FK
    int author_order
  }
  BOOK ||--o{ BOOK_AUTHOR : has
  AUTHOR ||--o{ BOOK_AUTHOR : writes

  BOOK_CATEGORY {
    int book_id     FK
    int category_id FK
  }
  BOOK ||--o{ BOOK_CATEGORY : classified_as
  CATEGORY ||--o{ BOOK_CATEGORY : groups

  %% === Inventory (Copies) ===
  BOOK_COPY {
    int id PK
    int book_id FK
    string barcode     "UNIQUE"
    string shelf_location
    string status      "AVAILABLE|ON_LOAN|LOST|DAMAGED"
    date acquisition_date
  }
  BOOK ||--o{ BOOK_COPY : has

  %% === Circulation (Loans) ===
  LOAN {
    int id PK
    int copy_id    FK
    uuid member_id FK
    timestamptz borrowed_at
    timestamptz due_at
    timestamptz returned_at
    string status  "OPEN|RETURNED|OVERDUE"
    int fine_cents
    uuid created_by FK "USER"
    uuid closed_by  FK "USER"
  }
  BOOK_COPY ||--o{ LOAN : loaned_in
  MEMBER    ||--o{ LOAN : makes
  USER      ||--o{ LOAN : processed_by

  %% === Auditing ===
  ACTIVITY_LOG {
    bigserial id PK
    uuid user_id FK
    string action       "CREATE|UPDATE|DELETE|LOGIN|LOAN_CREATE|LOAN_RETURN..."
    string resource_type
    string resource_id
    jsonb  meta
    timestamptz occurred_at
    string ip
  }
  USER ||--o{ ACTIVITY_LOG : writes
```
