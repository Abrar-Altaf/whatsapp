# WhatsApp API Backend

A Spring Boot backend server implementing WhatsApp-like chat features: user profile, chatrooms, messaging (text & attachments), emoji reactions, and pagination. Includes Swagger UI for API testing.

---

## Features
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

## API Endpoints

### Authentication
- All endpoints require `X-USER-ID` header (user id, integer). No real login implemented.

### Profile
- `GET /api/profile` — Get my profile
- `PUT /api/profile` — Update my profile (displayName, avatarUrl)

### Chatrooms
- `GET /api/chatrooms?page=0&size=10` — List my chatrooms (paginated)
- `POST /api/chatrooms` — Create chatroom (1:1 or group)
  - Body: `{ "name": "Group Name", "isGroup": true, "memberIds": [2,3] }`
- `GET /api/chatrooms/{id}` — Get chatroom details
- `POST /api/chatrooms/{id}/members` — Add members
  - Body: `{ "memberIds": [4,5] }`

### Messages
- `GET /api/chatrooms/{chatroomId}/messages?page=0&size=20` — List messages (paginated)
- `POST /api/chatrooms/{chatroomId}/messages` — Send message
  - Multipart form: `content` (text), `attachment` (file, optional, max 10MB)
  - Attachments: images saved to `/static/picture`, videos to `/static/video`

### Emoji Reactions
- `POST /api/messages/{messageId}/emoji` — Add/replace emoji
  - Body: `{ "emojiType": "thumbup" }` (allowed: thumbup, love, crying, surprised)
- `GET /api/messages/{messageId}/emoji` — List emoji reactions for a message

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