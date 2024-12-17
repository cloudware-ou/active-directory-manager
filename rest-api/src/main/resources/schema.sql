DROP TRIGGER IF EXISTS notify_insert ON commands;
DROP TRIGGER IF EXISTS notify_update ON commands;
DROP FUNCTION IF EXISTS notify_insert_event();
DROP FUNCTION IF EXISTS notify_update_event();


-- Create a function to send notifications for INSERT
CREATE OR REPLACE FUNCTION notify_insert_event() RETURNS trigger AS '
BEGIN
    IF NEW.command_status = ''PENDING'' THEN
        PERFORM pg_notify(''new_commands'', NEW.id::text);
    END IF;
    RETURN NEW;
END;
' LANGUAGE plpgsql;

-- Create a function to send notifications for UPDATE
CREATE OR REPLACE FUNCTION notify_update_event() RETURNS trigger AS '
BEGIN
    IF NEW.command_status = ''COMPLETED'' THEN
        PERFORM pg_notify(''completed_commands'', NEW.id::text);
    END IF;
    RETURN NEW;
END;
' LANGUAGE plpgsql;

-- Create a trigger for INSERT operations
CREATE TRIGGER notify_insert
    AFTER INSERT ON commands
    FOR EACH ROW EXECUTE FUNCTION notify_insert_event();

-- Create a trigger for UPDATE operations
CREATE TRIGGER notify_update
    AFTER UPDATE ON commands
    FOR EACH ROW EXECUTE FUNCTION notify_update_event();

NOTIFY insert_channel, 'test_message';