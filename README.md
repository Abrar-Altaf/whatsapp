# WhatsApp API Backend

A Spring Boot backend server implementing WhatsApp-like chat features: user profile, chatrooms, messaging (text & attachments), emoji reactions, and pagination. Includes Swagger UI for API testing.

---

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST   | /api/register | Register a new user |
| POST   | /api/login | Login user |
| GET    | /api/profile | Get current user's profile (header: X-USERNAME) |
| PUT    | /api/profile | Update current user's profile (header: X-USERNAME) |
| POST   | /api/profile/upload-picture | Upload profile picture (header: X-USERNAME, multipart) |
| GET    | /api/profile/by-mobile-numbers?mobileNumbers=123,456 | Get users by a list of mobile numbers |
| GET    | /api/chatrooms | List chatrooms for user (header: X-USERNAME, paginated) |
| POST   | /api/chatrooms | Create a new chatroom (header: X-USERNAME) |
| GET    | /api/chatrooms/{id} | Get chatroom details (header: X-USERNAME) |
| POST   | /api/chatrooms/{id}/members | Add members to chatroom (header: X-USERNAME) |
| GET    | /api/chatrooms/{chatroomId}/messages | List messages in chatroom (header: X-USERNAME, paginated) |
| POST   | /api/chatrooms/{chatroomId}/messages | Send message (header: X-USERNAME, multipart) |
| POST   | /api/messages/{messageId}/emoji | Add/replace emoji reaction (header: X-USERNAME) |
| GET    | /api/messages/{messageId}/emoji | List emoji reactions for a message |

---

## Swagger / OpenAPI Links

- [Swagger UI (index)](http://localhost:8080/swagger-ui/index.html)
- [Swagger UI (legacy)](http://localhost:8080/swagger-ui.html)
- [OpenAPI JSON](http://localhost:8080/v3/api-docs)
- [OpenAPI YAML](http://localhost:8080/v3/api-docs.yaml)

---

## Features
- **User Registration & Login**: Register and look up users by country code and mobile number
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
- MySQL (running locally or accessible remotely)

### Database Setup
1. Start MySQL.
2. Create a database named `whatsapp`:
   ```sh
   mysql -u root -p -e "CREATE DATABASE whatsapp;"
   # (Optional) Create user and grant privileges if needed
   # mysql -u root -p -e "CREATE USER 'root'@'localhost' IDENTIFIED BY 'yourpassword';"
   # mysql -u root -p -e "GRANT ALL PRIVILEGES ON whatsapp.* TO 'root'@'localhost';"
   ```
3. Update `src/main/resources/application.properties` if your DB credentials differ:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/whatsapp?useSSL=false&serverTimezone=UTC
   spring.datasource.username=root
   spring.datasource.password=
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
    "country_code": "+1",
    "mobile_number": "1234567890",
    "display_name": "Alice"
  }
  ```
- **Response:** 200 OK (created user, includes generated `username`), 409 Conflict (user already exists)

#### Login
- **POST** `/api/login`
- **Body:**
  ```json
  {
    "country_code": "+1",
    "mobile_number": "1234567890"
  }
  ```
- **Response:** 200 OK (user info), 404 Not Found

---

### Profile

#### Get My Profile
- **GET** `/api/profile`
- **Headers:** `X-USERNAME: <username>`
- **Response:** 200 OK (user info, includes `profileUrl` field)

#### Update My Profile
- **PUT** `/api/profile`
- **Headers:** `X-USERNAME: <username>`
- **Body:**
  ```json
  {
    "display_name": "Alice Wonderland",
    "profile_url": "https://example.com/newavatar.png"
  }
  ```
- **Response:** 200 OK (updated user)

#### Upload Profile Picture
- **POST** `/api/profile/upload-picture`
- **Headers:** `X-USERNAME: <username>`
- **Content-Type:** `multipart/form-data`
- **Form field:**
  - `file` (required, jpg/jpeg/png, max 10MB)
- **Response:** 200 OK (URL of uploaded profile picture)
- **Description:** Uploads a new profile picture for the user. The backend saves the file under `src/main/resources/static/profile` and updates the user's `profileUrl`. The image is then visible to all users via the profile API.

---

### Chatrooms

#### List My Chatrooms
- **GET** `/api/chatrooms?page=0&size=10`
- **Headers:** `X-USERNAME: <username>`
- **Response:** 200 OK (list of chatrooms)

#### Create Chatroom
- **POST** `/api/chatrooms`
- **Headers:** `X-USERNAME: <username>`
- **Body:**
  ```json
  {
    "name": "Group Name",
    "is_group": true,
    "member_ids": [2,3]
  }
  ```
- **Response:** 200 OK (created chatroom)

#### Get Chatroom Details
- **GET** `/api/chatrooms/{id}`
- **Headers:** `X-USERNAME: <username>`
- **Response:** 200 OK (chatroom info), 403 if not a member

#### Add Members to Chatroom
- **POST** `/api/chatrooms/{id}/members`
- **Headers:** `X-USERNAME: <username>`
- **Body:**
  ```json
  { "member_ids": [4,5] }
  ```
- **Response:** 200 OK (list of added users)

---

### Messages

#### List Messages in Chatroom
- **GET** `/api/chatrooms/{chatroomId}/messages?page=0&size=20`
- **Headers:** `X-USERNAME: <username>`
- **Response:** 200 OK (paginated list of messages)

#### Send Message (Text or Attachment)
- **POST** `/api/chatrooms/{chatroomId}/messages`
- **Headers:** `X-USERNAME: <username>`
- **Content-Type:** `multipart/form-data`
- **Form fields:**
  - `content` (text, optional)
  - `attachment` (file, optional, max 10MB)
- **Response:** 200 OK (created message)

---

### Emoji Reactions

#### Add/Replace Emoji Reaction
- **POST** `/api/messages/{messageId}/emoji`
- **Headers:** `X-USERNAME: <username>`
- **Body:**
  ```json
  { "emoji_type": "thumbup" }
  ```
  Allowed: `thumbup`, `love`, `crying`, `surprised`
- **Response:** 200 OK (emoji reaction)

#### List Emoji Reactions for a Message
- **GET** `/api/messages/{messageId}/emoji`
- **Response:** 200 OK (list of emoji reactions)

---

## Authentication Note
- All authenticated endpoints require the `X-USERNAME` header with the backend-generated username (e.g., `username_ab12cd`).
- The username is returned in the response when a user registers.
- Do not use userId in headers or requests.

---

## Attachments
- Images: jpg, jpeg, png, gif, bmp, webp
- Videos: mp4, mov, avi, mkv, webm
- Max size: 10MB
- Saved under `src/main/resources/static/picture`, `static/video`, or `static/profile` (for profile pictures)

---

## Development Notes
- ORM: Spring Data JPA
- DB: MySQL (default), H2 (dev, see commented config)
- API docs: Swagger/OpenAPI
- Simple auth: pass username in `X-USERNAME` header
- Pagination: all list endpoints

---

## Deployment
- Ready for deployment to any Spring Boot-compatible host (e.g., Render, Railway, Heroku, Fly.io)
- Ensure MySQL is available and update `application.properties` accordingly 