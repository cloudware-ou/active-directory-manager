DROP TRIGGER IF EXISTS notify_insert ON commands;
DROP TRIGGER IF EXISTS notify_update ON commands;
DROP TRIGGER IF EXISTS notify_one_time_keys_alice ON one_time_keys;
DROP TRIGGER IF EXISTS notify_one_time_keys_bob ON one_time_keys;
DROP FUNCTION IF EXISTS notify_new_commands();
DROP FUNCTION IF EXISTS notify_completed_commands();
drop function if exists notify_one_time_keys_bob();
drop function if exists notify_one_time_keys_alice();


-- Create a function to send notifications for INSERT
CREATE OR REPLACE FUNCTION notify_new_commands() RETURNS trigger AS '
BEGIN
    IF NEW.command_status = ''PENDING'' THEN
        PERFORM pg_notify(''new_commands'', NEW.id::text);
    END IF;
    RETURN NEW;
END;
' LANGUAGE plpgsql;

-- Create a function to send notifications for UPDATE
CREATE OR REPLACE FUNCTION notify_completed_commands() RETURNS trigger AS '
BEGIN
    IF NEW.command_status = ''COMPLETED'' THEN
        PERFORM pg_notify(''completed_commands'', NEW.id::text);
    END IF;
    RETURN NEW;
END;
' LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION notify_one_time_keys_alice() RETURNS trigger AS '
BEGIN
    PERFORM pg_notify(''one_time_keys_alice'', NEW.id::text);
    RETURN NEW;
END;
' LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION notify_one_time_keys_bob() RETURNS trigger AS '
BEGIN
    PERFORM pg_notify(''one_time_keys_bob'', NEW.id::text);
    RETURN NEW;
END;
' LANGUAGE plpgsql;

-- Create a trigger for INSERT operations
CREATE TRIGGER notify_insert
    AFTER INSERT ON commands
    FOR EACH ROW EXECUTE FUNCTION notify_new_commands();

-- Create a trigger for UPDATE operations
CREATE TRIGGER notify_update
    AFTER UPDATE ON commands
    FOR EACH ROW EXECUTE FUNCTION notify_completed_commands();

CREATE TRIGGER notify_one_time_keys_alice
    AFTER insert ON one_time_keys
    FOR EACH ROW EXECUTE FUNCTION notify_one_time_keys_alice();

CREATE TRIGGER notify_one_time_keys_bob
    AFTER update ON one_time_keys
    FOR EACH ROW EXECUTE FUNCTION notify_one_time_keys_bob();