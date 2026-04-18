const router = require('express').Router();
const jwt = require('jsonwebtoken');
const bcrypt = require('bcryptjs');
const config = require('../config');
const { verifyToken } = require('../middleware/auth');
const emailService = require('../services/emailService');

// Mock users database (trong thực tế sẽ dùng PostgreSQL)
const users = new Map();
let nextUserId = 1;

// Lưu thông tin đăng ký tạm thời chờ OTP
const pendingRegistrations = new Map();

// Step 1: Gửi OTP để đăng ký
router.post('/register/send-otp', async (req, res) => {
  try {
    const { username, email, password } = req.body;

    // Validate input
    if (!username || !email || !password) {
      return res.status(400).json({ 
        error: 'Vui lòng cung cấp tên đăng nhập, email và mật khẩu' 
      });
    }

    if (password.length < 6) {
      return res.status(400).json({ 
        error: 'Mật khẩu phải có ít nhất 6 ký tự' 
      });
    }

    // Check if user already exists
    for (let user of users.values()) {
      if (user.email === email) {
        return res.status(400).json({ error: 'Email đã được sử dụng' });
      }
      if (user.username === username) {
        return res.status(400).json({ error: 'Tên đăng nhập đã tồn tại' });
      }
    }

    // Hash password
    const hashedPassword = await bcrypt.hash(password, 10);

    // Lưu thông tin đăng ký tạm thời (hết hạn trong 10 phút)
    const expiryTime = Date.now() + 10 * 60 * 1000;
    pendingRegistrations.set(email, {
      username,
      email,
      password: hashedPassword,
      expiryTime
    });

    // Gửi OTP
    const emailResult = await emailService.sendOTPEmail(email);

    if (!emailResult.success) {
      pendingRegistrations.delete(email);
      return res.status(500).json({ 
        error: emailResult.error || 'Không thể gửi OTP' 
      });
    }

    res.status(200).json({
      success: true,
      message: 'OTP đã được gửi đến email của bạn',
      data: {
        email: email,
        // For development only - remove in production
        otp_test: process.env.NODE_ENV === 'development' ? emailService.getOTP(email) : undefined
      }
    });
  } catch (error) {
    console.error('❌ Lỗi gửi OTP:', error);
    res.status(500).json({ error: error.message });
  }
});

// Step 2: Xác thực OTP và tạo tài khoản
router.post('/register/verify-otp', async (req, res) => {
  try {
    const { email, otp } = req.body;

    if (!email || !otp) {
      return res.status(400).json({ 
        error: 'Vui lòng cung cấp email và OTP' 
      });
    }

    // Xác thực OTP
    const otpResult = emailService.verifyOTP(email, otp);
    if (!otpResult.success) {
      return res.status(400).json({ error: otpResult.error });
    }

    // Kiểm tra thông tin đăng ký tạm thời
    if (!pendingRegistrations.has(email)) {
      return res.status(400).json({ 
        error: 'Phiên đăng ký hết hạn, vui lòng thử lại' 
      });
    }

    const pending = pendingRegistrations.get(email);

    // Kiểm tra hết hạn
    if (Date.now() > pending.expiryTime) {
      pendingRegistrations.delete(email);
      return res.status(400).json({ 
        error: 'Phiên đăng ký hết hạn, vui lòng thử lại' 
      });
    }

    // Tạo tài khoản
    const user = {
      id: nextUserId++,
      username: pending.username,
      email: pending.email,
      password: pending.password,
      created_at: new Date(),
      email_verified: true
    };

    users.set(user.id, user);
    pendingRegistrations.delete(email);

    // Tạo JWT token
    const token = jwt.sign(
      { id: user.id, email: user.email },
      config.jwt.secret,
      { expiresIn: config.jwt.expiresIn }
    );

    res.status(201).json({
      success: true,
      message: 'Đăng ký thành công',
      data: {
        user: {
          id: user.id,
          username: user.username,
          email: user.email
        },
        token
      }
    });
  } catch (error) {
    console.error('❌ Lỗi xác thực OTP:', error);
    res.status(500).json({ error: error.message });
  }
});

// Đăng nhập (không thay đổi)
router.post('/login', async (req, res) => {
  try {
    const { email, password } = req.body;

    // Validate input
    if (!email || !password) {
      return res.status(400).json({ 
        error: 'Vui lòng cung cấp email và mật khẩu' 
      });
    }

    // Find user
    let user = null;
    for (let u of users.values()) {
      if (u.email === email) {
        user = u;
        break;
      }
    }

    if (!user) {
      return res.status(401).json({ error: 'Email hoặc mật khẩu không chính xác' });
    }

    // Check password
    const passwordValid = await bcrypt.compare(password, user.password);
    if (!passwordValid) {
      return res.status(401).json({ error: 'Email hoặc mật khẩu không chính xác' });
    }

    // Generate token
    const token = jwt.sign(
      { id: user.id, email: user.email },
      config.jwt.secret,
      { expiresIn: config.jwt.expiresIn }
    );

    res.json({
      success: true,
      message: 'Đăng nhập thành công',
      data: {
        user: {
          id: user.id,
          username: user.username,
          email: user.email
        },
        token
      }
    });
  } catch (error) {
    console.error('❌ Lỗi đăng nhập:', error);
    res.status(500).json({ error: error.message });
  }
});

// Đăng xuất
router.post('/logout', verifyToken, (req, res) => {
  res.json({
    success: true,
    message: 'Đăng xuất thành công'
  });
});

module.exports = router;
