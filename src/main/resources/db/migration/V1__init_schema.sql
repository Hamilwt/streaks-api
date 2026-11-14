CREATE TABLE users (
    id UUID PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(255),
    balance INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT uk_users_email UNIQUE (email)
);

CREATE TABLE habits (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    user_id UUID NOT NULL,
    CONSTRAINT fk_habits_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE check_ins (
    id UUID PRIMARY KEY,
    habit_id UUID NOT NULL,
    check_in_date DATE NOT NULL,
    CONSTRAINT fk_check_ins_habit FOREIGN KEY (habit_id) REFERENCES habits (id),
    CONSTRAINT uk_check_ins_habit_check_in_date UNIQUE (habit_id, check_in_date)
);
