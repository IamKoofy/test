CI (Build): GitHub Actions (already in use)

CD (Release/Deployment): Ansible Automation Platform

✨ Why Choose AAP Over GitHub Actions for Releases

1. ⚙️ Native Infrastructure Automation and Deployment Modules

AAP provides built-in modules for IIS, Windows, Linux, AWS, Azure, and network devices.

Examples:

win_iis_webapplication, win_iis_webapppool, win_copy, win_unzip for IIS deployments.

ec2, azure_rm modules for cloud automation.

Eliminates need to script or reinvent complex tasks in shell or PowerShell.

2. 🧩 Replaces Deployment Groups with Inventories

Inventories in AAP replace Azure DevOps Deployment Groups.

Options:

Static Inventories: Manually manage host lists.

Dynamic Inventories: Sync with AWS, Azure, CMDBs, etc.

Smart Inventories: Filter hosts based on tags/metadata.

3. 🔁 Role-Based Targeting and Isolation

Create environments like team-a-dev, team-b-prod and manage deployments per team.

Use group_vars, host_vars, or dynamic inventory filters for configuration.

4. 🌐 Centralized Release Control

Centralized visibility over all team releases.

RBAC (role-based access control) to restrict access.

Logging and audit trails built-in.

5. 🔹 Workflow Templates and Surveys

Use surveys to allow teams to pick:

Target environment (inventory group)

Artifact version (fetched from GitHub Actions)

Config flags (e.g., full or partial deploy)

Fully interactive release management, no need to hardcode inputs.

6. ✅ Robust Windows Support (for IIS Releases)

Modules like win_iis_webapplication, win_iis_webapppool, win_copy and win_unzip support:

App pool config

App deployment

JSON file overrides (e.g., appsettings.json)

App pool recycle/start/stop

7. ⚖️ Separation of Concerns

Keeps CI (build) logic separate from CD (release) logic.

Easier governance and audit in large orgs.

Release access can be granted without access to build pipelines or source code.

8. ⚡ Performance and Scalability

AAP supports scaling with execution nodes.

Reuse playbooks across hundreds of environments without duplicating workflows.

9. 🔍 Centralized Logs and Compliance

Unified logs per host per job.

Useful for troubleshooting failed releases.

Inbuilt retention and export capabilities.

📅 Common Use Cases We Can Solve with AAP

IIS-based .NET Web App deployments

App Pool creation/start/stop

Artifact download from GitHub CI builds

Environment-specific configurations

Partial/conditional deployments

On-prem and cloud target mix

✍️ Developer/Operator Workflow

Build happens via GitHub Actions (CI)

Artifact is uploaded via upload-artifact

Operator triggers release in AAP:

Selects environment from dropdown (survey)

Selects artifact version from GitHub artifact list

AAP playbook handles deployment:

Downloads artifact

Configures IIS

Recycles or starts app pool

🚫 GitHub Actions Limitations for CD

Limitation

Impact

No built-in Windows IIS deployment tasks

Requires complex scripting

No native inventory/host group management

Hard to manage multi-team releases

No RBAC for environment control

Risk of unauthorized deployments

No release dashboards or audits

Poor visibility post-release

