CREATE DATABASE IF NOT EXISTS crop_portal;
USE crop_portal;

CREATE TABLE roles (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(40) NOT NULL UNIQUE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE users (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  full_name VARCHAR(120) NOT NULL,
  email VARCHAR(160) NOT NULL UNIQUE,
  password VARCHAR(255) NOT NULL,
  phone VARCHAR(30),
  blocked BOOLEAN NOT NULL DEFAULT FALSE,
  refresh_token VARCHAR(255),
  password_reset_token VARCHAR(255),
  password_reset_expires_at TIMESTAMP NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_users_email (email),
  INDEX idx_users_status (blocked)
);

CREATE TABLE user_roles (
  user_id BIGINT NOT NULL,
  role_id BIGINT NOT NULL,
  PRIMARY KEY (user_id, role_id),
  FOREIGN KEY (user_id) REFERENCES users(id),
  FOREIGN KEY (role_id) REFERENCES roles(id)
);

CREATE TABLE farmers (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL UNIQUE,
  farm_location VARCHAR(255),
  farm_size_acres DOUBLE,
  primary_crop VARCHAR(100),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE doctors (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL UNIQUE,
  specialization VARCHAR(160),
  license_number VARCHAR(80),
  experience_years INT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE admins (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL UNIQUE,
  department VARCHAR(120),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE crops (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(120) NOT NULL UNIQUE,
  season VARCHAR(80),
  description TEXT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_crops_name (name)
);

CREATE TABLE image_uploads (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  farmer_id BIGINT NOT NULL,
  original_file_name VARCHAR(255),
  stored_file_name VARCHAR(255),
  content_type VARCHAR(100),
  size_bytes BIGINT,
  storage_provider VARCHAR(40) DEFAULT 'LOCAL',
  storage_path VARCHAR(500),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (farmer_id) REFERENCES farmers(id),
  INDEX idx_image_farmer (farmer_id)
);

CREATE TABLE disease_reports (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  farmer_id BIGINT NOT NULL,
  crop_id BIGINT,
  image_upload_id BIGINT NOT NULL,
  disease_name VARCHAR(160),
  confidence_score DOUBLE,
  affected_area VARCHAR(255),
  severity_level VARCHAR(40),
  recommended_pesticides TEXT,
  recommended_fertilizers TEXT,
  prevention_measures TEXT,
  expected_recovery_time VARCHAR(120),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (farmer_id) REFERENCES farmers(id),
  FOREIGN KEY (crop_id) REFERENCES crops(id),
  FOREIGN KEY (image_upload_id) REFERENCES image_uploads(id),
  INDEX idx_reports_farmer (farmer_id),
  INDEX idx_reports_disease (disease_name)
);

CREATE TABLE tickets (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  farmer_id BIGINT NOT NULL,
  doctor_id BIGINT,
  disease_report_id BIGINT,
  title VARCHAR(255) NOT NULL,
  description TEXT,
  status VARCHAR(40) NOT NULL DEFAULT 'OPEN',
  priority VARCHAR(40) DEFAULT 'MEDIUM',
  treatment_recommendation TEXT,
  prescription_document_path VARCHAR(500),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (farmer_id) REFERENCES farmers(id),
  FOREIGN KEY (doctor_id) REFERENCES doctors(id),
  FOREIGN KEY (disease_report_id) REFERENCES disease_reports(id),
  INDEX idx_tickets_status (status),
  INDEX idx_tickets_farmer (farmer_id),
  INDEX idx_tickets_doctor (doctor_id)
);

CREATE TABLE ticket_messages (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  ticket_id BIGINT NOT NULL,
  sender_id BIGINT NOT NULL,
  message TEXT NOT NULL,
  read_receipt BOOLEAN DEFAULT FALSE,
  read_at TIMESTAMP NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (ticket_id) REFERENCES tickets(id),
  FOREIGN KEY (sender_id) REFERENCES users(id),
  INDEX idx_messages_ticket (ticket_id)
);

CREATE TABLE notifications (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  title VARCHAR(160),
  body TEXT,
  read_flag BOOLEAN DEFAULT FALSE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id),
  INDEX idx_notifications_user (user_id)
);

CREATE TABLE audit_logs (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  actor_id BIGINT,
  action VARCHAR(120),
  resource_type VARCHAR(120),
  resource_id BIGINT,
  ip_address VARCHAR(80),
  metadata TEXT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (actor_id) REFERENCES users(id),
  INDEX idx_audit_actor (actor_id)
);
