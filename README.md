# Payment Service

**Payment Service** is a microservice designed for processing payments in multiple currencies with automatic conversion. The service implements a two-phase payment processing methodology (Dual Message System, DMS) that separates the process into authorization (reserving funds) and clearing (final fund deduction). It also supports asynchronous processing and management of pending payment operations.

---

## Key Features

- **Multi-Currency Payments and Conversion:**  
  Accepts payments in any supported currency and automatically converts the amount to a configured base currency (via `CurrencyApiProperties`).

- **Dual Message System (DMS):**  
  - **Authorization:** Validates payment details, reserves funds, and generates a unique verification code.  
  - **Clearing:** Finalizes the payment by deducting the reserved funds once the operation is confirmed.

- **Pending Payment Operations:**  
  Supports initiating, canceling, confirming, and tracking the status of pending payment operations to enable asynchronous processing and ensure reliable transaction management.

- **External Integration:**  
  Interacts with external services (e.g., Account Service) for updating account balances and uses messaging (e.g., via Redis) for synchronous inter-service communication.

---

## Architecture and Components

### 1. Controllers

- **PaymentController:**  
  Handles incoming payment requests. Upon receiving a request, it:
  - Validates input data (amount, currency, payment number).
  - Converts the payment amount from the request currency to the base currency using `CurrencyService`.
  - Generates a random verification code.
  - Returns a response containing the payment status, converted amount, verification code, and a confirmation message.

- **PendingOperationController:**  
  Manages pending payment operations:
  - **Initiate Operation (`POST /api/v1/operations/initiate`):** Creates a new pending payment operation and returns its unique UUID.
  - **Cancel Operation (`POST /api/v1/operations/cancel/{id}`):** Cancels a pending operation by its UUID, releasing any reserved funds.
  - **Confirm Operation (`POST /api/v1/operations/confirm/{id}`):** Confirms and finalizes a pending operation.
  - **Get Operation Status (`GET /api/v1/operations/{id}`):** Retrieves detailed status information for a pending operation.

### 2. Payment Processing Logic

- **Currency Conversion:**  
  The `CurrencyService` converts payment amounts from the original currency to the base currency (as defined in `CurrencyApiProperties`), logging the conversion details for traceability.

- **Dual Message Processing (DMS):**  
  Payments are processed in two phases. The authorization phase reserves funds and places the transaction in a pending state, while the clearing phase finalizes the payment and deducts the funds once confirmation is received.

- **Handling Pending Operations:**  
  The `PendingOperationService` manages the creation, cancellation, confirmation, and status tracking of pending operations, ensuring asynchronous processing and minimal delay.

- **Error Handling and Idempotency:**  
  The service layer implements robust error handling, input validation, and idempotency measures to prevent duplicate processing and ensure consistent transaction flows.
