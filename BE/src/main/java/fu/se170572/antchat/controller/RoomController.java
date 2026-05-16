package fu.se170572.antchat.controller;

import fu.se170572.antchat.dto.request.CreateRoomRequest;
import fu.se170572.antchat.dto.response.RoomDto;
import fu.se170572.antchat.security.CustomUserDetails;
import fu.se170572.antchat.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    @PostMapping
    public ResponseEntity<RoomDto> createRoom(
            @RequestBody CreateRoomRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(roomService.createRoom(request, userDetails.getId()));
    }

    @GetMapping
    public ResponseEntity<List<RoomDto>> getAllRooms() {
        return ResponseEntity.ok(roomService.getAllRooms());
    }

    @PostMapping("/{roomId}/members/{userId}")
    public ResponseEntity<String> addMember(@PathVariable Long roomId, @PathVariable Long userId) {
        try {
            roomService.addMemberToRoom(roomId, userId);
            return ResponseEntity.ok("Thêm thành viên thành công!");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{roomId}/members/{userId}")
    public ResponseEntity<String> removeMember(@PathVariable Long roomId, @PathVariable Long userId) {
        try {
            roomService.removeMemberFromRoom(roomId, userId);
            return ResponseEntity.ok("Xóa thành viên thành công!");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
