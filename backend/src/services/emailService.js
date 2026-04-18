const nodemailer = require('nodemailer');
const config = require('../config');

// Khởi tạo transporter
let transporter = null;
let emailServiceReady = false;

try {
  if (!process.env.GMAIL_USER || !process.env.GMAIL_PASSWORD) {
    console.warn('⚠️ GMAIL_USER hoặc GMAIL_PASSWORD chưa được cấu hình');
  } else {
    transporter = nodemailer.createTransport({
      service: 'gmail',
      auth: {
        user: process.env.GMAIL_USER,
        pass: process.env.GMAIL_PASSWORD
      }
    });
    emailServiceReady = true;
    console.log('✅ Email service khởi tạo thành công');
  }
} catch (error) {
  console.error('❌ Lỗi khởi tạo email service:', error);
}

// Lưu trữ OTP tạm thời (trong thực tế dùng Redis)
const otpStore = new Map();

module.exports = {
  // Gửi email xác nhận OTP
  sendOTPEmail: async (email) => {
    try {
      if (!emailServiceReady || !transporter) {
        console.error('❌ Email service không sẵn sàng');
        return {
          success: false,
          error: 'Dịch vụ email chưa được cấu hình. Vui lòng kiểm tra Gmail credentials'
        };
      }

      // Tạo OTP ngẫu nhiên 6 chữ số
      const otp = Math.floor(100000 + Math.random() * 900000).toString();
      const expireTime = Date.now() + 5 * 60 * 1000; // 5 phút

      // Lưu OTP
      otpStore.set(email, { otp, expireTime });
      console.log(`📝 OTP created for ${email}: ${otp}`);

      // Email content
      const mailOptions = {
        from: process.env.GMAIL_USER,
        to: email,
        subject: '🔐 Mã OTP Xác Thực Web Đọc Truyện',
        html: `
          <div style="font-family: Arial, sans-serif; max-width: 500px; margin: 0 auto;">
            <div style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 20px; text-align: center; border-radius: 8px;">
              <h2 style="margin: 0;">📚 Web Đọc Truyện</h2>
            </div>
            
            <div style="padding: 30px; background: #f5f5f5; border-radius: 8px; margin-top: -1px;">
              <h3 style="color: #333;">Xác Thực Tài Khoản</h3>
              <p style="color: #666; line-height: 1.6;">
                Vui lòng sử dụng mã OTP dưới đây để xác thực tài khoản Web Đọc Truyện của bạn. 
                Mã này sẽ hết hạn trong 5 phút.
              </p>
              
              <div style="background: white; padding: 20px; border-radius: 8px; text-align: center; margin: 20px 0;">
                <p style="font-size: 12px; color: #999; margin: 0 0 10px 0;">Mã OTP:</p>
                <h1 style="color: #667eea; font-size: 36px; letter-spacing: 5px; margin: 0;">
                  ${otp}
                </h1>
              </div>
              
              <p style="color: #666; font-size: 12px;">
                ⏱️ <strong>Điều khoản:</strong> Đừng chia sẻ mã này với bất kỳ ai
              </p>
              
              <div style="border-top: 1px solid #ddd; margin-top: 20px; padding-top: 20px; text-align: center;">
                <p style="color: #999; font-size: 11px; margin: 0;">
                  Nếu bạn không yêu cầu mã này, vui lòng bỏ qua email này.
                </p>
              </div>
            </div>
          </div>
        `
      };

      // Gửi email
      await transporter.sendMail(mailOptions);
      console.log(`✅ OTP email sent to ${email}`);

      return {
        success: true,
        message: 'Mã OTP đã được gửi đến email'
      };
    } catch (error) {
      console.error('❌ Lỗi gửi email OTP:', error);
      return {
        success: false,
        error: `Không thể gửi OTP: ${error.message}`
      };
    }
  },

  // Xác thực OTP
  verifyOTP: (email, otp) => {
    try {
      if (!otpStore.has(email)) {
        return {
          success: false,
          error: 'OTP không hợp lệ'
        };
      }

      const stored = otpStore.get(email);

      // Kiểm tra hết hạn
      if (Date.now() > stored.expireTime) {
        otpStore.delete(email);
        return {
          success: false,
          error: 'OTP đã hết hạn'
        };
      }

      // Kiểm tra OTP
      if (stored.otp !== otp) {
        return {
          success: false,
          error: 'OTP không chính xác'
        };
      }

      // Xóa OTP sau khi xác thực thành công
      otpStore.delete(email);

      return {
        success: true,
        message: 'OTP xác thực thành công'
      };
    } catch (error) {
      console.error('❌ Lỗi xác thực OTP:', error);
      return {
        success: false,
        error: 'Lỗi xác thực OTP'
      };
    }
  },

  // Lấy thông tin OTP (cho testing)
  getOTP: (email) => {
    return otpStore.get(email)?.otp || null;
  }
};
