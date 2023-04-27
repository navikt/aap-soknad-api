ALTER TABLE soknader
    ADD COLUMN routing boolean default false;
UPDATE soknader
set routing = false;