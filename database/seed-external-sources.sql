-- Seed script for popular stories from external sources
-- This script populates the database with sample stories from Project Gutenberg, OpenLibrary, and MangaDex
-- Run after creating the database schema

-- Popular Gutenberg books
INSERT INTO stories (title, title_alt, description, author, genre, type, status, source, cover_url, views_total, likes, comments_count, rating, rating_count) VALUES
('Pride and Prejudice', 'Kiêu hãnh và Định kiến', 'A romantic novel of manners written by Jane Austen in 1813. The novel follows the character development of Elizabeth Bennet, the protagonist of the book, who learns about the repercussions of hasty judgments and comes to appreciate the difference between superficial goodness and actual goodness.', 'Jane Austen', 'Romance', 'Lãng Mạn', 'completed', 'Gutenberg', 'https://covers.openlibrary.org/b/id/7725815-M.jpg', 0, 0, 0, 0, 0),
('Jane Eyre', 'Jane Eyre', 'A Gothic novel by Charlotte Brontë, published in 1847 under the pseudonym "Currer Bell". The novel follows the experiences of its protagonist, including her growth to adulthood, and her love for Mr. Rochester, the brooding master of Thornfield Hall.', 'Charlotte Brontë', 'Romance', 'Lãng Mạn', 'completed', 'Gutenberg', 'https://covers.openlibrary.org/b/id/7725822-M.jpg', 0, 0, 0, 0, 0),
('Wuthering Heights', 'Wuthering Heights', 'A wild, passionate story of the intense and almost demonic love between Cathy and Heathcliff. Set in the isolated Yorkshire moors, this novel is a masterpiece of atmosphere and an irresistible story of love and revenge.', 'Emily Brontë', 'Romance', 'Lãng Mạn', 'completed', 'Gutenberg', 'https://covers.openlibrary.org/b/id/7725815-M.jpg', 0, 0, 0, 0, 0),
('The Great Gatsby', 'Đại Gia Gatsby', 'A classic of American literature set in the Jazz Age. It documents moral and social decay. Fitzgerald finds his prose style and satirical touches to be at their best here. It is a masterpiece of our times.', 'F. Scott Fitzgerald', 'Văn học', 'Tiểu thuyết', 'completed', 'Gutenberg', 'https://covers.openlibrary.org/b/id/7888889-M.jpg', 0, 0, 0, 0, 0),
('Sherlock Holmes Adventures', 'Những cuộc phiêu lưu của Sherlock Holmes', 'A collection of short stories featuring the brilliant detective Sherlock Holmes and his faithful companion Dr. Watson. Stories include "A Scandal in Bohemia" and "The Red-Headed League".', 'Arthur Conan Doyle', 'Phiêu Lưu', 'Trinh Thám', 'completed', 'Gutenberg', 'https://covers.openlibrary.org/b/id/7725815-M.jpg', 0, 0, 0, 0, 0),
('Alice''s Adventures in Wonderland', 'Alice ở Xứ Sở Diệu Kỳ', 'Alice falls through a rabbit hole into a whimsical Wonderland filled with peculiar creatures and absurd situations. This beloved children''s classic combines wordplay, logic games, and satirical commentary.', 'Lewis Carroll', 'Viễn Tưởng', 'Trẻ em', 'completed', 'Gutenberg', 'https://covers.openlibrary.org/b/id/7725815-M.jpg', 0, 0, 0, 0, 0),
('Frankenstein', 'Frankenstein', 'A Gothic novel about Victor Frankenstein, a young scientist who becomes obsessed with the idea of creating life. Horrified by his creation, he abandons it, and the lonely creature seeks revenge.', 'Mary Wollstonecraft Shelley', 'Kinh Dị', 'Kinh Dị', 'completed', 'Gutenberg', 'https://covers.openlibrary.org/b/id/7888891-M.jpg', 0, 0, 0, 0, 0),
('Moby Dick', 'Con Cá Voi Trắng', 'A towering masterpiece of world literature, Moby Dick relates the epic voyage of the whaling ship Pequod and its fanatical captain Ahab''s quest for revenge on the white whale that mutilated him.', 'Herman Melville', 'Phiêu Lưu', 'Phiêu Lưu', 'completed', 'Gutenberg', 'https://covers.openlibrary.org/b/id/7888892-M.jpg', 0, 0, 0, 0, 0),
('The Count of Monte Cristo', 'Bá Tước Monte Cristo', 'An adventure novel that tells the story of Edmond Dantès, a young sailor who is wrongly imprisoned. After escaping and discovering a great treasure, he seeks revenge on those who betrayed him.', 'Alexandre Dumas', 'Phiêu Lưu', 'Lãng Mạn', 'completed', 'Gutenberg', 'https://covers.openlibrary.org/b/id/7725815-M.jpg', 0, 0, 0, 0, 0),
('The Three Musketeers', 'Ba Người Lính Ngự Vệ', 'A novel set in 17th century France about a young man d''Artagnan and his encounters with three musketeers. Together they become heroes in the court of King Louis XIII.', 'Alexandre Dumas', 'Phiêu Lưu', 'Phiêu Lưu', 'completed', 'Gutenberg', 'https://covers.openlibrary.org/b/id/7888893-M.jpg', 0, 0, 0, 0, 0);

-- Add a few sample user-uploaded stories as examples (status pending_review by default)
INSERT INTO story_uploads (user_id, title, author, description, genre, type, status, content, is_public, is_approved) 
VALUES 
(1, 'My First Story', 'Unknown Author', 'This is an example user-uploaded story that is pending approval', 'Adventure', 'original', 'pending_review', 'Chapter 1: The Beginning\n\nOnce upon a time...', false, false);

-- Optional: Add sample chapters for Gutenberg stories (requires knowing story IDs)
-- This would need to be done programmatically or with actual API integration

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_stories_source ON stories(source) WHERE source = 'Gutenberg';
CREATE INDEX IF NOT EXISTS idx_stories_published ON stories(status, created_at DESC) WHERE status = 'completed';
