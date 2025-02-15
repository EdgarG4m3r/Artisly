# Artisly Backend

This is my take on a scalable, lightweight, and secure e-commerce platform backend for selling digital goods in the Indonesian market. Developed in just a few days as my final university project, Artisly explores modern architectural principles and deployment strategies suited for serverless and cloud environments.

## Features

- **Store Management** — Users can create their own store, manage products, and handle orders.
- **Store Vetting** — Users can apply to become verified stores and receive a badge; the vetting process is handled by an admin and requires a government-issued ID.
- **Immutable Product** — A copy of the product is stored as an immutable object so that the exact product listing is preserved when an order is made. This immutable product is attached to the order.
- **Store Report** — Users can report a store, enabling an admin to take action.
- **Discussion** — Users and store owners can discuss the product on the product page.
- **Review** — Customers can review products and stores. Reviews are displayed on both the product and store pages.
- **Wishlist** — Users can add products to their wishlist.

## Architecture & Scalability

Artisly follows a monolithic, three-tier architecture designed for extendability and future development:

- **Three-Layer Architecture** — The system is divided into three layers:
    - **Handlers:** Responsible for handling requests, responses, and input sanitization.
    - **Services:** Contains the business logic.
    - **Data:** Manages data access and caching.
- **Modular Design** — Although monolithic, the codebase is structured to make it easier to extract each module into a microservice in the future.
- **Stateless Design** — The system is designed to be stateless, facilitating horizontal scaling. Caching are used to boost performance.
- **Optimized for Cloud/Serverless Deployments** — Artisly is designed for fast boot times and low memory usage

## Deployment

Artisly is intended for deployment in cloud environments. Its stateless design allows for easy horizontal scaling, and the use of Redis for caching enhances performance. The provided Dockerfile makes containerization straightforward:

```bash
docker build -t artisly-backend .
docker run -d -p 8080:8080 artisly-backend
```

## Security

Artisly is designed following an "assume breach" mindset throughout development.

### 1. Architectural Security
- **Modular Codebase:** The three-layer architecture enforces separation of concerns, localizing potential vulnerabilities.
- **Stateless Architecture:** Supports horizontal scaling and eliminates single points of failure.
- **Containerized Deployment:** Isolates the application from the host system, providing a controlled deployment environment.

### 2. Security-Conscious System Design
- **Defense in Depth:** Artisly employs a multi-layered defense strategy that begins with system design and extends to implementation, reducing the overall attack surface.

### 3. Secure Coding Best Practices
- **Input Sanitization:** Performed at the earliest point in the codebase to prevent injection attacks.
- **Secure Password Storage:** Utilizes Argon2id for hashing passwords, offering resistance against GPU and ASIC attacks.
- **Prepared Statements:** Used for all database operations to mitigate SQL injection risks.
- **Rate Limiting:** Implemented early in the request handling process to mitigate DDoS attacks.
- **Authentication & Authorization:** Managed using secure session tokens.
- **Error Handling:** Configured to avoid exposing sensitive information to clients while still providing useful debugging details to developers.

## Technology Stack

- **Backend:** Java
- **Database:** MySQL, Redis
- **Image Storage:** AWS S3
- **Other:** Docker, Algolia

## Disclaimer

While Artisly is fully tested for its current functionality, it still lacks some features required for a production-ready e-commerce platform. This project is intended for educational purposes and only showcases exploratory work in building a scalable, lightweight, and secure backend for cloud environments. Further development and testing are required for production development. Note that Algolia search should be integrated directly on the frontend.