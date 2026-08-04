CREATE TABLE user_accounts (
    id UUID PRIMARY KEY,
    full_name VARCHAR(120) NOT NULL,
    email VARCHAR(180) NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE candidate_profiles (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL UNIQUE REFERENCES user_accounts(id) ON DELETE CASCADE,
    headline VARCHAR(180),
    desired_role VARCHAR(120),
    location VARCHAR(120),
    desired_seniority VARCHAR(30),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE candidate_skills (
    profile_id UUID NOT NULL REFERENCES candidate_profiles(id) ON DELETE CASCADE,
    skill VARCHAR(80) NOT NULL,
    PRIMARY KEY (profile_id, skill)
);

CREATE TABLE candidate_work_models (
    profile_id UUID NOT NULL REFERENCES candidate_profiles(id) ON DELETE CASCADE,
    work_model VARCHAR(30) NOT NULL,
    PRIMARY KEY (profile_id, work_model)
);

CREATE TABLE job_postings (
    id UUID PRIMARY KEY,
    title VARCHAR(160) NOT NULL,
    company VARCHAR(120) NOT NULL,
    description TEXT NOT NULL,
    source_url VARCHAR(600) UNIQUE,
    location VARCHAR(120) NOT NULL,
    work_model VARCHAR(30) NOT NULL,
    seniority VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL,
    posted_at DATE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE job_required_skills (
    job_id UUID NOT NULL REFERENCES job_postings(id) ON DELETE CASCADE,
    skill VARCHAR(80) NOT NULL,
    PRIMARY KEY (job_id, skill)
);

CREATE TABLE job_desirable_skills (
    job_id UUID NOT NULL REFERENCES job_postings(id) ON DELETE CASCADE,
    skill VARCHAR(80) NOT NULL,
    PRIMARY KEY (job_id, skill)
);

CREATE TABLE job_matches (
    id UUID PRIMARY KEY,
    profile_id UUID NOT NULL REFERENCES candidate_profiles(id) ON DELETE CASCADE,
    job_id UUID NOT NULL REFERENCES job_postings(id) ON DELETE CASCADE,
    score INTEGER NOT NULL CHECK (score BETWEEN 0 AND 100),
    recommendation VARCHAR(30) NOT NULL,
    explanation VARCHAR(1000) NOT NULL,
    analyzed_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_job_match_profile_job UNIQUE (profile_id, job_id)
);

CREATE TABLE match_skills (
    match_id UUID NOT NULL REFERENCES job_matches(id) ON DELETE CASCADE,
    skill VARCHAR(80) NOT NULL,
    PRIMARY KEY (match_id, skill)
);

CREATE TABLE match_missing_required (
    match_id UUID NOT NULL REFERENCES job_matches(id) ON DELETE CASCADE,
    skill VARCHAR(80) NOT NULL,
    PRIMARY KEY (match_id, skill)
);

CREATE TABLE match_missing_desirable (
    match_id UUID NOT NULL REFERENCES job_matches(id) ON DELETE CASCADE,
    skill VARCHAR(80) NOT NULL,
    PRIMARY KEY (match_id, skill)
);

CREATE INDEX idx_job_postings_status ON job_postings(status);
CREATE INDEX idx_job_matches_profile_score ON job_matches(profile_id, score DESC);
