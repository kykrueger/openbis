-----------------------------------
-- Migration 001-002
-----------------------------------

-- Create Table EVENTS

CREATE TABLE EVENTS (
  LAST_SEEN_DELETION_EVENT_ID TECH_ID NOT NULL
);

GRANT SELECT ON TABLE EVENTS TO GROUP metabol_readonly;
GRANT ALL PRIVILEGES ON TABLE EVENTS TO GROUP metabol_readwrite;

ALTER TABLE eic_ms_runs ALTER samp_id DROP NOT NULL;
ALTER TABLE fia_ms_runs ALTER samp_id DROP NOT NULL;