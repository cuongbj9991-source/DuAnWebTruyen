import axios from 'axios';

// API Gateway or direct service URLs
// Railway production URL as default
const STORY_SERVICE = process.env.REACT_APP_STORY_SERVICE || 'https://duanwebtruyen-production.up.railway.app/api';
const USER_SERVICE = process.env.REACT_APP_USER_SERVICE || 'https://duanwebtruyen-production.up.railway.app/api';

const createClient = (baseURL, silent = false) => {
  const client = axios.create({
    baseURL,
    headers: {
      'Content-Type': 'application/json'
    }
  });

  // Add token to requests
  client.interceptors.request.use((config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  });

  // Suppress network errors and 404s for optional services in production
  if (silent) {
    client.interceptors.response.use(
      response => response,
      error => {
        // Suppress connection refused errors (optional services not deployed)
        if (error.code === 'ERR_NETWORK' || error.code === 'ECONNREFUSED') {
          return Promise.reject(error);
        }
        // Suppress 404 errors for favorites and reading-progress endpoints
        if (error.response?.status === 404) {
          console.warn('Optional endpoint not found:', error.config?.url);
          return { data: null };
        }
        return Promise.reject(error);
      }
    );
  }

  return client;
};

const storyClient = createClient(STORY_SERVICE);
const userClient = createClient(USER_SERVICE, true); // Silent mode for optional service

export const storyService = {
  // Lấy tất cả truyện
  getAllStories: (params = {}) => storyClient.get('/stories', { params }),

  // Tìm kiếm truyện
  searchStories: (keyword, params = {}) => 
    storyClient.get('/stories/search', { params: { keyword, ...params } }),

  // Lọc truyện
  filterStories: (filters = {}, params = {}) =>
    storyClient.get('/stories/filter', { params: { ...filters, ...params } }),

  // Lấy các tùy chọn lọc
  getFilterOptions: () => storyClient.get('/stories/filter-options'),

  // Lấy truyện theo ID
  getStoryById: (id) => storyClient.get(`/stories/${id}`)
};

export const chapterService = {
  // Lấy tất cả chương của truyện
  getChaptersByStoryId: (storyId) => 
    storyClient.get(`/chapters/story/${storyId}`),

  // Lấy chương theo số thứ tự
  getChapterByNumber: (storyId, chapterNumber) =>
    storyClient.get(`/chapters/story/${storyId}/chapter/${chapterNumber}`),

  // Lấy chương theo ID
  getChapterById: (id) => storyClient.get(`/chapters/${id}`)
};

export const userService = {
  // Lấy hồ sơ người dùng
  getUserProfile: (id) => userClient.get(`/users/${id}`),

  // Cập nhật hồ sơ người dùng
  updateUserProfile: (id, data) => userClient.put(`/users/${id}`, data),

  // Lấy lịch sử đọc
  getReadingHistory: (id) => userClient.get(`/users/${id}/reading-history`)
};

export const favoriteService = {
  // Lấy danh sách yêu thích
  getFavorites: () => userClient.get('/favorites'),

  // Kiểm tra xem có yêu thích không
  checkFavorite: (storyId) => userClient.get(`/favorites/${storyId}`),

  // Thêm vào yêu thích
  addToFavorites: (storyId) => userClient.post(`/favorites/${storyId}`),

  // Xóa khỏi yêu thích
  removeFromFavorites: (storyId) => userClient.delete(`/favorites/${storyId}`)
};

export const commentService = {
  // Lấy bình luận của truyện
  getComments: (storyId, params = {}) => 
    storyClient.get(`/comments/story/${storyId}`, { params }),

  // Tạo bình luận
  createComment: (data) => storyClient.post('/comments', data),

  // Cập nhật bình luận
  updateComment: (id, data) => storyClient.put(`/comments/${id}`, data),

  // Xóa bình luận
  deleteComment: (id) => storyClient.delete(`/comments/${id}`)
};

export const readingProgressService = {
  // Lấy tiến độ đọc của story
  getReadingProgress: (storyId) =>
    userClient.get(`/reading-progress/story/${storyId}`),

  // Cập nhật tiến độ đọc
  updateReadingProgress: (storyId, data) =>
    userClient.put(`/reading-progress/story/${storyId}`, data)
};

export const authService = {
  // Đăng ký
  register: (username, email, password) =>
    userClient.post('/auth/register', { username, email, password }),

  // Đăng nhập
  login: (email, password) =>
    userClient.post('/auth/login', { email, password })
};

export const uploadService = {
  // Tạo tải lên truyện mới
  createUpload: (data) => storyClient.post('/uploads', data),

  // Lấy các truyện của tôi
  getMyUploads: (page = 0, size = 10) =>
    storyClient.get('/uploads/my-uploads', { params: { page, size } }),

  // Lấy truyện đã phát hành
  getPublishedStories: (page = 0, size = 12) =>
    storyClient.get('/uploads/published', { params: { page, size } }),

  // Tìm kiếm trong truyện đã phát hành
  searchPublishedStories: (keyword, page = 0, size = 12) =>
    storyClient.get('/uploads/published/search', { params: { keyword, page, size } }),

  // Lấy upload theo ID
  getUploadById: (id) => storyClient.get(`/uploads/${id}`),

  // Cập nhật upload
  updateUpload: (id, data) => storyClient.put(`/uploads/${id}`, data),

  // Xóa upload
  deleteUpload: (id) => storyClient.delete(`/uploads/${id}`),

  // Duyệt upload (admin)
  approveUpload: (id) => storyClient.post(`/uploads/${id}/approve`),

  // Từ chối upload (admin)
  rejectUpload: (id, reason) => storyClient.post(`/uploads/${id}/reject`, { reason }),

  // Phát hành upload
  publishUpload: (id) => storyClient.post(`/uploads/${id}/publish`),

  // Lấy danh sách chờ duyệt (admin)
  getPendingReviews: (page = 0, size = 10) =>
    storyClient.get('/uploads/admin/pending-reviews', { params: { page, size } })
};

export const multiSourceSearchService = {
  // Tìm kiếm trên tất cả các nguồn
  searchAllSources: (keyword, page = 1) =>
    storyClient.get('/external-sources/search', { params: { keyword, page } }),

  // Lấy các sách được đề xuất
  getRecommended: () =>
    storyClient.get('/external-sources/recommended')
};

const apiServices = { storyService, chapterService, userService, favoriteService, commentService, readingProgressService, authService, uploadService, multiSourceSearchService };

export default apiServices;
