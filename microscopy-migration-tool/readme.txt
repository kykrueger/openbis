** The tool requires:

1. DSS service.properties configured with:

dss-rpc.put-default = eln-lims-dropbox
dss-rpc.put.ATTACHMENT = eln-lims-dropbox

2. A tags.txt file on the root to migrate the tags

The information can be retrieved from the database using the next SQL:

psql -U postgres -d openbis_standard
\o tags.txt

SELECT '"' || m.name || '"	"' || ea.perm_id ||'"'
FROM metaproject_assignments_all ma 
LEFT JOIN metaprojects m ON (m.id = ma.mepr_id) 
LEFT JOIN experiments_all ea ON (ea.id = ma.expe_id);

It follows the next format, including quotes, one tag per line:
"TagName"\t"permId"

** The tool produces:

1. Output of the process.

2. A file called openbis_audit_data_update.sql.

This file is a sql script that needs to be executed.
It migrates the audit data from the translated experiments to samples.