package com.doctruyen.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "http://localhost:3002", "https://duanwebtruyen-production.up.railway.app"}, 
             allowedHeaders = "*", 
             methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE},
             allowCredentials = "true")
public class AuthController {

  /**
   * Đăng ký tài khoản mới
   * POST /auth/register
   */
  @PostMapping("/register")
  public ResponseEntity<Map<String, Object>> register(@RequestBody Map<String, String> request) {
    try {
      String username = request.get("username");
      String email = request.get("email");
      String password = request.get("password");

      // Validate input
      if (username == null || username.trim().isEmpty()) {
        Map<String, Object> error = new HashMap<>();
        error.put("message", "Username không được để trống");
        return ResponseEntity.badRequest().body(error);
      }
      if (email == null || email.trim().isEmpty()) {
        Map<String, Object> error = new HashMap<>();
        error.put("message", "Email không được để trống");
        return ResponseEntity.badRequest().body(error);
      }
      if (password == null || password.length() < 6) {
        Map<String, Object> error = new HashMap<>();
        error.put("message", "Password phải có ít nhất 6 ký tự");
        return ResponseEntity.badRequest().body(error);
      }

      // Generate mock token
      String token = UUID.randomUUID().toString();

      // Return user info with token
      Map<String, Object> response = new HashMap<>();
      Map<String, Object> userInfo = new HashMap<>();
      userInfo.put("id", username.hashCode());
      userInfo.put("username", username);
      userInfo.put("email", email);

      response.put("user", userInfo);
      response.put("token", token);
      response.put("message", "Đăng ký thành công");

      return ResponseEntity.ok(response);
    } catch (Exception e) {
      Map<String, Object> error = new HashMap<>();
      error.put("message", "Lỗi đăng ký: " + e.getMessage());
      return ResponseEntity.badRequest().body(error);
    }
  }

  /**
   * Đăng nhập
   * POST /auth/login
   */
  @PostMapping("/login")
  public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> request) {
    try {
      String email = request.get("email");
      String password = request.get("password");

      // Validate input
      if (email == null || email.trim().isEmpty()) {
        Map<String, Object> error = new HashMap<>();
        error.put("message", "Email không được để trống");
        return ResponseEntity.badRequest().body(error);
      }
      if (password == null || password.length() < 6) {
        Map<String, Object> error = new HashMap<>();
        error.put("message", "Password không hợp lệ");
        return ResponseEntity.badRequest().body(error);
      }

      // Generate mock token
      String token = UUID.randomUUID().toString();

      // Return user info with token
      Map<String, Object> response = new HashMap<>();
      Map<String, Object> userInfo = new HashMap<>();
      userInfo.put("id", email.hashCode());
      userInfo.put("username", email.split("@")[0]);
      userInfo.put("email", email);

      response.put("user", userInfo);
      response.put("token", token);
      response.put("message", "Đăng nhập thành công");

      return ResponseEntity.ok(response);
    } catch (Exception e) {
      Map<String, Object> error = new HashMap<>();
      error.put("message", "Lỗi đăng nhập: " + e.getMessage());
      return ResponseEntity.badRequest().body(error);
    }
  }
}
