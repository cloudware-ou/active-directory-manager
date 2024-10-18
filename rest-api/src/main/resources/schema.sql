DROP FUNCTION IF EXISTS notify_new_command();
DROP TRIGGER IF EXISTS trigger_notify_new_command ON commands;

-- Create or replace the function
CREATE OR REPLACE FUNCTION notify_new_command()
RETURNS trigger AS '
BEGIN
  IF NEW.command_status = ''PENDING'' THEN
    PERFORM pg_notify(''new_command'', NEW.id::text);
END IF;
RETURN NEW;
END;
' LANGUAGE plpgsql;

-- Create the trigger
CREATE TRIGGER trigger_notify_new_command
    AFTER INSERT ON commands
    FOR EACH ROW
    EXECUTE FUNCTION notify_new_command();


