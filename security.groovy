1. Provision the External PostgreSQL Database:

Create a new PostgreSQL database instance in OpenShift, ensuring it uses UTF8 encoding.
Obtain the database connection details (hostname, port, username, password, database name).
2. Download the Database Migrator Utility:

Retrieve the Database Migrator utility from the Sonatype Downloads page: https://help.sonatype.com/repomanager3/download
Ensure you're using OpenJDK 8 (Oracle JDK is not compatible).
3. Back Up Existing Data (Optional):

Perform a full backup of your current Nexus data using your preferred methods.
4. Shut Down Nexus Repository:

Stop the Nexus server running on your VM.
5. Run the Database Migrator:

Navigate to the $data-dir/db directory of your Nexus installation.
Execute the migrator utility, providing the following information:
Path to the Nexus data directory
JDBC URL for the external PostgreSQL database
Database username and password
Example command: java -jar nexus-migrator.jar -data-dir /opt/sonatype-work/nexus3 -jdbc-url jdbc:postgresql://<hostname>:<port>/<database_name> -username <username> -password <password>
6. Configure Nexus for External Database:

Create the <data-dir>/etc/nexus.properties file if it doesn't exist.
Add the following properties:
nexus.datastore.enabled=true
nexus.datastore.provider=postgresql
nexus.datastore.url=<jdbc_url>
nexus.datastore.user=<username>
nexus.datastore.password=<password>
7. Restart Nexus Repository:

Start the Nexus server on your VM.
It will now connect to the external PostgreSQL database in OpenShift.
Additional Considerations:

Firewall Rules: Ensure firewall rules allow communication between your VM and the PostgreSQL database in OpenShift.
Network Connectivity: Verify network connectivity between the VM and the PostgreSQL database.
OpenShift Configuration: Review OpenShift documentation for any specific configuration requirements related to external database connections.
Nexus License: If using Nexus Repository Pro, ensure your license is valid and accessible.
Upgrade Considerations: If using an older Nexus version, upgrade to the latest release before migration for compatibility and support.
Troubleshooting:

Consult Sonatype documentation for troubleshooting guidance: https://help.sonatype.com/repomanager3
Contact Sonatype support if you encounter issues.
