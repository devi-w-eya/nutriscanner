package com.nutriscanner.api.controller;

import com.nutriscanner.api.dto.response.FavoriteDTO;
import com.nutriscanner.api.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/favorites")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class FavoriteController {

    private final FavoriteService favoriteService;

    @PostMapping("/{barcode}")
    public ResponseEntity<Void> addFavorite(
            @PathVariable String barcode,
            @RequestHeader("X-User-Id") String userId) {
        favoriteService.addFavorite(UUID.fromString(userId), barcode);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{barcode}")
    public ResponseEntity<Void> removeFavorite(
            @PathVariable String barcode,
            @RequestHeader("X-User-Id") String userId) {
        favoriteService.removeFavorite(UUID.fromString(userId), barcode);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<FavoriteDTO>> getFavorites(
            @RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(
                favoriteService.getFavorites(UUID.fromString(userId))
        );
    }
}
