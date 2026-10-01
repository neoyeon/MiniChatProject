# (Java & Spring) 실시간 채팅 웹사이트

## 프로젝트 속성

- **태그**: Java, Spring Boot, Spring Security, Thymeleaf, JPA, Oracle, WebSocket, STOMP, JavaScript
- **한 줄 설명**: 친구를 추가하고 1:1·그룹 대화방에서 실시간으로 메시지를 주고받는 웹 채팅 서비스입니다.
- **개발 인원(역할)**: 1명 · 기획 및 풀스택 개발
- **GitHub**: 저장소 공개 후 링크 추가

## 프로젝트 소개

회원가입과 로그인을 거쳐 사용자를 검색하고 친구로 추가한 뒤, 친구와 1:1 또는 그룹 채팅을 할 수 있는 웹 서비스입니다. Spring Boot와 Spring Security로 인증을 구성하고, Thymeleaf로 화면을 렌더링합니다. STOMP/WebSocket으로 메시지를 실시간 전달하고 JPA와 Oracle Database에 저장해 대화 내역을 다시 확인할 수 있도록 구현했습니다.

> **대표 이미지**: 대표 화면 이미지를 여기에 추가

## 💻 기술 스택

| 영역 | 기술 | 프로젝트에서 사용한 내용 |
|---|---|---|
| Backend | Java 17, Spring Boot, Spring MVC | 사용자·친구·채팅방·메시지 요청을 컨트롤러와 서비스로 분리해 처리 |
| Security | Spring Security, BCrypt | 로그인 인증, 인증된 사용자 정보 사용, 비밀번호 해시 저장, CSRF 보호 |
| View | Thymeleaf, HTML, CSS, JavaScript | 로그인·회원가입·친구 목록·채팅 화면 렌더링 및 화면 동작 구현 |
| Real-time | WebSocket, STOMP, SockJS | 채팅방 토픽 구독과 메시지 실시간 송수신 |
| Database | Oracle Database, Spring Data JPA | 사용자, 친구 관계, 채팅방, 참여자, 메시지 데이터 저장 및 조회 |
| Build | Gradle | 의존성 및 애플리케이션 빌드 구성 |

## 🙋 역할

**풀스택 개발**: Spring Boot·Thymeleaf·Oracle을 연결해 사용자 화면, 서버 기능, 데이터 처리를 구현했습니다.

- BCrypt 기반 비밀번호 암호화를 적용한 회원가입 및 로그인 흐름 구현
- 아이디 검색, 친구 추가 및 양방향 친구 관계 저장
- 친구를 초대하는 1:1·그룹 채팅방 생성과 기존 1:1 대화방 재사용
- 채팅방 멤버 확인, 메시지 저장, WebSocket 실시간 전달 구현
- 날짜·발신자별 메시지 표시와 채팅방별 대화 내역 조회 구현
- 채팅 화면과 입력 영역을 분리해 대화 내역 스크롤 중에도 입력창이 보이도록 구성

## 구현하며 고려한 점

- **채팅 영역 부분 갱신**: 채팅방을 바꿀 때 페이지 전체를 새로고침하지 않도록 방의 HTML 조각만 비동기로 요청하고, 채팅 영역에 삽입했습니다. 사이드바와 현재 화면 상태는 유지하면서 대화 내용만 전환할 수 있습니다.
- **방 전환과 실시간 구독 동기화**: 채팅방 내용을 가져오는 요청과 WebSocket 연결 준비를 함께 기다린 뒤 새 방을 구독하고, 이전 방 구독은 해제해 방을 빠르게 바꿔도 다른 대화의 메시지가 섞이지 않게 했습니다.
- **기존 1:1 방 재사용**: 친구와 이미 참여 중인 1:1 방을 먼저 찾아 재사용하고, 없을 때만 새 방을 생성해 중복 대화방 생성을 줄였습니다.
- **저장과 실시간 전달 순서**: 메시지를 데이터베이스에 저장한 후 채팅방 구독자에게 전송해, 실시간 화면과 재접속 후 조회하는 대화 내역이 같은 데이터를 기준으로 하도록 했습니다.
- **서버에서 참여 권한 검증**: 화면에서 전달된 친구·방 정보를 그대로 신뢰하지 않고, 친구 관계와 채팅방 멤버 여부를 서버에서 확인해 초대 및 메시지 접근을 제한했습니다.

## 📝 주요 기능 및 코드

각 코드에는 실제 구현 흐름을 설명하는 주석을 덧붙였습니다.

### 1. 회원가입과 비밀번호 암호화

중복 아이디인지 확인한 뒤, 비밀번호 원문 대신 BCrypt 해시를 저장합니다.

```java
public User register(String username, String password, String nickname) {
    // 이미 사용 중인 아이디는 가입할 수 없습니다.
    if (userRepository.existsByUsername(username)) {
        throw new IllegalArgumentException("이미 존재하는 사용자입니다.");
    }

    // 원문 비밀번호 대신 BCrypt 해시를 사용자 정보에 저장합니다.
    String encodedPassword = passwordEncoder.encode(password);
    User user = new User(username, encodedPassword, nickname);
    return userRepository.save(user);
}
```

### 2. 친구 검색과 친구 추가

친구 추가 시 자기 자신과 이미 등록된 사용자를 걸러내고, 양쪽 사용자의 친구 목록에 관계를 저장합니다.

```java
// 아래 user와 friend는 사용자 저장소에서 조회한 현재 사용자와 친구입니다.
// 자기 자신을 친구로 추가하는 것을 막습니다.
if (user.getId().equals(friend.getId())) {
    throw new IllegalArgumentException("자기 자신은 친구로 추가할 수 없습니다.");
}

// 중복 친구 관계를 만들지 않습니다.
if (friendRepository.existsByUserAndFriend(user, friend)) {
    throw new IllegalArgumentException("이미 친구입니다.");
}

// 양쪽 친구 목록에서 서로를 확인할 수 있도록 관계를 저장합니다.
friendRepository.save(new Friend(user, friend));
friendRepository.save(new Friend(friend, user));
```

### 3. 친구 목록에서 1:1 대화 시작

친구 목록의 `1:1` 버튼은 서버에 친구 아이디를 전송합니다. 이미 두 사용자가 참여 중인 1:1 방이 있으면 그 방을 반환하고, 없으면 새 방과 참여자를 생성합니다.

```java
// 현재 사용자와 친구 사이에 등록된 친구 관계가 있는지 확인합니다.
if (!friendRepository.existsByUserAndFriend(user, friend)) {
    throw new IllegalArgumentException("친구로 등록된 사용자와만 대화할 수 있습니다.");
}

// 두 사용자가 이미 참여 중인 1:1 방이면 새로 만들지 않고 재사용합니다.
for (ChatRoomMember membership : chatRoomMemberRepository.findByUser(user)) {
    ChatRoom room = membership.getRoom();
    if (room.getType() != ChatRoomType.DIRECT) continue;

    List<ChatRoomMember> members = chatRoomMemberRepository.findByRoom(room);
    if (members.size() == 2 && members.stream()
            .anyMatch(member -> member.getUser().getId().equals(friend.getId()))) {
        return room;
    }
}

// 기존 방이 없으면 채팅방을 만들고 두 사용자를 멤버로 등록합니다.
ChatRoom room = chatRoomRepository.save(
        new ChatRoom(user.getNickname() + " · " + friend.getNickname(), ChatRoomType.DIRECT, user)
);
chatRoomMemberRepository.save(new ChatRoomMember(room, user));
chatRoomMemberRepository.save(new ChatRoomMember(room, friend));
return room;
```

### 4. 그룹 채팅방 생성 및 초대 검증

방 이름과 채팅방 종류를 확인하고, 초대 대상이 실제 친구인지 서버에서도 검증합니다.

```java
// 비어 있거나 너무 긴 방 이름은 저장하지 않습니다.
String roomName = name == null ? "" : name.trim();
if (roomName.isEmpty() || roomName.length() > 100) {
    throw new IllegalArgumentException("채팅방 이름은 1자 이상 100자 이하로 입력해주세요.");
}

if (type == null) {
    throw new IllegalArgumentException("채팅방 종류를 선택해주세요.");
}
if (friendUsernames == null || friendUsernames.isEmpty()) {
    throw new IllegalArgumentException("채팅할 친구를 한 명 이상 선택해주세요.");
}

User user = userRepository.findByUsername(username)
        .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

// 1:1 방에는 친구 한 명만 초대할 수 있습니다.
if (type == ChatRoomType.DIRECT && friendUsernames.size() != 1) {
    throw new IllegalArgumentException("1:1 채팅은 친구 한 명만 선택할 수 있습니다.");
}

ChatRoom room = chatRoomRepository.save(new ChatRoom(roomName, type, user));
chatRoomMemberRepository.save(new ChatRoomMember(room, user));

// 선택된 친구마다 계정을 조회하고 서버에서 친구 관계를 검증합니다.
for (String friendUsername : friendUsernames) {
    User friend = userRepository.findByUsername(friendUsername)
            .orElseThrow(() -> new IllegalArgumentException("친구를 찾을 수 없습니다."));

    // 생성자를 초대 목록에서 제외하고, 중복 참여자 등록을 방지합니다.
    if (friend.getId().equals(user.getId())) continue;

    // 클라이언트 입력을 그대로 신뢰하지 않고 친구만 초대합니다.
    if (!friendRepository.existsByUserAndFriend(user, friend)) {
        throw new IllegalArgumentException("친구로 등록된 사용자만 채팅방에 초대할 수 있습니다.");
    }

    if (!chatRoomMemberRepository.existsByRoomAndUser(room, friend)) {
        chatRoomMemberRepository.save(new ChatRoomMember(room, friend));
    }
}
```

### 5. 실시간 메시지 저장과 전달

인증 정보에서 발신자를 확인하고 메시지를 데이터베이스에 저장한 뒤, 해당 채팅방 토픽 구독자에게 전달합니다.

```java
@MessageMapping("/chat.send")
public void sendMessage(ChatMessage chatMessage, Authentication authentication) {
// 요청 본문이 아니라 로그인 인증 정보에서 발신자 아이디를 가져옵니다.
String username = authentication.getName();
User user = userRepository.findByUsername(username)
        .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

// 채팅방 참여 여부를 확인하고 메시지를 데이터베이스에 저장합니다.
var savedMessage = messageService.sendMessage(
    chatMessage.getRoomId(), username, chatMessage.getContent()
);

// 저장 시각과 발신자 정보를 실시간 응답에 포함합니다.
ChatMessage response = new ChatMessage(
        chatMessage.getRoomId(), username, user.getNickname(),
        chatMessage.getContent(), savedMessage.getCreatedAt()
);

// 같은 채팅방을 구독 중인 사용자에게 저장된 메시지를 전달합니다.
messagingTemplate.convertAndSend(
        "/topic/room/" + chatMessage.getRoomId(), response
);
}
```

### 6. 채팅방 접근 권한과 대화 내역 조회

채팅방에 참여 중인 사용자만 대화 내역을 열 수 있습니다. 기존 메시지는 생성 시간 순서대로 조회합니다.

```java
// 요청한 사용자가 실제 채팅방 멤버인지 확인합니다.
boolean isMember = chatRoomMemberRepository.existsByRoomAndUser(room, user);
if (!isMember) {
    throw new IllegalArgumentException("이 채팅방에 참여하고 있지 않습니다.");
}

// 메시지 저장소에서 생성 시간 오름차순으로 대화 내역을 가져옵니다.
return messageRepository.findByRoomOrderByCreatedAtAsc(room);
```

### 7. 화면에서 채팅방 구독 및 최신 메시지 표시

화면에서 선택한 방의 HTML을 불러오고 WebSocket 연결이 준비된 뒤 해당 방 토픽을 구독합니다. 메시지를 추가한 뒤 대화 영역만 최신 위치로 이동합니다.

```javascript
// 화면 요청과 WebSocket 연결이 모두 준비될 때까지 기다립니다.
const [response] = await Promise.all([
    fetch('/main/room/' + roomId + '/fragment'),
    connectWebSocket()
]);

// 현재 선택된 방의 메시지만 수신합니다.
currentSubscription = stompClient.subscribe(
    '/topic/room/' + roomId,
    frame => addMessage(JSON.parse(frame.body))
);

// 메시지 영역만 마지막 메시지까지 이동시킵니다.
area.scrollTop = area.scrollHeight;
```

## 🖼️ 화면 자료

이미지를 직접 추가할 수 있도록 비워 두었습니다.

| 서비스 화면 | 설명 |
|---|---|
| *(이미지 추가)* | 시작·로그인 화면 |
| *(이미지 추가)* | 친구 검색 및 친구 목록 |
| *(이미지 추가)* | 1:1 대화 및 실시간 메시지 |
| *(이미지 추가)* | 그룹 채팅방 생성 화면 |
