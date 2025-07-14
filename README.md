# WhatsApp API Backend

A Spring Boot backend server implementing WhatsApp-like chat features: user profile, chatrooms, messaging (text & attachments), emoji reactions, and pagination. Includes Swagger UI for API testing.

---

## Features
- **User Registration & Login**: Register and look up users by username
- **User Profile**: View and update your profile
- **Chatrooms**: 1:1 and group chatrooms, add members, list chatrooms (paginated)
- **Messages**: Send text or attachments (picture/video, max 10MB), list messages (paginated)
- **Emoji Reactions**: React to messages with [thumbup, love, crying, surprised]
- **Pagination**: All list endpoints support pagination
- **Swagger UI**: Interactive API documentation and tester

---

## Getting Started

### Prerequisites
- Java 17+
- Maven
- PostgreSQL (running locally or accessible remotely)

### Database Setup
1. Start PostgreSQL.
2. Create a database named `whatsapp`:
   ```sh
   psql -U postgres -c "CREATE DATABASE whatsapp;"
   # (Optional) Create user and grant privileges if needed
   # psql -U postgres -c "CREATE USER postgres WITH PASSWORD 'postgres';"
   # psql -U postgres -c "GRANT ALL PRIVILEGES ON DATABASE whatsapp TO postgres;"
   ```
3. Update `src/main/resources/application.properties` if your DB credentials differ:
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/whatsapp
   spring.datasource.username=postgres
   spring.datasource.password=postgres
   ```

### Build & Run
```sh
./mvnw clean package -DskipTests
./mvnw spring-boot:run
```

---

## API Documentation

Once running, access Swagger UI at:
- [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

---

## API Reference

### Registration & Login

#### Register
- **POST** `/api/register`
- **Body:**
  ```json
  {
    "username": "alice",
    "displayName": "Alice",
    "avatarUrl": "https://example.com/avatar.png"
  }
  ```
- **Response:** 200 OK (created user), 409 Conflict (username exists)

#### Login
- **POST** `/api/register/login`
- **Body:**
  ```json
  { "username": "alice" }
  ```
- **Response:** 200 OK (user info), 404 Not Found

---

### Profile

#### Get My Profile
- **GET** `/api/profile`
- **Headers:** `X-USER-ID: <userId>`
- **Response:** 200 OK (user info)

#### Update My Profile
- **PUT** `/api/profile`
- **Headers:** `X-USER-ID: <userId>`
- **Body:**
  ```json
  {
    "displayName": "Alice Wonderland",
    "avatarUrl": "https://example.com/newavatar.png"
  }
  ```
- **Response:** 200 OK (updated user)

---

### Chatrooms

#### List My Chatrooms
- **GET** `/api/chatrooms?page=0&size=10`
- **Headers:** `X-USER-ID: <userId>`
- **Response:** 200 OK (list of chatrooms)

#### Create Chatroom
- **POST** `/api/chatrooms`
- **Headers:** `X-USER-ID: <userId>`
- **Body:**
  ```json
  {
    "name": "Group Name",
    "isGroup": true,
    "memberIds": [2,3]
  }
  ```
- **Response:** 200 OK (created chatroom)

#### Get Chatroom Details
- **GET** `/api/chatrooms/{id}`
- **Headers:** `X-USER-ID: <userId>`
- **Response:** 200 OK (chatroom info), 403 if not a member

#### Add Members to Chatroom
- **POST** `/api/chatrooms/{id}/members`
- **Headers:** `X-USER-ID: <userId>`
- **Body:**
  ```json
  { "memberIds": [4,5] }
  ```
- **Response:** 200 OK (list of added users)

---

### Messages

#### List Messages in Chatroom
- **GET** `/api/chatrooms/{chatroomId}/messages?page=0&size=20`
- **Headers:** `X-USER-ID: <userId>`
- **Response:** 200 OK (paginated list of messages)

#### Send Message (Text or Attachment)
- **POST** `/api/chatrooms/{chatroomId}/messages`
- **Headers:** `X-USER-ID: <userId>`
- **Content-Type:** `multipart/form-data`
- **Form fields:**
  - `content` (text, optional)
  - `attachment` (file, optional, max 10MB)
- **Response:** 200 OK (created message)

---

### Emoji Reactions

#### Add/Replace Emoji Reaction
- **POST** `/api/messages/{messageId}/emoji`
- **Headers:** `X-USER-ID: <userId>`
- **Body:**
  ```json
  { "emojiType": "thumbup" }
  ```
  Allowed: `thumbup`, `love`, `crying`, `surprised`
- **Response:** 200 OK (emoji reaction)

#### List Emoji Reactions for a Message
- **GET** `/api/messages/{messageId}/emoji`
- **Response:** 200 OK (list of emoji reactions)

---

## Attachments
- Images: jpg, jpeg, png, gif, bmp, webp
- Videos: mp4, mov, avi, mkv, webm
- Max size: 10MB
- Saved under `src/main/resources/static/picture` or `static/video`

---

## Development Notes
- ORM: Spring Data JPA
- DB: PostgreSQL (default), H2 (dev, see commented config)
- API docs: Swagger/OpenAPI
- Simple auth: pass user id in `X-USER-ID` header
- Pagination: all list endpoints

---

## Deployment
- Ready for deployment to any Spring Boot-compatible host (e.g., Render, Railway, Heroku, Fly.io)
- Ensure PostgreSQL is available and update `application.properties` accordingly

---

## Optional Enhancements
- Integrate Kafka or RabbitMQ for message queueing
- Real authentication (JWT, OAuth, etc.)
- WebSocket for real-time chat

---

## License
MIT 