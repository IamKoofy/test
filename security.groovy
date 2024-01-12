Connect to PostgreSQL:

Connect to PostgreSQL using a tool such as psql or any PostgreSQL client.
bash
Copy code
psql -h <host> -U <admin_user> -d <database>
Replace <host>, <admin_user>, and <database> with your actual values.

Grant Permissions:

Once connected, grant the necessary permissions to the user. For a Nexus Repository Manager, you typically need to grant privileges like SELECT, INSERT, UPDATE, DELETE, and USAGE on the relevant schema.
sql
Copy code
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO <your_user>;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO <your_user>;
Replace <your_user> with the actual username you're using for Nexus Repository Manager.

Set Default Privileges (Optional):

You might want to set default privileges so that future objects created in the schema automatically inherit the necessary permissions.
sql
Copy code
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO <your_user>;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT USAGE, SELECT ON SEQUENCES TO <your_user>;
Grant Connect (if necessary):

If your Nexus Repository Manager connects to the database, you need to grant the user the CONNECT privilege.
sql
Copy code
GRANT CONNECT ON DATABASE <database> TO <your_user>;
