-- Add import-related columns to stories table
ALTER TABLE stories ADD COLUMN IF NOT EXISTS external_id VARCHAR(255) UNIQUE;
ALTER TABLE stories ADD COLUMN IF NOT EXISTS is_public BOOLEAN DEFAULT TRUE;

-- Create index for external_id for faster lookups
CREATE INDEX IF NOT EXISTS idx_stories_external_id ON stories(external_id);
CREATE INDEX IF NOT EXISTS idx_stories_source ON stories(source);
CREATE INDEX IF NOT EXISTS idx_stories_is_public ON stories(is_public);

-- Verify columns were added
SELECT column_name, data_type FROM information_schema.columns 
WHERE table_name = 'stories' AND column_name IN ('external_id', 'is_public');
