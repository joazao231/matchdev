CREATE TABLE job_applications (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES user_accounts(id) ON DELETE CASCADE,
    job_id UUID NOT NULL REFERENCES job_postings(id) ON DELETE CASCADE,
    status VARCHAR(30) NOT NULL,
    notes VARCHAR(1000),
    applied_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_job_application_user_job UNIQUE (user_id, job_id)
);

CREATE INDEX idx_job_applications_user_status
    ON job_applications(user_id, status, updated_at DESC);
