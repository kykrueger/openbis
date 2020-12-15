-- Disabling all available triggers

CREATE FUNCTION toggle_all_available_triggers(enable boolean) RETURNS void AS $$
DECLARE trig RECORD;
        s VARCHAR;
        modification varchar;
        toggle_trigger_sql varchar;
BEGIN
    IF enable THEN
        modification := 'ENABLE';
    ELSE
        modification := 'DISABLE';
    END IF;

    FOR trig IN
        SELECT DISTINCT event_object_table table_name, trigger_name
        FROM information_schema.triggers
    LOOP
        EXECUTE 'ALTER TABLE ' || trig.table_name || ' ' || modification || ' TRIGGER ' || trig.trigger_name;
    END LOOP;
END
$$ LANGUAGE plpgsql;

SELECT toggle_all_available_triggers(false);
