# Centralized Docs Management — Eclipse Starter Project

A Java 21 + Spring Boot + MySQL learning prototype based on the banking ECM notes.

## Implemented features

- Ingestion: upload a document and save its metadata.
- Retrieval: search metadata, view document details and download files.
- Bulk service: upload up to 100 files in one request.
- Retention: default retention is 7 years; expired records are automatically archived daily.
- Repository codes: ASC_ST, BANK_ST, CLAIMS_ST, LIFE_ST, IMCO_ST, PNC_ST and SDR_ST.
- Document ID: database-generated numeric ID displayed with at least six digits, e.g. `000001`.

## Important scope note

This is an educational prototype. MySQL stores metadata and a local folder stores file contents. It does not connect to a licensed OpenText/Documentum Docbase and does not implement Kafka messaging. A real banking deployment requires approved Documentum APIs and credentials, Kafka infrastructure, authentication/authorization, encryption, audit logging, malware scanning, backups and compliance review. Do not upload real customer or banking documents to this demo.

## Requirements

- JDK 21
- Eclipse IDE (Eclipse IDE for Enterprise Java and Web Developers is recommended)
- MySQL Server and MySQL Workbench
- Maven support in Eclipse (m2e)
- Postman or another REST client

## Step 1 — Check Java

Open Windows Command Prompt:

```bat
java -version
javac -version
```

Both should report Java 21. If not, set `JAVA_HOME` to your JDK 21 folder and add `%JAVA_HOME%\bin` to Windows `Path`. Restart Eclipse and Command Prompt after changing environment variables.

## Step 2 — Create the database

Open MySQL Workbench and run:

```sql
CREATE DATABASE centralized_docs_db;
```

Hibernate creates the `documents` table when the application starts.

## Step 3 — Configure the MySQL password

Open `src/main/resources/application.properties` and replace:

```properties
spring.datasource.password=CHANGE_ME
```

with your local MySQL password. Update the username or port too if your MySQL setup differs.

## Step 4 — Import the project into Eclipse

1. Extract the ZIP into a development folder, e.g. `C:\JavaProjects`.
2. Open Eclipse.
3. Select **File > Import**.
4. Select **Maven > Existing Maven Projects**, then click **Next**.
5. Browse to the extracted `centralized-docs-management` folder (the folder containing `pom.xml`).
6. Ensure the project is selected and click **Finish**.
7. Wait for Maven dependencies to download and Eclipse to finish building.
8. If red errors remain, right-click the project and choose **Maven > Update Project**. Tick **Force Update of Snapshots/Releases**, then click **OK**.
9. Check **Project > Properties > Java Compiler** and select Java 21. Under **Java Build Path > Libraries**, make sure a JDK 21 execution environment is selected.

## Step 5 — Run the application

1. In Package Explorer, open `src/main/java`.
2. Open `CentralizedDocsApplication.java`.
3. Right-click inside the file.
4. Select **Run As > Java Application**. If Spring Tools is installed, **Run As > Spring Boot App** is also available.
5. Watch the Eclipse Console and wait for the Spring Boot started message.
6. Open this URL:

`http://localhost:8080/api/documents/health`

Expected response:

```json
{
  "status": "UP",
  "application": "Centralized Docs Management"
}
```

If port 8080 is already in use, change `server.port=8081` in `application.properties` and use port 8081 in Postman.

## Step 6 — Upload a document using Postman

- Method: `POST`
- URL: `http://localhost:8080/api/documents/upload`
- Select **Body > form-data**.
- Add these keys:

| Key | Type | Example |
|---|---|---|
| customerId | Text | CUST1001 |
| accountNumber | Text | ACC10001 |
| repositoryCode | Text | BANK_ST |
| documentType | Text | CREDIT_CARD_STATEMENT |
| description | Text | November statement |
| documentCategory | Text | BANK |
| subCategory | Text | Consumer Loan |
| formStatus | Text | ACTIVE |
| retentionYears | Text | 7 |
| file | File | Select a PDF |

Click **Send**. The response includes a `documentId`, for example `000001`.

Supported file extensions: PDF, PNG, JPG, JPEG and TXT. The configured maximum size is 15 MB per file.

## Step 7 — Search documents

- Method: `GET`
- Example URL:

`http://localhost:8080/api/documents/search?customerId=CUST1001&repositoryCode=BANK_ST`

Optional filters: `accountNumber`, `documentType` and `documentCategory`.

## Step 8 — View metadata

- Method: `GET`
- URL: `http://localhost:8080/api/documents/1`

Use the numeric database ID in the URL. The response displays the ID in six-digit format.

## Step 9 — Download a document

- Method: `GET`
- URL: `http://localhost:8080/api/documents/1/download`

Use the numeric database ID returned by the upload operation.

## Step 10 — Bulk upload

- Method: `POST`
- URL: `http://localhost:8080/api/documents/bulk-upload`
- Body: `form-data`
- Add `files` as a **File** field and select multiple files if your Postman version supports it.
- Add common metadata fields: `customerId`, `accountNumber`, `repositoryCode`, `documentType`, `documentCategory`, `retentionYears`.

The prototype accepts up to 100 files per bulk request.

## Step 11 — Archive a document

- Method: `POST`
- URL: `http://localhost:8080/api/documents/1/archive`

The metadata is marked as archived. The original file is not physically deleted.

## Step 12 — Run retention manually

- Method: `POST`
- URL: `http://localhost:8080/api/documents/retention/run`

The scheduled retention service also checks expired documents daily at 2:00 AM using server local time. This prototype archives expired records; it does not automatically erase files.

## Package structure

```text
com.example.centralizeddocs
├── CentralizedDocsApplication.java
├── controller
│   └── DocumentController.java
├── dto
│   └── DocumentResponse.java
├── entity
│   └── Document.java
├── exception
│   └── ApiExceptionHandler.java
├── repository
│   └── DocumentRepository.java
└── service
    ├── DocumentService.java
    ├── DocumentStorageService.java
    └── RetentionService.java
```

## Suggested project presentation

1. Introduce the banking document-management problem.
2. Explain the architecture: REST API, services, MySQL metadata and local file storage.
3. Demonstrate uploading a statement.
4. Show the generated six-digit document ID and metadata.
5. Search by customer ID and repository code.
6. Download the stored document.
7. Demonstrate bulk upload and retention archiving.
8. Explain future production integrations: Documentum Docbase, Kafka, authentication, authorization and audit trails.
