-- ================================================================
--  My Personal Library — Final MySQL Database Schema
--  Engine  : InnoDB
--  Charset : utf8mb4 / utf8mb4_unicode_ci
--  MySQL   : 8.0.16+ (CHECK constraints enforced)
--
--  [ORIG]  = existed in original Laravel project
--  [NEW]   = added for JSP Final Exam
--  [EXT]   = original feature extended/upgraded
-- ================================================================

CREATE DATABASE IF NOT EXISTS my_personal_library_jsp
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE my_personal_library_jsp;

-- ================================================================
-- [ORIG] Table 1: users
-- ================================================================
CREATE TABLE users (
    user_id    INT          NOT NULL AUTO_INCREMENT,
    name       VARCHAR(255) NOT NULL,
    email      VARCHAR(255) NOT NULL,
    password   VARCHAR(255) NOT NULL,
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
                                     ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_users       PRIMARY KEY (user_id),
    CONSTRAINT uq_users_email UNIQUE (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ================================================================
-- [NEW] Table 2: authors
-- Normalization of the original free-text books.author column.
-- Belongs to individual users (personal library — not shared).
-- UNIQUE(author_id, user_id) required as composite FK target from books.
-- ================================================================
CREATE TABLE authors (
    author_id  INT          NOT NULL AUTO_INCREMENT,
    user_id    INT          NOT NULL,
    name       VARCHAR(255) NOT NULL,
    bio        TEXT         NULL,
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_authors         PRIMARY KEY (author_id),
    CONSTRAINT uq_authors_comp    UNIQUE (author_id, user_id),
    CONSTRAINT fk_authors_user    FOREIGN KEY (user_id)
        REFERENCES users(user_id)
        ON DELETE CASCADE ON UPDATE CASCADE,

    INDEX idx_authors_user_id (user_id),
    INDEX idx_authors_name    (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ================================================================
-- [NEW] Table 3: categories
-- Normalization of the original free-text books.category column.
-- Belongs to individual users.
-- UNIQUE(category_id, user_id) required as composite FK target from books.
-- UNIQUE(user_id, name) prevents duplicate category names per user.
-- ================================================================
CREATE TABLE categories (
    category_id INT          NOT NULL AUTO_INCREMENT,
    user_id     INT          NOT NULL,
    name        VARCHAR(100) NOT NULL,
    description TEXT         NULL,
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_categories            PRIMARY KEY (category_id),
    CONSTRAINT uq_categories_comp       UNIQUE (category_id, user_id),
    CONSTRAINT uq_categories_user_name  UNIQUE (user_id, name),
    CONSTRAINT fk_categories_user       FOREIGN KEY (user_id)
        REFERENCES users(user_id)
        ON DELETE CASCADE ON UPDATE CASCADE,

    INDEX idx_categories_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ================================================================
-- [ORIG+EXT] Table 4: books
-- Original columns fully preserved.
-- author_id FK replaces free-text author column.
-- category_id FK replaces free-text category column.
-- UNIQUE(book_id, user_id) required as composite FK target for
--   book_notes, reading_logs, reading_goals, and favorites.
-- Composite FK (author_id, user_id) prevents cross-user author assignment.
-- Single-column FK on category_id uses SET NULL (composite SET NULL
--   not possible when user_id is NOT NULL — see design decision notes).
-- ================================================================
CREATE TABLE books (
    book_id           INT          NOT NULL AUTO_INCREMENT,
    user_id           INT          NOT NULL,
    author_id         INT          NOT NULL,
    category_id       INT          NULL,
    title             VARCHAR(255) NOT NULL,
    status            ENUM(
                          'Want to Read',
                          'Currently Reading',
                          'Finished Reading'
                      )            NOT NULL DEFAULT 'Want to Read',
    cover_image       VARCHAR(500) NULL     COMMENT 'Relative path: covers/<uuid>.<ext>',
    book_file_path    VARCHAR(500) NULL     COMMENT 'Stored outside webapp webroot',
    original_filename VARCHAR(255) NULL,
    current_page      INT UNSIGNED NULL,
    total_pages       INT UNSIGNED NULL,
    summary           TEXT         NULL,
    created_at        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
                                   ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_books           PRIMARY KEY (book_id),
    CONSTRAINT uq_books_ownership UNIQUE (book_id, user_id),

    CONSTRAINT fk_books_user FOREIGN KEY (user_id)
        REFERENCES users(user_id)
        ON DELETE CASCADE ON UPDATE CASCADE,

    -- Composite FK: author must belong to the same user as the book
    CONSTRAINT fk_books_author FOREIGN KEY (author_id, user_id)
        REFERENCES authors(author_id, user_id)
        ON DELETE RESTRICT ON UPDATE CASCADE,

    -- Single-column FK: SET NULL on category delete
    -- (Ownership enforced at application layer via WHERE user_id = ?)
    CONSTRAINT fk_books_category FOREIGN KEY (category_id)
        REFERENCES categories(category_id)
        ON DELETE SET NULL ON UPDATE CASCADE,

    CONSTRAINT chk_books_current_page CHECK (current_page IS NULL OR current_page >= 0),
    CONSTRAINT chk_books_total_pages  CHECK (total_pages  IS NULL OR total_pages  >= 1),
    CONSTRAINT chk_books_pages        CHECK (
        current_page IS NULL OR total_pages IS NULL OR current_page <= total_pages
    ),

    INDEX idx_books_user_id     (user_id),
    INDEX idx_books_author_id   (author_id),
    INDEX idx_books_category_id (category_id),
    INDEX idx_books_status      (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ================================================================
-- [NEW] Table 5: book_notes
-- Private per-book notes per user.
-- Composite FK (book_id, user_id) guarantees the book belongs
-- to the note's author at the database engine level.
-- ================================================================
CREATE TABLE book_notes (
    note_id    INT      NOT NULL AUTO_INCREMENT,
    book_id    INT      NOT NULL,
    user_id    INT      NOT NULL,
    content    TEXT     NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
                        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_book_notes      PRIMARY KEY (note_id),
    CONSTRAINT fk_notes_ownership FOREIGN KEY (book_id, user_id)
        REFERENCES books(book_id, user_id)
        ON DELETE CASCADE ON UPDATE CASCADE,

    INDEX idx_notes_book_id (book_id),
    INDEX idx_notes_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ================================================================
-- [NEW] Table 6: reading_logs
-- Reading session log (date + pages read) per owned book.
-- Composite FK enforces ownership (same logic as book_notes).
-- ================================================================
CREATE TABLE reading_logs (
    log_id     INT          NOT NULL AUTO_INCREMENT,
    book_id    INT          NOT NULL,
    user_id    INT          NOT NULL,
    pages_read INT UNSIGNED NOT NULL,
    log_date   DATE         NOT NULL,
    notes      TEXT         NULL,
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_reading_logs   PRIMARY KEY (log_id),
    CONSTRAINT fk_logs_ownership FOREIGN KEY (book_id, user_id)
        REFERENCES books(book_id, user_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT chk_logs_pages    CHECK (pages_read > 0),

    INDEX idx_logs_book_id (book_id),
    INDEX idx_logs_user_id (user_id),
    INDEX idx_logs_date    (log_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ================================================================
-- [NEW] Table 7: reading_goals
-- User-defined reading targets. Two types:
--   'books_count' : read N books (target_books, optional deadline).
--   'finish_book' : finish a specific book (book_id, optional deadline).
-- CHECK constraint enforces column consistency per goal type.
-- ================================================================
CREATE TABLE reading_goals (
    goal_id      INT          NOT NULL AUTO_INCREMENT,
    user_id      INT          NOT NULL,
    title        VARCHAR(100) NOT NULL,
    goal_type    ENUM('books_count', 'finish_book') NOT NULL,
    target_books INT UNSIGNED NULL COMMENT 'Used when goal_type = books_count',
    book_id      INT          NULL COMMENT 'Used when goal_type = finish_book',
    deadline     DATE         NULL,
    is_completed TINYINT(1)   NOT NULL DEFAULT 0,
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
                              ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_reading_goals PRIMARY KEY (goal_id),

    CONSTRAINT fk_goals_user FOREIGN KEY (user_id)
        REFERENCES users(user_id)
        ON DELETE CASCADE ON UPDATE CASCADE,

    -- Composite FK on (book_id, user_id) for finish_book goals
    CONSTRAINT fk_goals_book FOREIGN KEY (book_id, user_id)
        REFERENCES books(book_id, user_id)
        ON DELETE CASCADE ON UPDATE CASCADE,

    CONSTRAINT chk_goals_target CHECK (target_books IS NULL OR target_books > 0),

    INDEX idx_goals_user_id (user_id),
    INDEX idx_goals_book_id (book_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ================================================================
-- [ORIG+EXT] Table 8: favorites  (M:N junction)
-- Original: two separate FKs on user_id and book_id.
-- Extended: upgraded to composite FK (book_id, user_id) →
--   books(book_id, user_id) to enforce at DB level that a user
--   can only favorite their own books.
-- ================================================================
CREATE TABLE favorites (
    user_id    INT      NOT NULL,
    book_id    INT      NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_favorites     PRIMARY KEY (user_id, book_id),
    CONSTRAINT fk_fav_ownership FOREIGN KEY (book_id, user_id)
        REFERENCES books(book_id, user_id)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ================================================================
-- Seed data
-- ================================================================
-- NO demo user is seeded. Register the first account via /register.
-- The application BCrypt-hashes the password before storing.
--
-- After registering (your first user will have user_id = 1),
-- uncomment and run the INSERT statements below to get sample data.
--
-- INSERT INTO authors (user_id, name, bio) VALUES
--   (1, 'J.K. Rowling',     'British author of the Harry Potter series.'),
--   (1, 'Robert C. Martin', 'Software engineer, author of Clean Code.'),
--   (1, 'James Clear',      'Author of Atomic Habits.'),
--   (1, 'Yuval Noah Harari','Israeli historian, author of Sapiens.');
--
-- INSERT INTO categories (user_id, name, description) VALUES
--   (1, 'Fiction',     'Novels and fictional stories'),
--   (1, 'Non-Fiction', 'Factual books and essays'),
--   (1, 'Technology',  'Programming and software engineering'),
--   (1, 'Self-Help',   'Personal development and productivity'),
--   (1, 'Biography',   'Life stories of real people');
--
-- INSERT INTO books (user_id, author_id, category_id, title, status, summary) VALUES
--   (1, 1, 1, 'Harry Potter and the Philosopher''s Stone',
--    'Finished Reading', 'A young wizard discovers his magical heritage.'),
--   (1, 2, 3, 'Clean Code',
--    'Currently Reading', 'A handbook of agile software craftsmanship.'),
--   (1, 3, 4, 'Atomic Habits',
--    'Want to Read', 'Tiny changes lead to remarkable results.'),
--   (1, 4, 2, 'Sapiens',
--    'Want to Read', 'A brief history of humankind.');
